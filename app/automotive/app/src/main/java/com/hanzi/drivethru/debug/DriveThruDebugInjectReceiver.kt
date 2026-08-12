package com.hanzi.drivethru.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.di.DriveThruRuntime

class DriveThruDebugInjectReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DriveThruDebugActions.ACTION_INJECT_DEBUG_EVENT) {
            return
        }

        val coordinator = DriveThruRuntime.get(context).customUiFlowCoordinator
        if (intent.getBooleanExtra(DriveThruDebugActions.EXTRA_RESET_SESSION, false)) {
            coordinator.resetEntryTrigger()
        }

        val gearState = intent.getStringExtra(DriveThruDebugActions.EXTRA_GEAR)
            ?.let(DriveThruDebugActions::parseGear)
        val parking = intent.extras?.takeIf { it.containsKey(DriveThruDebugActions.EXTRA_PARKING) }
            ?.getBoolean(DriveThruDebugActions.EXTRA_PARKING)
        val speedMetersPerSecond = intent.readNumberExtra(DriveThruDebugActions.EXTRA_SPEED_MPS)

        val resolvedGearState = when {
            gearState != null -> gearState
            parking == true -> GearState.PARK
            parking == false -> GearState.DRIVE
            else -> null
        }
        resolvedGearState?.let(coordinator::setGearState)
        speedMetersPerSecond?.let(coordinator::setVehicleSpeed)

        val source = intent.getStringExtra(DriveThruDebugActions.EXTRA_SOURCE)?.lowercase()
        val stage = DriveThruDebugActions.parseStage(intent.getStringExtra(DriveThruDebugActions.EXTRA_STAGE))
        val lanePoint = DriveThruDebugActions.parseLanePoint(intent.getStringExtra(DriveThruDebugActions.EXTRA_LANE_POINT))

        when {
            stage == DriveThruZoneStage.EXIT || stage == DriveThruZoneStage.OUTSIDE -> {
                coordinator.resetEntryTrigger()
            }

            source == DriveThruDebugActions.SOURCE_BEACON || intent.hasExtra(DriveThruDebugActions.EXTRA_BEACON_ID) -> {
                val beaconId = intent.getStringExtra(DriveThruDebugActions.EXTRA_BEACON_ID) ?: "beacon-mcd-001"
                val resolvedStage = stage ?: DriveThruZoneStage.ORDERING_READY
                coordinator.injectBeaconTrigger(
                    stage = resolvedStage,
                    beaconId = beaconId,
                    lanePoint = lanePoint,
                )
            }

            source == DriveThruDebugActions.SOURCE_GPS ||
                (intent.hasExtra(DriveThruDebugActions.EXTRA_LATITUDE) && intent.hasExtra(DriveThruDebugActions.EXTRA_LONGITUDE)) -> {
                val latitude = intent.readNumberExtra(DriveThruDebugActions.EXTRA_LATITUDE) ?: 37.4979
                val longitude = intent.readNumberExtra(DriveThruDebugActions.EXTRA_LONGITUDE) ?: 127.0276
                val resolvedStage = stage ?: DriveThruZoneStage.APPROACHING
                coordinator.injectGpsTrigger(
                    stage = resolvedStage,
                    latitude = latitude,
                    longitude = longitude,
                    lanePoint = lanePoint,
                )
            }
        }
    }

    private fun Intent.readNumberExtra(key: String): Double? {
        val value = extras?.get(key) ?: return null
        return when (value) {
            is Double -> value
            is Float -> value.toDouble()
            is Int -> value.toDouble()
            is Long -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }
}
