package com.hanzi.drivethru.data.store

import com.hanzi.drivethru.core.model.EntryTriggerEvent
import com.hanzi.drivethru.core.model.Store
import com.hanzi.drivethru.data.tenant.TenantCatalogRepository

class TenantStoreResolver(
    private val tenantCatalogRepository: TenantCatalogRepository,
) : StoreResolver {
    override fun resolveStore(entryTriggerEvent: EntryTriggerEvent): Store? {
        return tenantCatalogRepository.resolveStore(entryTriggerEvent)
    }
}
