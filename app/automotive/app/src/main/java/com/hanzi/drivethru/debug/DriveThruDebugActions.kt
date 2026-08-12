package com.hanzi.drivethru.debug

import com.hanzi.drivethru.core.model.DriveThruLanePoint
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import com.hanzi.drivethru.core.model.GearState

object DriveThruDebugActions {
    const val ACTION_INJECT_DEBUG_EVENT = "com.hanzi.drivethru.action.INJECT_DEBUG_EVENT"

    const val EXTRA_SOURCE = "source"
    const val EXTRA_STAGE = "stage"
    const val EXTRA_LANE_POINT = "lane_point"
    const val EXTRA_LATITUDE = "latitude"
    const val EXTRA_LONGITUDE = "longitude"
    const val EXTRA_BEACON_ID = "beacon_id"
    const val EXTRA_SPEED_MPS = "speed_mps"
    const val EXTRA_GEAR = "gear"
    const val EXTRA_PARKING = "parking"
    const val EXTRA_RESET_SESSION = "reset_session"

    const val SOURCE_GPS = "gps"
    const val SOURCE_BEACON = "beacon"
    const val SOURCE_VEHICLE = "vehicle"

    fun parseStage(raw: String?): DriveThruZoneStage? {
        return DriveThruZoneStage.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
    }

    fun parseLanePoint(raw: String?): DriveThruLanePoint? {
        return DriveThruLanePoint.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
    }

    fun parseGear(raw: String?): GearState? {
        return GearState.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
    }
}
