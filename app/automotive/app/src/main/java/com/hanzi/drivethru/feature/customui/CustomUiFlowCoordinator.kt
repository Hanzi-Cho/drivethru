package com.hanzi.drivethru.feature.customui

import com.hanzi.drivethru.core.model.CustomUiDestination
import com.hanzi.drivethru.core.model.CustomUiViewState
import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.model.VehicleSignalSnapshot
import com.hanzi.drivethru.core.state.DriveThruSafetyPolicy
import com.hanzi.drivethru.core.state.OrderingSessionController
import com.hanzi.drivethru.core.state.StopStatePolicy
import com.hanzi.drivethru.data.menu.MenuRepository
import com.hanzi.drivethru.data.store.StoreResolver
import com.hanzi.drivethru.data.vehicle.VehicleSignalProvider

class CustomUiFlowCoordinator(
    private val menuRepository: MenuRepository,
    private val vehicleSignalProvider: VehicleSignalProvider,
    private val storeResolver: StoreResolver,
    private val orderingSessionController: OrderingSessionController,
    private val safetyPolicy: DriveThruSafetyPolicy,
    private val stopStatePolicy: StopStatePolicy,
) {
    private var lastSafeOrderingDestination: CustomUiDestination = CustomUiDestination.FULL_MENU

    private var viewState: CustomUiViewState = CustomUiViewState(
        destination = CustomUiDestination.STANDBY,
        activeStore = null,
        vehicleSignal = vehicleSignalProvider.getSnapshot(),
        orderDraft = null,
        stopStateReason = null,
        statusMessage = "Waiting for a drive-thru entry event.",
    )

    fun getViewState(): CustomUiViewState = viewState

    fun enterDemoStore() {
        val store = storeResolver.resolveStore() ?: return
        orderingSessionController.startSession(store)
        viewState = viewState.copy(
            destination = CustomUiDestination.STORE_READY,
            activeStore = store,
            orderDraft = orderingSessionController.getActiveSession()?.orderDraft,
            stopStateReason = null,
            statusMessage = "Store detected. Park the vehicle to open full ordering UI.",
        )
        syncVehicleSignal()
    }

    fun setGearState(gearState: GearState) {
        vehicleSignalProvider.updateGearState(gearState)
        syncVehicleSignal()
    }

    fun setVehicleSpeed(speedMetersPerSecond: Double) {
        vehicleSignalProvider.updateSpeed(speedMetersPerSecond)
        syncVehicleSignal()
    }

    fun openFullMenu() {
        val store = viewState.activeStore ?: return
        val snapshot = vehicleSignalProvider.getSnapshot()
        if (!safetyPolicy.canShowFullOrderingUi(snapshot)) {
            viewState = viewState.copy(
                destination = CustomUiDestination.STORE_READY,
                vehicleSignal = snapshot,
                statusMessage = "Full ordering is locked until the vehicle is in PARK.",
            )
            return
        }

        lastSafeOrderingDestination = CustomUiDestination.FULL_MENU
        viewState = viewState.copy(
            destination = CustomUiDestination.FULL_MENU,
            activeStore = store,
            vehicleSignal = snapshot,
            stopStateReason = null,
            orderDraft = orderingSessionController.getActiveSession()?.orderDraft,
            statusMessage = "Full ordering UI is active.",
        )
    }

    fun addMenuItem(menuItemId: String) {
        val menuItem = menuRepository.findMenuItemById(menuItemId) ?: return
        orderingSessionController.addMenuItem(menuItem)
        lastSafeOrderingDestination = CustomUiDestination.FULL_MENU
        viewState = viewState.copy(
            destination = CustomUiDestination.FULL_MENU,
            orderDraft = orderingSessionController.getActiveSession()?.orderDraft,
            statusMessage = "${menuItem.name} added to the draft.",
        )
    }

    fun openCartReview() {
        if (!orderingSessionController.hasActiveDraft()) {
            viewState = viewState.copy(
                statusMessage = "Add at least one menu item before reviewing the cart.",
            )
            return
        }

        lastSafeOrderingDestination = CustomUiDestination.CART_REVIEW
        viewState = viewState.copy(
            destination = CustomUiDestination.CART_REVIEW,
            orderDraft = orderingSessionController.getActiveSession()?.orderDraft,
            statusMessage = "Review the current draft before proceeding.",
        )
    }

    fun resumeOrdering() {
        val snapshot = vehicleSignalProvider.getSnapshot()
        if (!safetyPolicy.canShowFullOrderingUi(snapshot)) {
            viewState = viewState.copy(
                destination = CustomUiDestination.STOP_STATE,
                vehicleSignal = snapshot,
                statusMessage = "The vehicle is still not safe to resume ordering.",
            )
            return
        }

        viewState = viewState.copy(
            destination = lastSafeOrderingDestination,
            vehicleSignal = snapshot,
            stopStateReason = null,
            orderDraft = orderingSessionController.getActiveSession()?.orderDraft,
            statusMessage = "Vehicle returned to a safe state. Ordering resumed.",
        )
    }

    fun closeSession() {
        orderingSessionController.clearSession()
        viewState = viewState.copy(
            destination = CustomUiDestination.STANDBY,
            activeStore = null,
            orderDraft = null,
            stopStateReason = null,
            statusMessage = "Session closed. Waiting for the next entry event.",
        )
    }

    private fun syncVehicleSignal() {
        val snapshot = vehicleSignalProvider.getSnapshot()
        val stopStateReason = stopStatePolicy.evaluateTransition(viewState.destination, snapshot)

        viewState = if (stopStateReason != null) {
            viewState.copy(
                destination = CustomUiDestination.STOP_STATE,
                vehicleSignal = snapshot,
                orderDraft = orderingSessionController.getActiveSession()?.orderDraft,
                stopStateReason = stopStateReason,
                statusMessage = "Vehicle safety guard engaged. Resume only after returning to PARK.",
            )
        } else {
            val nextDestination = when {
                viewState.destination == CustomUiDestination.STORE_READY &&
                    safetyPolicy.canShowFullOrderingUi(snapshot) -> CustomUiDestination.FULL_MENU
                else -> viewState.destination
            }
            viewState.copy(
                destination = nextDestination,
                vehicleSignal = snapshot,
                orderDraft = orderingSessionController.getActiveSession()?.orderDraft,
                stopStateReason = null,
                statusMessage = when (nextDestination) {
                    CustomUiDestination.FULL_MENU ->
                        "Safe state confirmed. Full ordering UI is available."
                    else -> viewState.statusMessage
                },
            )
        }
    }
}
