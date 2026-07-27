package com.hanzi.drivethru.data.menu

import com.hanzi.drivethru.core.model.MenuItem
import com.hanzi.drivethru.core.model.MenuSection

interface MenuRepository {
    fun getMenuSections(): List<MenuSection>
    fun getAllMenuItems(): List<MenuItem>
    fun findMenuItemById(itemId: String): MenuItem?
    fun getSeededCart(): List<SeededCartConfig>
}
