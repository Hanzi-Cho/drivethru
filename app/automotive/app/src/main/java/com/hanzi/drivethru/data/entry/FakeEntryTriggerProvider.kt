package com.hanzi.drivethru.data.entry

import com.hanzi.drivethru.core.model.DriveThruLanePoint
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import com.hanzi.drivethru.core.model.EntryTriggerEvent
import com.hanzi.drivethru.core.model.EntryTriggerSource

class FakeEntryTriggerProvider : EntryTriggerProvider {
    private var current = EntryTriggerEvent.outside()

    override fun currentEvent(): EntryTriggerEvent = current

    override fun simulateGps(stage: DriveThruZoneStage, latitude: Double, longitude: Double, lanePoint: DriveThruLanePoint?) {
        current = EntryTriggerEvent(
            source = EntryTriggerSource.GPS_GEOFENCE,
            stage = stage,
            lanePoint = lanePoint,
            latitude = latitude,
            longitude = longitude,
            beaconId = null,
            timestampMillis = System.currentTimeMillis(),
        )
    }

    override fun simulateBeacon(stage: DriveThruZoneStage, beaconId: String, lanePoint: DriveThruLanePoint?) {
        current = EntryTriggerEvent(
            source = EntryTriggerSource.BEACON,
            stage = stage,
            lanePoint = lanePoint,
            latitude = null,
            longitude = null,
            beaconId = beaconId,
            timestampMillis = System.currentTimeMillis(),
        )
    }

    override fun resetToOutside() {
        current = EntryTriggerEvent.outside()
    }
}
