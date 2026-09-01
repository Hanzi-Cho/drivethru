package com.hanzi.drivethru.data.menu

import android.content.Context
import com.hanzi.drivethru.data.menu.local.DriveThruDatabase
import com.hanzi.drivethru.data.tenant.TenantCatalogRepository

class MenuRepositorySelector(
    private val context: Context,
) {
    fun select(tenantCatalogRepository: TenantCatalogRepository): StoreScopedMenuRepository {
        return TenantMenuRepository(
            tenantCatalogRepository = tenantCatalogRepository,
            menuDao = DriveThruDatabase.getInstance(context).menuDao(),
            fallbackRepository = FakeMenuRepository(),
        )
    }
}
