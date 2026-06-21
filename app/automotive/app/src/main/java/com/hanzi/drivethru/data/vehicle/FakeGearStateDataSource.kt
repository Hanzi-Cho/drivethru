package com.hanzi.drivethru.data.vehicle

import com.hanzi.drivethru.core.model.GearState

class FakeGearStateDataSource(
    initialState: GearState = GearState.DRIVE,
) : GearStateDataSource {
    private var currentGearState: GearState = initialState

    override fun getCurrentGearState(): GearState = currentGearState

    override fun updateGearState(gearState: GearState) {
        currentGearState = gearState
    }
}
