package com.hanzi.drivethru.core.model

data class VehicleSignalSnapshot(
    val gearState: GearState,
    val speedMetersPerSecond: Double,
    val timestampMillis: Long,
) {
    val motionState: VehicleMotionState
        get() = if (speedMetersPerSecond > 0.5) VehicleMotionState.MOVING else VehicleMotionState.STOPPED
}
