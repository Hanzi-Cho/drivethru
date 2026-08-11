package com.hanzi.drivethru.data.vehicle

import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.model.CarSignalReading
import com.hanzi.drivethru.core.model.VehicleSignalSnapshot

interface VehicleSignalProvider {
    fun getSnapshot(): VehicleSignalSnapshot
    fun updateGearState(gearState: GearState)
    fun updateSpeed(speedMetersPerSecond: Double)
    fun getDiagnostics(): List<CarSignalReading>
}
