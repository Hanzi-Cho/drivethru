package com.hanzi.drivethru.app

import androidx.car.app.Screen
import androidx.car.app.Session
import com.hanzi.drivethru.core.state.DriveThruStateStore
import com.hanzi.drivethru.feature.root.DriveThruTabRootScreen

class DriveThruSession(
    private val stateStore: DriveThruStateStore,
) : Session() {
    override fun onCreateScreen(intent: android.content.Intent): Screen {
        return DriveThruTabRootScreen(carContext, stateStore)
    }
}
