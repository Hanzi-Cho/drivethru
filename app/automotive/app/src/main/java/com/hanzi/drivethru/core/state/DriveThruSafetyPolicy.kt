package com.hanzi.drivethru.core.state

import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.model.StopStateReason
import com.hanzi.drivethru.core.model.VehicleSignalSnapshot

class DriveThruSafetyPolicy {
    companion object {
        const val SESSION_ABORT_SPEED_METERS_PER_SECOND = 8.0
    }

    fun canShowFullOrderingUi(snapshot: VehicleSignalSnapshot): Boolean {
        return snapshot.gearState == GearState.PARK
    }

    fun shouldAbortOrderingSession(snapshot: VehicleSignalSnapshot): Boolean {
        return snapshot.speedMetersPerSecond >= SESSION_ABORT_SPEED_METERS_PER_SECOND
    }

    fun determineStopStateReason(snapshot: VehicleSignalSnapshot): StopStateReason? {
        return when {
            snapshot.speedMetersPerSecond > 0.5 -> StopStateReason.VEHICLE_STARTED_MOVING
            snapshot.gearState != GearState.PARK -> StopStateReason.VEHICLE_LEFT_PARK
            else -> null
        }
    }
}
