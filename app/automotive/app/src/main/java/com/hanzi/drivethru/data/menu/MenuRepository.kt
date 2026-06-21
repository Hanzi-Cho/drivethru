package com.hanzi.drivethru.data.menu

import com.hanzi.drivethru.core.model.MenuItem

interface MenuRepository {
    fun getQuickOrderMenu(storeName: String): List<MenuItem>
    fun getFullMenu(storeName: String): List<MenuItem>
    fun findMenuItemById(storeName: String, itemId: String): MenuItem?
}
