package com.hanzi.drivethru.data.store

import com.hanzi.drivethru.core.model.Store
import com.hanzi.drivethru.core.model.StoreCapability
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import com.hanzi.drivethru.core.model.EntryTriggerEvent

class FakeStoreResolver : StoreResolver {
    override fun resolveStore(entryTriggerEvent: EntryTriggerEvent): Store? {
        if (entryTriggerEvent.stage == DriveThruZoneStage.OUTSIDE || entryTriggerEvent.stage == DriveThruZoneStage.EXIT) {
            return null
        }

        return Store(
            id = "store_demo_001",
            name = "Hanzi DriveThru Demo Store",
            capabilities = setOf(StoreCapability.FULL_ORDERING, StoreCapability.QUICK_ORDER),
            menuSource = "fake-menu-repository",
        )
    }
}
