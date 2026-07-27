package com.hanzi.drivethru.feature.common

import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.Row
import com.hanzi.drivethru.core.model.GlobalStatus

internal object DriveThruTemplateSupport {
    fun formatPrice(price: Int): String = "%,d KRW".format(price)

    fun buildStatusRow(status: GlobalStatus): Row {
        return Row.Builder()
            .setTitle("Drive-Thru Service")
            .addText(status.cautionLabel)
            .addText("${status.dataLevel} · ${status.wifiLevel} · ${status.batteryLevel} · ${status.timeLabel}")
            .build()
    }

    fun buildHeaderActions(status: GlobalStatus): List<Action> {
        return listOf(
            Action.Builder()
                .setTitle(status.cautionLabel)
                .setBackgroundColor(CarColor.RED)
                .setOnClickListener {}
                .build(),
            Action.Builder()
                .setTitle(status.timeLabel)
                .setOnClickListener {}
                .build(),
        )
    }
}
