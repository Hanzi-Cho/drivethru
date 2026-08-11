package com.hanzi.drivethru.data.store

import com.hanzi.drivethru.core.model.Store

interface StoreResolver {
    fun resolveStore(entryToken: String = "demo-store"): Store?
}
