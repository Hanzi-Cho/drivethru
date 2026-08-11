package com.hanzi.drivethru.core.model

data class MenuOptionChoice(
    val id: String,
    val label: String,
    val priceDelta: Int,
    val imageUrl: String? = null,
)

data class MenuOptionGroup(
    val id: String,
    val title: String,
    val required: Boolean,
    val minSelections: Int,
    val maxSelections: Int,
    val choices: List<MenuOptionChoice>,
)
