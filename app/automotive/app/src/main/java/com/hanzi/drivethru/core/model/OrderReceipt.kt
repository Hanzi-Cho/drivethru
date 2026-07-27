package com.hanzi.drivethru.core.model

data class OrderReceipt(
    val orderId: String,
    val storeName: String,
    val items: List<CartLineItem>,
    val paymentMethod: PaymentMethod,
    val totalAmount: Int,
    val pickupMessage: String,
)
