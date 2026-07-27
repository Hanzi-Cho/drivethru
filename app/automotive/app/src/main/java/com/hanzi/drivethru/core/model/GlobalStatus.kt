package com.hanzi.drivethru.core.model

data class GlobalStatus(
    val cautionLabel: String,
    val dataLevel: String,
    val wifiLevel: String,
    val batteryLevel: String,
    val timeLabel: String,
)
