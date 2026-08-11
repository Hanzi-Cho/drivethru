package com.hanzi.drivethru.core.state

import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.model.StopStateReason
import com.hanzi.drivethru.core.model.VehicleSignalSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DriveThruSafetyPolicyTest {
    private val safetyPolicy = DriveThruSafetyPolicy()

    @Test
    fun `full ordering is allowed only in park`() {
        val parkSnapshot = VehicleSignalSnapshot(
            gearState = GearState.PARK,
            speedMetersPerSecond = 0.0,
            timestampMillis = 1L,
        )
        val driveSnapshot = parkSnapshot.copy(gearState = GearState.DRIVE)

        assertTrue(safetyPolicy.canShowFullOrderingUi(parkSnapshot))
        assertFalse(safetyPolicy.canShowFullOrderingUi(driveSnapshot))
    }

    @Test
    fun `moving speed triggers moving stop state reason`() {
        val snapshot = VehicleSignalSnapshot(
            gearState = GearState.PARK,
            speedMetersPerSecond = 1.2,
            timestampMillis = 1L,
        )

        assertEquals(
            StopStateReason.VEHICLE_STARTED_MOVING,
            safetyPolicy.determineStopStateReason(snapshot),
        )
    }
}
