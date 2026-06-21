package com.hanzi.drivethru.data.vehicle

import com.hanzi.drivethru.core.model.GearState

interface GearStateDataSource {
    fun getCurrentGearState(): GearState
    fun updateGearState(gearState: GearState)
}
