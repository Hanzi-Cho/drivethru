package com.hanzi.drivethru.feature.status

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import com.hanzi.drivethru.R
import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.state.DriveThruStateStore
import com.hanzi.drivethru.feature.menu.FullMenuScreen
import com.hanzi.drivethru.feature.menu.SimplifiedMenuScreen

class WaitingForEntryScreen(
    carContext: CarContext,
    private val stateStore: DriveThruStateStore,
) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        return MessageTemplate.Builder(
            carContext.getString(R.string.waiting_message),
        )
            .setTitle(carContext.getString(R.string.waiting_title))
            .setDebugGearActionStrip()
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.enter_demo_store))
                    .setOnClickListener {
                        stateStore.enterDemoStore()
                        if (stateStore.shouldShowFullMenu()) {
                            screenManager.push(FullMenuScreen(carContext, stateStore))
                        } else {
                            screenManager.push(SimplifiedMenuScreen(carContext, stateStore))
                        }
                    }
                    .build(),
            )
            .setHeaderAction(Action.APP_ICON)
            .build()
    }

    private fun MessageTemplate.Builder.setDebugGearActionStrip(): MessageTemplate.Builder {
        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.debug_drive))
                    .setOnClickListener { stateStore.updateGearState(GearState.DRIVE) }
                    .build(),
            )
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.debug_park))
                    .setOnClickListener { stateStore.updateGearState(GearState.PARK) }
                    .build(),
            )
            .build()

        return setActionStrip(actionStrip)
    }
}
