package com.hanzi.drivethru.data.menu

import com.hanzi.drivethru.core.model.MenuItem
import com.hanzi.drivethru.core.model.MenuSection
import com.hanzi.drivethru.data.tenant.TenantCatalogRepository

class TenantMenuRepository(
    private val tenantCatalogRepository: TenantCatalogRepository,
    private val fallbackRepository: MenuRepository = FakeMenuRepository(),
) : StoreScopedMenuRepository {
    private var activeStoreId: String? = null

    override fun activateStore(storeId: String) {
        activeStoreId = storeId
    }

    override fun getActiveStoreId(): String? = activeStoreId

    override fun getSyncStatus(): String = tenantCatalogRepository.getStatus()

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
        val storeId = activeStoreId ?: return fallbackRepository.getAllMenuItems()
        return tenantCatalogRepository.loadMenuCatalog(storeId).items.ifEmpty {
            fallbackRepository.getAllMenuItems()
        }
    }

    override fun findMenuItemById(itemId: String): MenuItem? {
        return getAllMenuItems().firstOrNull { it.id == itemId && it.available }
    }

    override fun getSeededCart(): List<SeededCartConfig> = fallbackRepository.getSeededCart()
}
