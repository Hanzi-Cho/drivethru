package com.hanzi.drivethru.data.menu

import com.hanzi.drivethru.core.model.MenuItem
import com.hanzi.drivethru.core.model.MenuSection

class FakeMenuRepository : MenuRepository {
    private val menuItems = listOf(
        MenuItem(
            id = "double_beef_burger_set",
            name = "Double beef burger set",
            price = 8900,
            category = "burger",
            available = true,
            description = "Signature burger combo for the main menu layout",
            quickOrderEligible = true,
        ),
        MenuItem(
            id = "cheese_fries_large",
            name = "Cheese fries (L)",
            price = 4500,
            category = "side",
            available = true,
            description = "Popular side item for the cart summary layout",
            quickOrderEligible = true,
        ),
        MenuItem(
            id = "vanilla_shake",
            name = "Vanilla shake",
            price = 2000,
            category = "dessert",
            available = true,
            description = "Light dessert item for a compact order summary",
            quickOrderEligible = true,
        ),
        MenuItem(
            id = "iced_americano",
            name = "Iced americano",
            price = 4500,
            category = "drink",
            available = true,
            description = "Fast beverage option for one-tap ordering",
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

    override fun getMenuSections(): List<MenuSection> {
        val titles = mapOf(
            "burger" to "Burgers",
            "drink" to "Beverages",
            "side" to "Sides",
            "dessert" to "Desserts",
        )
        return menuItems
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
        return menuItems.filter { it.available }
    }

    override fun findMenuItemById(itemId: String): MenuItem? {
        return menuItems.firstOrNull { it.id == itemId && it.available }
    }

    override fun getSeededCart(): List<SeededCartConfig> {
        return listOf(
            SeededCartConfig(
                menuItemId = "double_beef_burger_set",
                quantity = 1,
                selectedOptions = listOf("Regular set", "Zero cola", "No onion"),
            ),
            SeededCartConfig(
                menuItemId = "cheese_fries_large",
                quantity = 1,
                selectedOptions = listOf("Cheese sauce"),
            ),
            SeededCartConfig(
                menuItemId = "vanilla_shake",
                quantity = 1,
                selectedOptions = listOf("Extra whipped cream"),
            ),
        )
    }
}
