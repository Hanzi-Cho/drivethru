package com.hanzi.drivethru.data.tenant

import com.hanzi.drivethru.core.model.MenuItem
import com.hanzi.drivethru.core.model.StoreCapability

data class TenantGeoFence(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double,
)

data class TenantStoreConfig(
    val brandId: String,
    val storeId: String,
    val displayName: String,
    val capabilities: Set<StoreCapability>,
    val menuSourceMode: String,
    val localMenuAssetPath: String?,
    val localOrderOptionsAssetPath: String?,
    val remoteMenuUrl: String?,
    val geofence: TenantGeoFence?,
    val beaconIds: List<String>,
    val tenantPath: String,
)

data class TenantResolverEntry(
    val brandId: String,
    val storeId: String,
    val displayName: String,
    val tenantPath: String,
    val geofence: TenantGeoFence?,
    val beaconIds: List<String>,
)

data class TenantMenuCatalog(
    val storeId: String,
    val items: List<MenuItem>,
)
