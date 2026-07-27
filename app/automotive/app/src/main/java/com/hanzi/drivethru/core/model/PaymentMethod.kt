package com.hanzi.drivethru.core.model

data class PaymentMethod(
    val displayName: String,
    val maskedNumber: String,
    val autoPayLabel: String,
)
