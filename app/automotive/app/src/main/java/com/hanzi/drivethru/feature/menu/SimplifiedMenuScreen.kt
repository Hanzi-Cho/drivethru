package com.hanzi.drivethru.feature.menu

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.SectionedItemList
import androidx.car.app.model.Template
import androidx.car.app.model.ListTemplate
import com.hanzi.drivethru.R
import com.hanzi.drivethru.core.model.DriveThruState
import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.state.DriveThruStateStore
import androidx.car.app.model.Row
import com.hanzi.drivethru.feature.order.OrderReviewScreen

class SimplifiedMenuScreen(
    carContext: CarContext,
    private val stateStore: DriveThruStateStore,
) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val state = stateStore.currentState as? DriveThruState.SimplifiedMenu
            ?: DriveThruState.SimplifiedMenu(
                storeName = carContext.getString(R.string.default_store_name),
                gearState = stateStore.currentGearState,
            )

        val quickOrderList = ItemList.Builder().apply {
            stateStore.getQuickOrderMenu(state.storeName).forEach { menuItem ->
                addItem(
                    Row.Builder()
                        .setTitle(menuItem.name)
                        .addText(
                            carContext.getString(
                                R.string.menu_item_price_description,
                                menuItem.price.toString(),
                                menuItem.description,
                            ),
                        )
                        .setOnClickListener {
                            if (stateStore.selectMenuItem(state.storeName, menuItem.id)) {
                                screenManager.push(OrderReviewScreen(carContext, stateStore))
                            }
                        }
                        .build(),
                )
            }
        }.build()

        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.debug_drive))
                    .setOnClickListener {
                        stateStore.updateGearState(GearState.DRIVE)
                        invalidate()
                    }
                    .build(),
            )
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.debug_park))
                    .setOnClickListener {
                        stateStore.updateGearState(GearState.PARK)
                        screenManager.push(FullMenuScreen(carContext, stateStore))
                    }
                    .build(),
            )
            .build()

        return ListTemplate.Builder()
            .setTitle(
                carContext.getString(
                    R.string.simplified_menu_title,
                    state.storeName,
                ),
            )
            .setHeaderAction(Action.BACK)
            .setActionStrip(actionStrip)
            .addSectionedList(
                SectionedItemList.create(
                    quickOrderList,
                    carContext.getString(R.string.quick_order_section_title),
                ),
            )
            .addSectionedList(
                SectionedItemList.create(
                    ItemList.Builder()
                        .addItem(
                            Row.Builder()
                                .setTitle(
                                    carContext.getString(
                                        R.string.debug_current_gear,
                                        state.gearState.name,
                                    ),
                                )
                                .addText(carContext.getString(R.string.simplified_menu_gear_hint))
                                .build(),
                        )
                        .build(),
                    carContext.getString(R.string.debug_section_title),
                ),
            )
            .build()
    }
}
