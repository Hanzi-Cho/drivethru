package com.hanzi.drivethru.core.model

data class MenuSection(
    val id: String,
    val title: String,
    val items: List<MenuItem>,
)
