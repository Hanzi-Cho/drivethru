package com.hanzi.drivethru.di

import com.hanzi.drivethru.core.state.DriveThruStateStore
import com.hanzi.drivethru.core.state.DriveThruSafetyPolicy
import com.hanzi.drivethru.core.state.OrderingSessionController
import com.hanzi.drivethru.core.state.StopStatePolicy
import com.hanzi.drivethru.data.menu.FakeMenuRepository
import com.hanzi.drivethru.data.payment.FakePaymentMethodRepository
import com.hanzi.drivethru.data.settings.FakeSettingsRepository
import com.hanzi.drivethru.data.status.FakeStatusRepository
import com.hanzi.drivethru.data.store.FakeStoreResolver
import com.hanzi.drivethru.data.vehicle.FakeVehicleSignalProvider
import com.hanzi.drivethru.feature.customui.CustomUiFlowCoordinator

class AppContainer {
    private val menuRepository = FakeMenuRepository()
    private val paymentMethodRepository = FakePaymentMethodRepository()
    private val settingsRepository = FakeSettingsRepository()
    private val statusRepository = FakeStatusRepository()
    private val vehicleSignalProvider = FakeVehicleSignalProvider()
    private val storeResolver = FakeStoreResolver()
    private val safetyPolicy = DriveThruSafetyPolicy()
    private val stopStatePolicy = StopStatePolicy(safetyPolicy)
    private val orderingSessionController = OrderingSessionController()

    val stateStore: DriveThruStateStore = DriveThruStateStore(
        menuRepository = menuRepository,
        paymentMethodRepository = paymentMethodRepository,
        settingsRepository = settingsRepository,
        statusRepository = statusRepository,
    )

    val customUiFlowCoordinator: CustomUiFlowCoordinator = CustomUiFlowCoordinator(
        menuRepository = menuRepository,
        vehicleSignalProvider = vehicleSignalProvider,
        storeResolver = storeResolver,
        orderingSessionController = orderingSessionController,
        safetyPolicy = safetyPolicy,
        stopStatePolicy = stopStatePolicy,
    )
}
