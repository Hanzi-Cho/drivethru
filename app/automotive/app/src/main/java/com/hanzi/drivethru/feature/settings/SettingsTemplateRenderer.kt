package com.hanzi.drivethru.feature.settings

import androidx.car.app.CarContext
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.hanzi.drivethru.core.state.DriveThruStateStore
import com.hanzi.drivethru.feature.common.DriveThruTemplateSupport

class SettingsTemplateRenderer(
    private val carContext: CarContext,
    private val stateStore: DriveThruStateStore,
) {
    fun render(): Template {
        val settingsList = ItemList.Builder()
            .addItem(DriveThruTemplateSupport.buildStatusRow(stateStore.getGlobalStatus()))
        stateStore.getSettings().forEach { item ->
            settingsList.addItem(
                Row.Builder()
                    .setTitle(item.title)
                    .addText(item.subtitle)
                    .build(),
            )
        }

        return ListTemplate.Builder()
            .setSingleList(settingsList.build())
            .build()
    }
}
