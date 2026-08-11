package com.hanzi.drivethru.data.vehicle

import com.hanzi.drivethru.core.model.CarSignalReading
import com.hanzi.drivethru.core.model.CarSignalSource
import com.hanzi.drivethru.core.model.CarSignalStatus
import com.hanzi.drivethru.core.model.CarSignalType

class FakeCarDataFacade : CarDataFacade {
    private val values = mutableMapOf(
        CarSignalType.GEAR_SELECTION to CarSignalType.GEAR_SELECTION.defaultValue,
        CarSignalType.VEHICLE_SPEED to CarSignalType.VEHICLE_SPEED.defaultValue,
        CarSignalType.PARKING_BRAKE to CarSignalType.PARKING_BRAKE.defaultValue,
        CarSignalType.IGNITION_STATE to CarSignalType.IGNITION_STATE.defaultValue,
    )

    override fun read(type: CarSignalType): CarSignalReading {
        return CarSignalReading(
            type = type,
            rawValue = values[type] ?: type.defaultValue,
            source = CarSignalSource.FAKE,
            status = CarSignalStatus.OK,
            timestampMillis = System.currentTimeMillis(),
            detail = "Fake signal provider",
        )
    }

    fun update(type: CarSignalType, value: Any) {
        values[type] = value
    }
}
