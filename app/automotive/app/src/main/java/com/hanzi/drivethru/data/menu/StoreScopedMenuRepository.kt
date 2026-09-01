package com.hanzi.drivethru.data.menu

import com.hanzi.drivethru.core.model.MenuItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface StoreScopedMenuRepository : MenuRepository {
    fun activateStore(storeId: String)
    fun getActiveStoreId(): String?
    fun getSyncStatus(): String

    /**
     * Live view of [storeId]'s menu for screens that want to react to background updates
     * instead of polling [getAllMenuItems]. Repositories without a persistent, observable
     * source of truth can fall back to a single-shot emission of the current snapshot.
     */
    fun observeMenuItems(storeId: String): Flow<List<MenuItem>> = flowOf(getAllMenuItems())
}
