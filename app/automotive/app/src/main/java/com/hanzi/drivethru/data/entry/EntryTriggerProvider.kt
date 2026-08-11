package com.hanzi.drivethru.data.entry

import com.hanzi.drivethru.core.model.DriveThruLanePoint
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import com.hanzi.drivethru.core.model.EntryTriggerEvent

interface EntryTriggerProvider {
    fun currentEvent(): EntryTriggerEvent
    fun simulateGps(stage: DriveThruZoneStage, latitude: Double, longitude: Double, lanePoint: DriveThruLanePoint? = null)
    fun simulateBeacon(stage: DriveThruZoneStage, beaconId: String, lanePoint: DriveThruLanePoint? = null)
    fun resetToOutside()
}
