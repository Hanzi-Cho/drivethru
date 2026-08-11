package com.hanzi.drivethru.data.menu

interface StoreScopedMenuRepository : MenuRepository {
    fun activateStore(storeId: String)
    fun getActiveStoreId(): String?
    fun getSyncStatus(): String
}
