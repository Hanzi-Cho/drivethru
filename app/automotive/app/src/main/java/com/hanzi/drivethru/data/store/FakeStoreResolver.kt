package com.hanzi.drivethru.data.store

import com.hanzi.drivethru.core.model.Store
import com.hanzi.drivethru.core.model.StoreCapability

class FakeStoreResolver : StoreResolver {
    override fun resolveStore(entryToken: String): Store? {
        if (entryToken != "demo-store") {
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
