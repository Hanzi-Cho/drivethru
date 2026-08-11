package com.hanzi.drivethru.feature.customui

import com.hanzi.drivethru.core.model.CustomUiDestination
import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.state.DriveThruSafetyPolicy
import com.hanzi.drivethru.core.state.OrderingSessionController
import com.hanzi.drivethru.core.state.StopStatePolicy
import com.hanzi.drivethru.data.menu.FakeMenuRepository
import com.hanzi.drivethru.data.store.FakeStoreResolver
import com.hanzi.drivethru.data.vehicle.FakeVehicleSignalProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomUiFlowCoordinatorTest {
    private fun createCoordinator(): CustomUiFlowCoordinator {
        val safetyPolicy = DriveThruSafetyPolicy()
        return CustomUiFlowCoordinator(
            menuRepository = FakeMenuRepository(),
            vehicleSignalProvider = FakeVehicleSignalProvider(),
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
        coordinator.enterDemoStore()

        assertEquals(CustomUiDestination.FULL_MENU, coordinator.getViewState().destination)
    }

    @Test
    fun `moving vehicle from full menu enters stop state and preserves draft`() {
        val coordinator = createCoordinator()

        coordinator.setGearState(GearState.PARK)
        coordinator.enterDemoStore()
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
        coordinator.enterDemoStore()
        coordinator.addMenuItem("double_beef_burger_set")
        coordinator.openCartReview()
        coordinator.setGearState(GearState.DRIVE)
        coordinator.setGearState(GearState.PARK)
        coordinator.resumeOrdering()

        assertEquals(CustomUiDestination.CART_REVIEW, coordinator.getViewState().destination)
    }
}
