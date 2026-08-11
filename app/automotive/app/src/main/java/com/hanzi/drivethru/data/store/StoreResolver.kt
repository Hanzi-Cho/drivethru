package com.hanzi.drivethru.data.store

import com.hanzi.drivethru.core.model.Store
import com.hanzi.drivethru.core.model.EntryTriggerEvent

interface StoreResolver {
    fun resolveStore(entryTriggerEvent: EntryTriggerEvent): Store?
}
