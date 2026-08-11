package com.hanzi.drivethru.data.entry

import android.content.Context
import com.hanzi.drivethru.core.model.DriveThruLanePoint
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import com.hanzi.drivethru.core.model.EntryTriggerEvent

class AndroidGeofenceEntryTriggerProvider(
    @Suppress("UNUSED_PARAMETER") private val context: Context,
) : EntryTriggerProvider {
    private val fallback = FakeEntryTriggerProvider()

    override fun currentEvent(): EntryTriggerEvent = fallback.currentEvent()

    override fun simulateGps(stage: DriveThruZoneStage, latitude: Double, longitude: Double, lanePoint: DriveThruLanePoint?) {
        fallback.simulateGps(stage, latitude, longitude, lanePoint)
    }

    override fun simulateBeacon(stage: DriveThruZoneStage, beaconId: String, lanePoint: DriveThruLanePoint?) {
        fallback.simulateBeacon(stage, beaconId, lanePoint)
    }

    override fun resetToOutside() {
        fallback.resetToOutside()
    }
}
