package com.hanzi.drivethru.data.entry

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.hanzi.drivethru.core.model.DriveThruLanePoint
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import com.hanzi.drivethru.core.model.EntryTriggerEvent

class BleBeaconEntryTriggerProvider(
    private val context: Context,
    private val tenantLocationCatalog: TenantLocationCatalog = TenantLocationCatalog(context),
) : EntryTriggerProvider {
    private val fallback = FakeEntryTriggerProvider()
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach(::handleScanResult)
        }
    }
    private var scanner: BluetoothLeScanner? = null
    private var started = false

    override val eventFlow = fallback.eventFlow

    override fun currentEvent(): EntryTriggerEvent = fallback.currentEvent()

    override fun start() {
        if (started || !hasBlePermission()) {
            return
        }
        val adapter = bluetoothManager?.adapter ?: return
        if (!adapter.isEnabled) {
            return
        }
        scanner = adapter.bluetoothLeScanner ?: return
        runCatching {
            scanner?.startScan(scanCallback)
            started = true
        }
    }

    override fun stop() {
        if (!started) {
            return
        }
        runCatching {
            scanner?.stopScan(scanCallback)
        }
        started = false
    }

    override fun simulateGps(stage: DriveThruZoneStage, latitude: Double, longitude: Double, lanePoint: DriveThruLanePoint?) = Unit

    override fun simulateBeacon(stage: DriveThruZoneStage, beaconId: String, lanePoint: DriveThruLanePoint?) {
        fallback.simulateBeacon(stage, beaconId, lanePoint)
    }

    override fun resetToOutside() {
        fallback.resetToOutside()
    }

    private fun handleScanResult(result: ScanResult) {
        val candidateIds = buildSet {
            result.device?.name?.takeIf(String::isNotBlank)?.let(::add)
            result.device?.address?.takeIf(String::isNotBlank)?.let(::add)
            result.scanRecord?.serviceUuids?.forEach { add(it.uuid.toString()) }
            val manufacturerSpecificData = result.scanRecord?.manufacturerSpecificData
            if (manufacturerSpecificData != null) {
                for (index in 0 until manufacturerSpecificData.size()) {
                    val key = manufacturerSpecificData.keyAt(index)
                    add("mfg:$key")
                }
            }
        }

        val matchedBeacon = tenantLocationCatalog.entries.firstOrNull { entry ->
            entry.beaconIds.any(candidateIds::contains)
        } ?: return

        fallback.simulateBeacon(
            stage = DriveThruZoneStage.ORDERING_READY,
            beaconId = matchedBeacon.beaconIds.first(),
            lanePoint = DriveThruLanePoint.MENU_BOARD,
        )
    }

    private fun hasBlePermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}
