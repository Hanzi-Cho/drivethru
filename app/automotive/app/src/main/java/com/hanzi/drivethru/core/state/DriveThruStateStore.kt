package com.hanzi.drivethru.core.state

import com.hanzi.drivethru.core.model.DriveThruState
import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.model.OrderDraft
import com.hanzi.drivethru.core.model.OrderLineItem
import com.hanzi.drivethru.data.menu.MenuRepository
import com.hanzi.drivethru.data.vehicle.GearStateDataSource

class DriveThruStateStore(
    private val gearStateDataSource: GearStateDataSource,
    private val menuRepository: MenuRepository,
) {
    private val demoStoreName = "Demo Drive-Thru"

    var currentState: DriveThruState = DriveThruState.WaitingForEntry
        private set

    val currentGearState: GearState
        get() = gearStateDataSource.getCurrentGearState()

    fun enterDemoStore() {
        currentState = createMenuState(currentGearState)
    }

    fun updateGearState(gearState: GearState) {
        gearStateDataSource.updateGearState(gearState)
        currentState = when (val state = currentState) {
            DriveThruState.WaitingForEntry -> DriveThruState.WaitingForEntry
            is DriveThruState.SimplifiedMenu -> createMenuState(gearState, state.storeName)
            is DriveThruState.FullMenu -> createMenuState(gearState, state.storeName)
            is DriveThruState.ReviewingOrder -> state
        }
    }

    fun shouldShowFullMenu(): Boolean = currentGearState == GearState.PARK

    fun getQuickOrderMenu(storeName: String): List<com.hanzi.drivethru.core.model.MenuItem> {
        return menuRepository.getQuickOrderMenu(storeName)
    }

    fun getFullMenu(storeName: String): List<com.hanzi.drivethru.core.model.MenuItem> {
        return menuRepository.getFullMenu(storeName)
    }

    fun selectMenuItem(storeName: String, itemId: String): Boolean {
        val menuItem = menuRepository.findMenuItemById(storeName, itemId) ?: return false
        currentState = DriveThruState.ReviewingOrder(
            orderDraft = OrderDraft(
                storeName = storeName,
                items = listOf(
                    OrderLineItem(
                        menuItem = menuItem,
                        quantity = 1,
                    ),
                ),
            ),
        )
        return true
    }

    private fun createMenuState(
        gearState: GearState,
        storeName: String = demoStoreName,
    ): DriveThruState {
        return if (gearState == GearState.PARK) {
            DriveThruState.FullMenu(
                storeName = storeName,
                gearState = gearState,
            )
        } else {
            DriveThruState.SimplifiedMenu(
                storeName = storeName,
                gearState = gearState,
            )
        }
    }
}
