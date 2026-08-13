package com.hanzi.drivethru.data.entry

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.hanzi.drivethru.core.model.DriveThruLanePoint
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import com.hanzi.drivethru.core.model.EntryTriggerEvent
import kotlin.math.sqrt

class AndroidGeofenceEntryTriggerProvider(
    private val context: Context,
    private val tenantLocationCatalog: TenantLocationCatalog = TenantLocationCatalog(context),
) : EntryTriggerProvider {
    private val fallback = FakeEntryTriggerProvider()
    private val locationManager = context.getSystemService(LocationManager::class.java)
    private val locationListener = LocationListener { location -> handleLocation(location) }
    private var started = false

    override val eventFlow = fallback.eventFlow

    override fun currentEvent(): EntryTriggerEvent = fallback.currentEvent()

    override fun start() {
        if (started || locationManager == null || !hasLocationPermission()) {
            return
        }
        runCatching {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2_000L,
                10f,
                locationListener,
            )
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let(::handleLocation)
            started = true
        }
    }

    override fun stop() {
        if (!started || locationManager == null) {
            return
        }
        runCatching {
            locationManager.removeUpdates(locationListener)
        }
        started = false
    }

    override fun simulateGps(stage: DriveThruZoneStage, latitude: Double, longitude: Double, lanePoint: DriveThruLanePoint?) {
        fallback.simulateGps(stage, latitude, longitude, lanePoint)
    }

    override fun simulateBeacon(stage: DriveThruZoneStage, beaconId: String, lanePoint: DriveThruLanePoint?) = Unit

    override fun resetToOutside() {
        fallback.resetToOutside()
    }

    private fun handleLocation(location: Location) {
        val nearest = tenantLocationCatalog.entries
            .mapNotNull { entry ->
                val geofence = entry.geofence ?: return@mapNotNull null
                val distance = roughDistanceMeters(
                    latitudeA = geofence.latitude,
                    longitudeA = geofence.longitude,
                    latitudeB = location.latitude,
                    longitudeB = location.longitude,
                )
                entry to distance
            }
            .minByOrNull { (_, distance) -> distance }
            ?: run {
                fallback.resetToOutside()
                return
            }

        val geofence = nearest.first.geofence ?: return
        val distance = nearest.second
        val stage = when {
            distance <= geofence.radiusMeters * 0.45 -> DriveThruZoneStage.ORDERING_READY
            distance <= geofence.radiusMeters -> DriveThruZoneStage.IN_ZONE
            distance <= geofence.radiusMeters * 1.35 -> DriveThruZoneStage.APPROACHING
            else -> DriveThruZoneStage.OUTSIDE
        }

        if (stage == DriveThruZoneStage.OUTSIDE) {
            fallback.resetToOutside()
            return
        }

        val lanePoint = when (stage) {
            DriveThruZoneStage.APPROACHING -> DriveThruLanePoint.ENTRANCE
            DriveThruZoneStage.IN_ZONE -> DriveThruLanePoint.ENTRANCE
            DriveThruZoneStage.ORDERING_READY -> DriveThruLanePoint.MENU_BOARD
            else -> null
        }

        fallback.simulateGps(
            stage = stage,
            latitude = location.latitude,
            longitude = location.longitude,
            lanePoint = lanePoint,
        )
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun roughDistanceMeters(
        latitudeA: Double,
        longitudeA: Double,
        latitudeB: Double,
        longitudeB: Double,
    ): Double {
        return sqrt(
            ((latitudeA - latitudeB) * (latitudeA - latitudeB)) +
                ((longitudeA - longitudeB) * (longitudeA - longitudeB)),
        ) * 111_000
    }
}
