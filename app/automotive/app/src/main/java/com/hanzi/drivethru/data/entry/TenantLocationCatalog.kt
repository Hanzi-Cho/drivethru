package com.hanzi.drivethru.data.entry

import android.content.Context
import com.hanzi.drivethru.data.tenant.TenantGeoFence
import org.json.JSONObject

data class TenantLocationEntry(
    val storeId: String,
    val displayName: String,
    val geofence: TenantGeoFence?,
    val beaconIds: Set<String>,
)

class TenantLocationCatalog(context: Context) {
    val entries: List<TenantLocationEntry> = readEntries(context)

    private fun readEntries(context: Context): List<TenantLocationEntry> {
        val json = context.assets.open("tenants/resolver-map.json").bufferedReader().use { it.readText() }
        val stores = JSONObject(json).getJSONArray("stores")
        return buildList {
            for (index in 0 until stores.length()) {
                val item = stores.getJSONObject(index)
                val geofence = item.optJSONObject("geofence")?.let {
                    TenantGeoFence(
                        latitude = it.getDouble("latitude"),
                        longitude = it.getDouble("longitude"),
                        radiusMeters = it.getDouble("radiusMeters"),
                    )
                }
                val beaconIds = buildSet {
                    val array = item.optJSONArray("beaconIds")
                    if (array != null) {
                        for (beaconIndex in 0 until array.length()) {
                            add(array.getString(beaconIndex))
                        }
                    }
                }
                add(
                    TenantLocationEntry(
                        storeId = item.getString("storeId"),
                        displayName = item.getString("displayName"),
                        geofence = geofence,
                        beaconIds = beaconIds,
                    ),
                )
            }
        }
    }
}
