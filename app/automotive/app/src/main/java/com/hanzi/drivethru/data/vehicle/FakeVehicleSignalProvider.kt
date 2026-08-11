package com.hanzi.drivethru.data.vehicle

import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.model.VehicleSignalSnapshot

class FakeVehicleSignalProvider(
    initialSnapshot: VehicleSignalSnapshot = VehicleSignalSnapshot(
        gearState = GearState.DRIVE,
        speedMetersPerSecond = 0.0,
        timestampMillis = System.currentTimeMillis(),
    ),
) : VehicleSignalProvider {
    private var currentSnapshot: VehicleSignalSnapshot = initialSnapshot

    override fun getSnapshot(): VehicleSignalSnapshot = currentSnapshot

    override fun updateGearState(gearState: GearState) {
        currentSnapshot = currentSnapshot.copy(
            gearState = gearState,
            timestampMillis = System.currentTimeMillis(),
        )
    }

    override fun updateSpeed(speedMetersPerSecond: Double) {
        currentSnapshot = currentSnapshot.copy(
            speedMetersPerSecond = speedMetersPerSecond,
            timestampMillis = System.currentTimeMillis(),
        )
    }
}
