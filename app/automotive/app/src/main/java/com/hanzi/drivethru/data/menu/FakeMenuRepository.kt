package com.hanzi.drivethru.data.menu

import com.hanzi.drivethru.core.model.MenuItem

class FakeMenuRepository : MenuRepository {
    private val menuItems = listOf(
        MenuItem(
            id = "burger_classic_set",
            name = "Classic burger set",
            price = 8900,
            category = "burger",
            available = true,
            description = "Fastest safe option while staying in drive",
            quickOrderEligible = true,
        ),
        MenuItem(
            id = "iced_americano",
            name = "Iced americano",
            price = 2500,
            category = "drink",
            available = true,
            description = "Simple beverage shortcut for the first demo",
            quickOrderEligible = true,
        ),
        MenuItem(
            id = "double_cheese_burger",
            name = "Double cheese burger set",
            price = 10500,
            category = "burger",
            available = true,
            description = "Full browse mode item available only while parked",
            quickOrderEligible = false,
        ),
        MenuItem(
            id = "spicy_chicken_combo",
            name = "Spicy chicken combo",
            price = 9900,
            category = "burger",
            available = true,
            description = "Second primary item for the parked browsing flow",
            quickOrderEligible = false,
        ),
        MenuItem(
            id = "vanilla_latte",
            name = "Vanilla latte",
            price = 4800,
            category = "drink",
            available = true,
            description = "Parked browsing beverage recommendation",
            quickOrderEligible = false,
        ),
        MenuItem(
            id = "lemon_ade",
            name = "Lemon ade",
            price = 4300,
            category = "drink",
            available = true,
            description = "Cold drink option for the full menu state",
            quickOrderEligible = false,
        ),
    )

    override fun getQuickOrderMenu(storeName: String): List<MenuItem> {
        return menuItems.filter { it.available && it.quickOrderEligible }
    }

    override fun getFullMenu(storeName: String): List<MenuItem> {
        return menuItems.filter { it.available }
    }

    override fun findMenuItemById(storeName: String, itemId: String): MenuItem? {
        return menuItems.firstOrNull { it.id == itemId && it.available }
    }
}
