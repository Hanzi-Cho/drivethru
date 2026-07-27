package com.hanzi.drivethru.feature.order

import androidx.car.app.CarContext
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.hanzi.drivethru.R
import com.hanzi.drivethru.core.model.AppDestination
import com.hanzi.drivethru.core.model.OrderReceipt
import com.hanzi.drivethru.core.state.DriveThruStateStore
import com.hanzi.drivethru.feature.common.DriveThruTemplateSupport

class OrderTemplateRenderer(
    private val carContext: CarContext,
    private val stateStore: DriveThruStateStore,
    private val onStateChanged: () -> Unit,
) {
    fun render(): Template {
        val receipt = stateStore.getLatestOrderReceipt()
            ?: return MessageTemplate.Builder(carContext.getString(R.string.order_empty_message))
                .addAction(
                    Action.Builder()
                        .setTitle(carContext.getString(R.string.order_go_to_cart))
                        .setOnClickListener {
                            stateStore.selectDestination(AppDestination.CART)
                            onStateChanged()
                        }
                        .build(),
                )
                .build()

        val pane = Pane.Builder()
            .addRow(DriveThruTemplateSupport.buildStatusRow(stateStore.getGlobalStatus()))
            .addRow(
                Row.Builder()
                    .setTitle(carContext.getString(R.string.order_complete_title))
                    .addText(receipt.pickupMessage)
                    .build(),
            )
            .addRow(buildPaymentRow(receipt))
            .apply {
                receipt.items.forEach { item ->
                    addRow(
                        Row.Builder()
                            .setTitle("${item.menuItem.name} x${item.quantity}")
                            .addText(DriveThruTemplateSupport.formatPrice(item.totalPrice))
                            .build(),
                    )
                }
            }
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.order_back_to_menu))
                    .setOnClickListener {
                        stateStore.selectDestination(AppDestination.MENU)
                        onStateChanged()
                    }
                    .build(),
            )
            .build()

        return PaneTemplate.Builder(pane).build()
    }

    private fun buildPaymentRow(receipt: OrderReceipt): Row {
        return Row.Builder()
            .setTitle(
                carContext.getString(
                    R.string.order_payment_title,
                    DriveThruTemplateSupport.formatPrice(receipt.totalAmount),
                ),
            )
            .addText("${receipt.paymentMethod.displayName} · ${receipt.paymentMethod.maskedNumber}")
            .addText(receipt.orderId)
            .build()
    }
}
