package com.hanzi.drivethru.data.vehicle

import com.hanzi.drivethru.core.model.CarSignalCatalog
import com.hanzi.drivethru.core.model.CarSignalReading
import com.hanzi.drivethru.core.model.CarSignalType
import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.model.VehicleSignalSnapshot

class CarDataVehicleSignalProvider(
    private val carDataFacade: CarDataFacade,
    private val fakeCarDataFacade: FakeCarDataFacade,
) : VehicleSignalProvider {
    override fun getSnapshot(): VehicleSignalSnapshot {
        val gearReading = carDataFacade.read(CarSignalType.GEAR_SELECTION)
        val speedReading = carDataFacade.read(CarSignalType.VEHICLE_SPEED)
        val gearOrdinal = (gearReading.intValue() ?: GearState.UNKNOWN.ordinal)
            .coerceIn(0, GearState.entries.lastIndex)

        return VehicleSignalSnapshot(
            gearState = GearState.entries[gearOrdinal],
            speedMetersPerSecond = (speedReading.floatValue() ?: 0f).toDouble(),
            timestampMillis = maxOf(gearReading.timestampMillis, speedReading.timestampMillis),
        )
    }

    override fun updateGearState(gearState: GearState) {
        fakeCarDataFacade.update(CarSignalType.GEAR_SELECTION, gearState.ordinal)
        fakeCarDataFacade.update(CarSignalType.PARKING_BRAKE, gearState == GearState.PARK)
    }

    override fun updateSpeed(speedMetersPerSecond: Double) {
        fakeCarDataFacade.update(CarSignalType.VEHICLE_SPEED, speedMetersPerSecond.toFloat())
    }

    override fun getDiagnostics(): List<CarSignalReading> = carDataFacade.snapshot(CarSignalCatalog.defaultSignals)
}
