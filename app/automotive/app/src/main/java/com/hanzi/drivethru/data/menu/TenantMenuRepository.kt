package com.hanzi.drivethru.data.menu

import com.hanzi.drivethru.core.model.MenuItem
import com.hanzi.drivethru.core.model.MenuSection
import com.hanzi.drivethru.data.menu.local.MenuDao
import com.hanzi.drivethru.data.menu.local.toDomain
import com.hanzi.drivethru.data.menu.local.toEntity
import com.hanzi.drivethru.data.tenant.TenantCatalogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

/**
 * Room is the source of truth for menu data (see [MenuDao]). This repository never hands a
 * screen a server response directly: a beacon/geofence entry event only ever
 * (1) subscribes to whatever is already cached for the store - instant, offline-safe - and
 * (2) kicks off a background [refreshFromServer] that writes into Room, which is what actually
 * fans the update out to every observer (Compose custom UI and the legacy Car App Library
 * screens alike).
 */
class TenantMenuRepository(
    private val tenantCatalogRepository: TenantCatalogRepository,
    private val menuDao: MenuDao,
    private val fallbackRepository: MenuRepository = FakeMenuRepository(),
) : StoreScopedMenuRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val syncStatus = AtomicReference("Tenant menu repository idle.")
    private val cachedSnapshot = AtomicReference<List<MenuItem>>(emptyList())
    private var activeStoreId: String? = null
    private var cacheObservationJob: Job? = null
    private var refreshJob: Job? = null

    override fun activateStore(storeId: String) {
        val storeChanged = activeStoreId != storeId
        activeStoreId = storeId

        if (storeChanged) {
            cacheObservationJob?.cancel()
            cacheObservationJob = repositoryScope.launch {
                observeMenuItems(storeId).collectLatest { items ->
                    cachedSnapshot.set(items)
                }
            }
        }

        // Local-first: the collectLatest above already surfaced whatever Room had cached for
        // this store the moment it was launched. This refresh is purely the "sync with server"
        // half, running independently so a slow/failed network call never blocks that read.
        refreshJob?.cancel()
        refreshJob = repositoryScope.launch {
            refreshFromServer(storeId)
        }
    }

    override fun getActiveStoreId(): String? = activeStoreId

    override fun getSyncStatus(): String = syncStatus.get()

    override fun observeMenuItems(storeId: String): Flow<List<MenuItem>> {
        return menuDao.observeMenuItems(storeId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getMenuSections(): List<MenuSection> {
        val titles = mapOf(
            "burger" to "Burgers",
            "drink" to "Beverages",
            "side" to "Sides",
            "dessert" to "Desserts",
            "kimbap" to "Kimbap",
            "meal" to "Meals",
        )
        return getAllMenuItems()
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

    override fun getAllMenuItems(): List<MenuItem> {
        return cachedSnapshot.get().ifEmpty { fallbackRepository.getAllMenuItems() }
    }

    override fun findMenuItemById(itemId: String): MenuItem? {
        return getAllMenuItems().firstOrNull { it.id == itemId && it.available }
    }

    override fun getSeededCart(): List<SeededCartConfig> = fallbackRepository.getSeededCart()

    private suspend fun refreshFromServer(storeId: String) {
        syncStatus.set("Showing cached menu for $storeId. Refreshing from server...")
        val remoteCatalog = runCatching { tenantCatalogRepository.loadMenuCatalog(storeId) }.getOrNull()
        if (remoteCatalog != null && remoteCatalog.items.isNotEmpty()) {
            menuDao.replaceStoreMenu(storeId, remoteCatalog.items.map { it.toEntity(storeId) })
            syncStatus.set("Menu synced from server for $storeId.")
        } else {
            syncStatus.set("Server refresh failed for $storeId; showing last cached menu.")
        }
    }
}
