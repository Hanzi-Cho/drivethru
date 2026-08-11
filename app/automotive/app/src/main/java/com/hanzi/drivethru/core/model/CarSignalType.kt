package com.hanzi.drivethru.core.model

enum class CarSignalValueKind {
    INT,
    FLOAT,
    BOOLEAN,
}

enum class CarSignalType(
    val propertyFieldName: String,
    val valueKind: CarSignalValueKind,
    val defaultValue: Any,
) {
    GEAR_SELECTION(
        propertyFieldName = "GEAR_SELECTION",
        valueKind = CarSignalValueKind.INT,
        defaultValue = GearState.DRIVE.ordinal,
    ),
    VEHICLE_SPEED(
        propertyFieldName = "PERF_VEHICLE_SPEED",
        valueKind = CarSignalValueKind.FLOAT,
        defaultValue = 0f,
    ),
    PARKING_BRAKE(
        propertyFieldName = "PARKING_BRAKE_ON",
        valueKind = CarSignalValueKind.BOOLEAN,
        defaultValue = false,
    ),
    IGNITION_STATE(
        propertyFieldName = "IGNITION_STATE",
        valueKind = CarSignalValueKind.INT,
        defaultValue = 0,
    ),
}

object CarSignalCatalog {
    val defaultSignals: List<CarSignalType> = listOf(
        CarSignalType.GEAR_SELECTION,
        CarSignalType.VEHICLE_SPEED,
        CarSignalType.PARKING_BRAKE,
        CarSignalType.IGNITION_STATE,
    )
}
