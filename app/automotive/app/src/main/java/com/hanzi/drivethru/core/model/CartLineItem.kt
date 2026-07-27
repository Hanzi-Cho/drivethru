package com.hanzi.drivethru.core.model

data class CartLineItem(
    val menuItem: MenuItem,
    val quantity: Int,
    val selectedOptions: List<String>,
) {
    val totalPrice: Int
        get() = menuItem.price * quantity
}
