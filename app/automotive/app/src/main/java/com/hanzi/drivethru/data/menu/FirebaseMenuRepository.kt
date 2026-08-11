package com.hanzi.drivethru.data.menu

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hanzi.drivethru.core.model.MenuItem
import com.hanzi.drivethru.core.model.MenuSection
import java.util.concurrent.atomic.AtomicReference

class FirebaseMenuRepository(
    context: Context,
    private val fallback: MenuRepository,
) : StoreScopedMenuRepository {
    private val cache = AtomicReference(fallback.getAllMenuItems())
    private val status = AtomicReference("Firebase unavailable, using fake menu fallback.")
    private var activeStoreId: String? = null

    init {
        tryInitialize(context)
    }

    override fun getMenuSections(): List<MenuSection> {
        val titles = mapOf(
            "burger" to "Burgers",
            "drink" to "Beverages",
            "side" to "Sides",
            "dessert" to "Desserts",
        )
        return cache.get()
            .filter { it.available }
            .groupBy { it.category }
            .map { (category, items) ->
                MenuSection(
                    id = category,
                    title = titles[category] ?: category.replaceFirstChar { it.uppercase() },
                    items = items,
                )
            }
    }

    override fun getAllMenuItems(): List<MenuItem> = cache.get()

    override fun findMenuItemById(itemId: String): MenuItem? {
        return cache.get().firstOrNull { it.id == itemId && it.available }
    }

    override fun getSeededCart(): List<SeededCartConfig> = fallback.getSeededCart()

    override fun getSyncStatus(): String = status.get()

    override fun activateStore(storeId: String) {
        activeStoreId = storeId
        val apps = FirebaseApp.getApps(contextRef)
        if (apps.isNotEmpty()) {
            val database = FirebaseDatabase.getInstance()
            subscribeToMenu(database.reference.child("stores").child(storeId).child("menu"))
        }
    }

    override fun getActiveStoreId(): String? = activeStoreId

    private val contextRef = context

    private fun tryInitialize(context: Context) {
        val apps = FirebaseApp.getApps(context)
        if (apps.isEmpty()) {
            status.set("Firebase not configured in this build. Running with fake menu data.")
            return
        }
        status.set("Firebase configured. Waiting for active store selection.")
    }

    private fun subscribeToMenu(reference: DatabaseReference) {
        status.set("Firebase connected. Waiting for realtime menu sync.")
        reference.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = snapshot.children.mapNotNull { child ->
                        val id = child.key ?: return@mapNotNull null
                        val name = child.child("name").getValue(String::class.java) ?: return@mapNotNull null
                        val price = child.child("price").getValue(Int::class.java) ?: 0
                        val category = child.child("category").getValue(String::class.java) ?: "uncategorized"
                        val available = child.child("available").getValue(Boolean::class.java) ?: true
                        MenuItem(
                            id = id,
                            name = name,
                            price = price,
                            category = category,
                            available = available,
                            description = child.child("description").getValue(String::class.java)
                                ?: "Realtime synced menu item",
                            quickOrderEligible = child.child("quickOrderEligible").getValue(Boolean::class.java) ?: false,
                        )
                    }

                    if (items.isNotEmpty()) {
                        cache.set(items)
                        status.set("Firebase realtime menu sync active.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    status.set("Firebase sync cancelled: ${error.message}. Falling back to cached menu.")
                }
            },
        )
    }
}
