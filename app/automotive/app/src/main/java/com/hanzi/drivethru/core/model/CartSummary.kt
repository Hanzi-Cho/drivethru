package com.hanzi.drivethru.core.model

data class CartSummary(
    val subtotal: Int,
    val discount: Int,
    val total: Int,
    val itemCount: Int,
)
