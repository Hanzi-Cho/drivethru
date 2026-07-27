package com.hanzi.drivethru.feature.cart

import androidx.car.app.CarContext
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.hanzi.drivethru.R
import com.hanzi.drivethru.core.model.AppDestination
import com.hanzi.drivethru.core.model.CartLineItem
import com.hanzi.drivethru.core.state.DriveThruStateStore
import com.hanzi.drivethru.feature.common.DriveThruTemplateSupport

class CartTemplateRenderer(
    private val carContext: CarContext,
    private val stateStore: DriveThruStateStore,
    private val onStateChanged: () -> Unit,
) {
    fun render(): Template {
        val pane = Pane.Builder()
            .addRow(DriveThruTemplateSupport.buildStatusRow(stateStore.getGlobalStatus()))
            .apply {
                stateStore.getCartItems().forEach { item ->
                    addRow(buildCartRow(item))
                }
                addRow(buildSummaryRow())
            }
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.cart_pay_now))
                    .setBackgroundColor(CarColor.YELLOW)
                    .setOnClickListener {
                        stateStore.submitOrder()
                        onStateChanged()
                    }
                    .build(),
            )
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.cart_back_to_menu))
                    .setOnClickListener {
                        stateStore.selectDestination(AppDestination.MENU)
                        onStateChanged()
                    }
                    .build(),
            )
            .build()

        return PaneTemplate.Builder(pane).build()
    }

    private fun buildCartRow(item: CartLineItem): Row {
        return Row.Builder()
            .setTitle("${item.menuItem.name} x${item.quantity}")
            .addText(item.selectedOptions.joinToString(" · "))
            .addText(DriveThruTemplateSupport.formatPrice(item.totalPrice))
            .setOnClickListener {
                stateStore.decrementCartItem(item.menuItem.id)
                onStateChanged()
            }
            .build()
    }

    private fun buildSummaryRow(): Row {
        val summary = stateStore.getCartSummary()
        val payment = stateStore.getDefaultPaymentMethod()
        return Row.Builder()
            .setTitle(
                carContext.getString(
                    R.string.cart_total_row,
                    DriveThruTemplateSupport.formatPrice(summary.total),
                ),
            )
            .addText(
                carContext.getString(
                    R.string.cart_summary_details,
                    DriveThruTemplateSupport.formatPrice(summary.subtotal),
                    DriveThruTemplateSupport.formatPrice(summary.discount),
                ),
            )
            .addText("${payment.displayName} · ${payment.maskedNumber} · ${payment.autoPayLabel}")
            .build()
    }
}
