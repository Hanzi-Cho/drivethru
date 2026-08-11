package com.hanzi.drivethru.core.model

enum class CarSignalSource {
    FAKE,
    CAR_PROPERTY_MANAGER,
    FIREBASE_CACHE,
}

enum class CarSignalStatus {
    OK,
    FALLBACK,
    UNAVAILABLE,
}

data class CarSignalReading(
    val type: CarSignalType,
    val rawValue: Any,
    val source: CarSignalSource,
    val status: CarSignalStatus,
    val timestampMillis: Long,
    val detail: String,
) {
    fun intValue(): Int? = rawValue as? Int
    fun floatValue(): Float? = rawValue as? Float
    fun booleanValue(): Boolean? = rawValue as? Boolean
}
