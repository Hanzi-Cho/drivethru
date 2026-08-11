package com.hanzi.drivethru.di

import android.content.Context
import com.hanzi.drivethru.core.state.DriveThruStateStore
import com.hanzi.drivethru.core.state.DriveThruSafetyPolicy
import com.hanzi.drivethru.core.state.OrderingSessionController
import com.hanzi.drivethru.core.state.StopStatePolicy
import com.hanzi.drivethru.data.entry.FakeEntryTriggerProvider
import com.hanzi.drivethru.data.menu.FakeMenuRepository
import com.hanzi.drivethru.data.menu.MenuRepositorySelector
import com.hanzi.drivethru.data.payment.FakePaymentMethodRepository
import com.hanzi.drivethru.data.settings.FakeSettingsRepository
import com.hanzi.drivethru.data.status.FakeStatusRepository
import com.hanzi.drivethru.data.store.FakeStoreResolver
import com.hanzi.drivethru.data.vehicle.CarDataVehicleSignalProvider
import com.hanzi.drivethru.data.vehicle.CarPropertyManagerCarDataFacade
import com.hanzi.drivethru.data.vehicle.FakeCarDataFacade
import com.hanzi.drivethru.data.vehicle.SafeCarDataFacade
import com.hanzi.drivethru.feature.customui.CustomUiFlowCoordinator

class AppContainer(context: Context) {
    private val menuRepository = MenuRepositorySelector(context).select()
    private val paymentMethodRepository = FakePaymentMethodRepository()
    private val settingsRepository = FakeSettingsRepository()
    private val statusRepository = FakeStatusRepository()
    private val fakeCarDataFacade = FakeCarDataFacade()
    private val carDataFacade = SafeCarDataFacade(
        primary = CarPropertyManagerCarDataFacade(context),
        fallback = fakeCarDataFacade,
    )
    private val vehicleSignalProvider = CarDataVehicleSignalProvider(
        carDataFacade = carDataFacade,
        fakeCarDataFacade = fakeCarDataFacade,
    )
    private val entryTriggerProvider = FakeEntryTriggerProvider()
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
        entryTriggerProvider = entryTriggerProvider,
        storeResolver = storeResolver,
        orderingSessionController = orderingSessionController,
        safetyPolicy = safetyPolicy,
        stopStatePolicy = stopStatePolicy,
    )
}
