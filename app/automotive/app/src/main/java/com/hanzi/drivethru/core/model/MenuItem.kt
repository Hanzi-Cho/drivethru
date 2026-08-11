package com.hanzi.drivethru.core.model

data class MenuItem(
    val id: String,
    val name: String,
    val price: Int,
    val category: String,
    val available: Boolean,
    val description: String,
    val quickOrderEligible: Boolean,
    val imageUrl: String? = null,
    val optionGroups: List<MenuOptionGroup> = emptyList(),
)
