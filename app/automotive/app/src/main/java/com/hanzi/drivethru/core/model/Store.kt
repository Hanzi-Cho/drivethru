package com.hanzi.drivethru.core.model

data class Store(
    val id: String,
    val name: String,
    val capabilities: Set<StoreCapability>,
    val menuSource: String,
)
