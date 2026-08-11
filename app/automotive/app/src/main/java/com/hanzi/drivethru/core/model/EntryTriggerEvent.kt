package com.hanzi.drivethru.core.model

enum class EntryTriggerSource {
    GPS_GEOFENCE,
    BEACON,
}

data class EntryTriggerEvent(
    val source: EntryTriggerSource,
    val stage: DriveThruZoneStage,
    val lanePoint: DriveThruLanePoint?,
    val latitude: Double?,
    val longitude: Double?,
    val beaconId: String?,
    val timestampMillis: Long,
) {
    companion object {
        fun outside(): EntryTriggerEvent {
            return EntryTriggerEvent(
                source = EntryTriggerSource.GPS_GEOFENCE,
                stage = DriveThruZoneStage.OUTSIDE,
                lanePoint = null,
                latitude = null,
                longitude = null,
                beaconId = null,
                timestampMillis = System.currentTimeMillis(),
            )
        }
    }
}
