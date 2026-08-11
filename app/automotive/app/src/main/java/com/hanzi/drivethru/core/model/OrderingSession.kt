package com.hanzi.drivethru.core.model

data class OrderingSession(
    val store: Store,
    val orderDraft: OrderDraft,
    val startedAtMillis: Long,
    val lastUpdatedAtMillis: Long,
)
