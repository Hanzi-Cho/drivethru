package com.hanzi.drivethru.data.menu.local

import androidx.room.Entity

/**
 * Persistent, per-store cache row. This table is the single source of truth for menu data:
 * every screen reads from here, and the network is only ever a writer into it.
 */
@Entity(tableName = "menu_items", primaryKeys = ["storeId", "id"])
data class MenuItemEntity(
    val storeId: String,
    val id: String,
    val name: String,
    val price: Int,
    val category: String,
    val available: Boolean,
    val description: String,
    val quickOrderEligible: Boolean,
    val imageUrl: String?,
    val optionGroupsJson: String,
)
