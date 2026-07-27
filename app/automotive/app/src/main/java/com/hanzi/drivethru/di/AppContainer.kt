package com.hanzi.drivethru.di

import com.hanzi.drivethru.core.state.DriveThruStateStore
import com.hanzi.drivethru.data.menu.FakeMenuRepository
import com.hanzi.drivethru.data.payment.FakePaymentMethodRepository
import com.hanzi.drivethru.data.settings.FakeSettingsRepository
import com.hanzi.drivethru.data.status.FakeStatusRepository

class AppContainer {
    private val menuRepository = FakeMenuRepository()
    private val paymentMethodRepository = FakePaymentMethodRepository()
    private val settingsRepository = FakeSettingsRepository()
    private val statusRepository = FakeStatusRepository()

    val stateStore: DriveThruStateStore = DriveThruStateStore(
        menuRepository = menuRepository,
        paymentMethodRepository = paymentMethodRepository,
        settingsRepository = settingsRepository,
        statusRepository = statusRepository,
    )
}
