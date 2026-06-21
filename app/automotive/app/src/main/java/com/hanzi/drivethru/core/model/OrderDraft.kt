package com.hanzi.drivethru.core.model

data class OrderLineItem(
    val menuItem: MenuItem,
    val quantity: Int,
)

data class OrderDraft(
    val storeName: String,
    val items: List<OrderLineItem>,
) {
    val totalPrice: Int
        get() = items.sumOf { it.menuItem.price * it.quantity }
}
