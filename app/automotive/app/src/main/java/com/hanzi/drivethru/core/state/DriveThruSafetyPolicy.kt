package com.hanzi.drivethru.core.state

import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.model.StopStateReason
import com.hanzi.drivethru.core.model.VehicleSignalSnapshot

class DriveThruSafetyPolicy {
    fun canShowFullOrderingUi(snapshot: VehicleSignalSnapshot): Boolean {
        return snapshot.gearState == GearState.PARK
    }

    fun determineStopStateReason(snapshot: VehicleSignalSnapshot): StopStateReason? {
        return when {
            snapshot.speedMetersPerSecond > 0.5 -> StopStateReason.VEHICLE_STARTED_MOVING
            snapshot.gearState != GearState.PARK -> StopStateReason.VEHICLE_LEFT_PARK
            else -> null
        }
    }
}
