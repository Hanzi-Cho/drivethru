package com.hanzi.drivethru.core.model

sealed interface DriveThruState {
    data object WaitingForEntry : DriveThruState
    data class SimplifiedMenu(
        val storeName: String,
        val gearState: GearState,
    ) : DriveThruState
    data class FullMenu(
        val storeName: String,
        val gearState: GearState,
    ) : DriveThruState
    data class ReviewingOrder(
        val orderDraft: OrderDraft,
    ) : DriveThruState
}
