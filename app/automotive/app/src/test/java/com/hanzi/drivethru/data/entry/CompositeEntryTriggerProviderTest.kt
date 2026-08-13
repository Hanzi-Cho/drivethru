package com.hanzi.drivethru.data.entry

import com.hanzi.drivethru.core.model.DriveThruLanePoint
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import org.junit.Assert.assertEquals
import org.junit.Test

class CompositeEntryTriggerProviderTest {
    @Test
    fun `composite forwards gps simulation to child providers`() {
        val gpsProvider = FakeEntryTriggerProvider()
        val beaconProvider = FakeEntryTriggerProvider()
        val provider = CompositeEntryTriggerProvider(listOf(gpsProvider, beaconProvider))

        provider.simulateGps(
            stage = DriveThruZoneStage.ORDERING_READY,
            latitude = 37.4979,
            longitude = 127.0276,
            lanePoint = DriveThruLanePoint.MENU_BOARD,
        )

        assertEquals(DriveThruZoneStage.ORDERING_READY, gpsProvider.currentEvent().stage)
        assertEquals(DriveThruZoneStage.ORDERING_READY, beaconProvider.currentEvent().stage)
    }
}
