package com.hanzi.drivethru.data.entry

import com.hanzi.drivethru.core.model.DriveThruLanePoint
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import com.hanzi.drivethru.core.model.EntryTriggerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CompositeEntryTriggerProvider(
    private val providers: List<EntryTriggerProvider>,
) : EntryTriggerProvider {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutableEventFlow = MutableStateFlow(EntryTriggerEvent.outside())
    private val collectionJobs = mutableListOf<Job>()

    override val eventFlow: StateFlow<EntryTriggerEvent> = mutableEventFlow.asStateFlow()

    override fun currentEvent(): EntryTriggerEvent = mutableEventFlow.value

    override fun start() {
        if (collectionJobs.isNotEmpty()) {
            return
        }
        providers.forEach { provider ->
            provider.start()
            collectionJobs += scope.launch {
                provider.eventFlow.collectLatest { event ->
                    mutableEventFlow.value = event
                }
            }
        }
    }

    override fun stop() {
        collectionJobs.forEach(Job::cancel)
        collectionJobs.clear()
        providers.forEach(EntryTriggerProvider::stop)
    }

    override fun simulateGps(stage: DriveThruZoneStage, latitude: Double, longitude: Double, lanePoint: DriveThruLanePoint?) {
        providers.forEach { it.simulateGps(stage, latitude, longitude, lanePoint) }
    }

    override fun simulateBeacon(stage: DriveThruZoneStage, beaconId: String, lanePoint: DriveThruLanePoint?) {
        providers.forEach { it.simulateBeacon(stage, beaconId, lanePoint) }
    }

    override fun resetToOutside() {
        providers.forEach(EntryTriggerProvider::resetToOutside)
        mutableEventFlow.value = EntryTriggerEvent.outside()
    }
}
