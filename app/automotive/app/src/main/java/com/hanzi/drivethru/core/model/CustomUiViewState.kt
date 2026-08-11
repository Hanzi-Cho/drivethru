package com.hanzi.drivethru.core.model

data class CustomUiViewState(
    val destination: CustomUiDestination,
    val activeStore: Store?,
    val vehicleSignal: VehicleSignalSnapshot,
    val orderDraft: OrderDraft?,
    val stopStateReason: StopStateReason?,
    val statusMessage: String,
) {
    val canResumeSession: Boolean
        get() = orderDraft != null && orderDraft.items.isNotEmpty()
}
