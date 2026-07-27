package com.hanzi.drivethru.data.menu

data class SeededCartConfig(
    val menuItemId: String,
    val quantity: Int,
    val selectedOptions: List<String>,
)
