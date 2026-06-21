package com.hanzi.drivethru.feature.menu

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.SectionedItemList
import androidx.car.app.model.Template
import com.hanzi.drivethru.R
import com.hanzi.drivethru.core.model.DriveThruState
import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.state.DriveThruStateStore
import com.hanzi.drivethru.feature.order.OrderReviewScreen

class FullMenuScreen(
    carContext: CarContext,
    private val stateStore: DriveThruStateStore,
) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val state = stateStore.currentState as? DriveThruState.FullMenu
            ?: DriveThruState.FullMenu(
                storeName = carContext.getString(R.string.default_store_name),
                gearState = stateStore.currentGearState,
            )

        val burgerList = ItemList.Builder()
        val drinkList = ItemList.Builder()
        stateStore.getFullMenu(state.storeName).forEach { menuItem ->
            val row = Row.Builder()
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
                .build()

            if (menuItem.category == "burger") {
                burgerList.addItem(row)
            } else {
                drinkList.addItem(row)
            }
        }

        val debugList = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle(
                        carContext.getString(
                            R.string.debug_current_gear,
                            state.gearState.name,
                        ),
                    )
                    .addText(carContext.getString(R.string.full_menu_gear_hint))
                    .build(),
            )
            .build()

        return ListTemplate.Builder()
            .setTitle(
                carContext.getString(
                    R.string.full_menu_title,
                    state.storeName,
                ),
            )
            .setHeaderAction(Action.BACK)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle(carContext.getString(R.string.debug_drive))
                            .setOnClickListener {
                                stateStore.updateGearState(GearState.DRIVE)
                                screenManager.push(SimplifiedMenuScreen(carContext, stateStore))
                            }
                            .build(),
                    )
                    .addAction(
                        Action.Builder()
                            .setTitle(carContext.getString(R.string.debug_park))
                            .setOnClickListener {
                                stateStore.updateGearState(GearState.PARK)
                                invalidate()
                            }
                            .build(),
                    )
                    .build(),
            )
            .addSectionedList(
                SectionedItemList.create(
                    burgerList.build(),
                    carContext.getString(R.string.full_menu_burger_section),
                ),
            )
            .addSectionedList(
                SectionedItemList.create(
                    drinkList.build(),
                    carContext.getString(R.string.full_menu_drink_section),
                ),
            )
            .addSectionedList(
                SectionedItemList.create(
                    debugList,
                    carContext.getString(R.string.debug_section_title),
                ),
            )
            .build()
    }
}
