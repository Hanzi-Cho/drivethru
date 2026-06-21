package com.hanzi.drivethru.feature.order

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.SectionedItemList
import androidx.car.app.model.Template
import com.hanzi.drivethru.R
import com.hanzi.drivethru.core.model.DriveThruState
import com.hanzi.drivethru.core.state.DriveThruStateStore

class OrderReviewScreen(
    carContext: CarContext,
    private val stateStore: DriveThruStateStore,
) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val state = stateStore.currentState as? DriveThruState.ReviewingOrder
            ?: return ListTemplate.Builder()
                .setTitle(carContext.getString(R.string.order_review_missing_title))
                .setHeaderAction(Action.BACK)
                .build()

        val orderRows = ItemList.Builder().apply {
            state.orderDraft.items.forEach { lineItem ->
                addItem(
                    Row.Builder()
                        .setTitle(
                            carContext.getString(
                                R.string.order_review_item_title,
                                lineItem.menuItem.name,
                                lineItem.quantity.toString(),
                            ),
                        )
                        .addText(
                            carContext.getString(
                                R.string.order_review_item_price,
                                lineItem.menuItem.price.toString(),
                            ),
                        )
                        .build(),
                )
            }
        }.build()

        val summaryRows = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle(
                        carContext.getString(
                            R.string.order_review_total,
                            state.orderDraft.totalPrice.toString(),
                        ),
                    )
                    .addText(carContext.getString(R.string.order_review_status))
                    .build(),
            )
            .build()

        return ListTemplate.Builder()
            .setTitle(
                carContext.getString(
                    R.string.order_review_title,
                    state.orderDraft.storeName,
                ),
            )
            .setHeaderAction(Action.BACK)
            .addSectionedList(
                SectionedItemList.create(
                    orderRows,
                    carContext.getString(R.string.order_review_items_section),
                ),
            )
            .addSectionedList(
                SectionedItemList.create(
                    summaryRows,
                    carContext.getString(R.string.order_review_summary_section),
                ),
            )
            .build()
    }
}
