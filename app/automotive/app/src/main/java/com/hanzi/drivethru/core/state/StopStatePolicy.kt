package com.hanzi.drivethru.core.state

import com.hanzi.drivethru.core.model.CustomUiDestination
import com.hanzi.drivethru.core.model.StopStateReason
import com.hanzi.drivethru.core.model.VehicleSignalSnapshot

class StopStatePolicy(
    private val safetyPolicy: DriveThruSafetyPolicy,
) {
    fun evaluateTransition(
        currentDestination: CustomUiDestination,
        snapshot: VehicleSignalSnapshot,
    ): StopStateReason? {
        val orderingVisible = currentDestination == CustomUiDestination.FULL_MENU ||
            currentDestination == CustomUiDestination.CART_REVIEW
        if (!orderingVisible) {
            return null
        }

        return safetyPolicy.determineStopStateReason(snapshot)
    }
}
