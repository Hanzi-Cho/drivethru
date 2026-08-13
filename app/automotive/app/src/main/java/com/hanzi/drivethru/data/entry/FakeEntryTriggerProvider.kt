package com.hanzi.drivethru.data.entry

import com.hanzi.drivethru.core.model.DriveThruLanePoint
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import com.hanzi.drivethru.core.model.EntryTriggerEvent
import com.hanzi.drivethru.core.model.EntryTriggerSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeEntryTriggerProvider : EntryTriggerProvider {
    private val mutableEventFlow = MutableStateFlow(EntryTriggerEvent.outside())

    override val eventFlow: StateFlow<EntryTriggerEvent> = mutableEventFlow.asStateFlow()

    override fun currentEvent(): EntryTriggerEvent = mutableEventFlow.value

    override fun start() = Unit

    override fun stop() = Unit

    override fun simulateGps(stage: DriveThruZoneStage, latitude: Double, longitude: Double, lanePoint: DriveThruLanePoint?) {
        publishEvent(
            EntryTriggerEvent(
                source = EntryTriggerSource.GPS_GEOFENCE,
                stage = stage,
                lanePoint = lanePoint,
                latitude = latitude,
                longitude = longitude,
                beaconId = null,
                timestampMillis = System.currentTimeMillis(),
            ),
        )
    }

    override fun simulateBeacon(stage: DriveThruZoneStage, beaconId: String, lanePoint: DriveThruLanePoint?) {
        publishEvent(
            EntryTriggerEvent(
                source = EntryTriggerSource.BEACON,
                stage = stage,
                lanePoint = lanePoint,
                latitude = null,
                longitude = null,
                beaconId = beaconId,
                timestampMillis = System.currentTimeMillis(),
            ),
        )
    }

    override fun resetToOutside() {
        publishEvent(EntryTriggerEvent.outside())
    }

    fun publishEvent(event: EntryTriggerEvent) {
        mutableEventFlow.value = event
    }
}
