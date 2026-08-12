package com.hanzi.drivethru.feature.customui

import com.hanzi.drivethru.core.model.CustomUiDestination
import com.hanzi.drivethru.core.model.DriveThruLanePoint
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.state.DriveThruSafetyPolicy
import com.hanzi.drivethru.core.state.OrderingSessionController
import com.hanzi.drivethru.core.state.StopStatePolicy
import com.hanzi.drivethru.data.entry.FakeEntryTriggerProvider
import com.hanzi.drivethru.data.menu.FakeMenuRepository
import com.hanzi.drivethru.data.store.FakeStoreResolver
import com.hanzi.drivethru.data.vehicle.FakeVehicleSignalProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomUiFlowCoordinatorTest {
    private fun createCoordinator(): CustomUiFlowCoordinator {
        val safetyPolicy = DriveThruSafetyPolicy()
        return CustomUiFlowCoordinator(
            menuRepository = FakeMenuRepository(),
            vehicleSignalProvider = FakeVehicleSignalProvider(),
            entryTriggerProvider = FakeEntryTriggerProvider(),
            storeResolver = FakeStoreResolver(),
            orderingSessionController = OrderingSessionController(),
            safetyPolicy = safetyPolicy,
            stopStatePolicy = StopStatePolicy(safetyPolicy),
        )
    }

    @Test
    fun `entering store in park moves to full menu`() {
        val coordinator = createCoordinator()

        coordinator.setGearState(GearState.PARK)
        coordinator.simulateGpsTrigger(DriveThruZoneStage.ORDERING_READY, DriveThruLanePoint.MENU_BOARD)

        assertEquals(CustomUiDestination.FULL_MENU, coordinator.getViewState().destination)
    }

    @Test
    fun `moving vehicle from full menu enters stop state and preserves draft`() {
        val coordinator = createCoordinator()

        coordinator.setGearState(GearState.PARK)
        coordinator.simulateGpsTrigger(DriveThruZoneStage.ORDERING_READY, DriveThruLanePoint.MENU_BOARD)
        coordinator.addMenuItem("double_beef_burger_set")
        coordinator.setGearState(GearState.DRIVE)

        val state = coordinator.getViewState()
        assertEquals(CustomUiDestination.STOP_STATE, state.destination)
        assertTrue(state.orderDraft?.items?.isNotEmpty() == true)
    }

    @Test
    fun `resume ordering requires safe state and restores previous ordering screen`() {
        val coordinator = createCoordinator()

        coordinator.setGearState(GearState.PARK)
        coordinator.simulateGpsTrigger(DriveThruZoneStage.ORDERING_READY, DriveThruLanePoint.MENU_BOARD)
        coordinator.addMenuItem("double_beef_burger_set")
        coordinator.openCartReview()
        coordinator.setGearState(GearState.DRIVE)
        coordinator.setGearState(GearState.PARK)
        coordinator.resumeOrdering()

        assertEquals(CustomUiDestination.CART_REVIEW, coordinator.getViewState().destination)
    }

    @Test
    fun `exit trigger closes session and returns to standby`() {
        val coordinator = createCoordinator()

        coordinator.setGearState(GearState.PARK)
        coordinator.simulateBeaconTrigger(DriveThruZoneStage.ORDERING_READY, DriveThruLanePoint.MENU_BOARD)
        coordinator.resetEntryTrigger()

        assertEquals(CustomUiDestination.STANDBY, coordinator.getViewState().destination)
    }

    @Test
    fun `high speed abort closes session and resets store context`() {
        val coordinator = createCoordinator()

        coordinator.setGearState(GearState.PARK)
        coordinator.simulateGpsTrigger(DriveThruZoneStage.ORDERING_READY, DriveThruLanePoint.MENU_BOARD)
        coordinator.addMenuItem("double_beef_burger_set")
        coordinator.setVehicleSpeed(8.2)

        val state = coordinator.getViewState()
        assertEquals(CustomUiDestination.STANDBY, state.destination)
        assertNull(state.activeStore)
        assertTrue(state.statusMessage.contains("speed threshold"))
    }
}
