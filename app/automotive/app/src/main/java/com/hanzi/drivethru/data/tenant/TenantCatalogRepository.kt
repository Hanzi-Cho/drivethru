package com.hanzi.drivethru.data.tenant

import android.content.Context
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import com.hanzi.drivethru.core.model.EntryTriggerEvent
import com.hanzi.drivethru.core.model.EntryTriggerSource
import com.hanzi.drivethru.core.model.MenuItem
import com.hanzi.drivethru.core.model.MenuOptionChoice
import com.hanzi.drivethru.core.model.MenuOptionGroup
import com.hanzi.drivethru.core.model.Store
import com.hanzi.drivethru.core.model.StoreCapability
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.pow

class TenantCatalogRepository(
    private val context: Context,
) {
    private val status = AtomicReference("Tenant catalog idle.")
    private val resolverIndex: List<TenantResolverEntry> by lazy {
        parseResolverIndex(readAsset("tenants/resolver-map.json"))
    }
    private val storeConfigCache = mutableMapOf<String, TenantStoreConfig>()
    private val menuCatalogCache = mutableMapOf<String, TenantMenuCatalog>()

    fun resolveStore(entryTriggerEvent: EntryTriggerEvent): Store? {
        if (entryTriggerEvent.stage == DriveThruZoneStage.OUTSIDE || entryTriggerEvent.stage == DriveThruZoneStage.EXIT) {
            return null
        }

        val resolverEntry = when (entryTriggerEvent.source) {
            EntryTriggerSource.BEACON -> resolverIndex.firstOrNull { entry ->
                entryTriggerEvent.beaconId != null && entry.beaconIds.contains(entryTriggerEvent.beaconId)
            }

            EntryTriggerSource.GPS_GEOFENCE -> resolverIndex.minByOrNull { entry ->
                distanceScore(entry, entryTriggerEvent)
            }?.takeIf { entry ->
                isWithinFence(entry, entryTriggerEvent)
            }
        } ?: return null

        val storeConfig = loadStoreConfig(resolverEntry.storeId)
        return Store(
            id = storeConfig.storeId,
            brandId = storeConfig.brandId,
            name = storeConfig.displayName,
            capabilities = storeConfig.capabilities,
            menuSource = storeConfig.menuSourceMode,
            tenantPath = storeConfig.tenantPath,
        )
    }

    fun loadStoreConfig(storeId: String): TenantStoreConfig {
        return storeConfigCache.getOrPut(storeId) {
            val entry = resolverIndex.first { it.storeId == storeId }
            parseStoreConfig(
                tenantPath = entry.tenantPath,
                json = readAsset("${entry.tenantPath}/store.config.json"),
            )
        }
    }

    fun loadMenuCatalog(storeId: String): TenantMenuCatalog {
        return menuCatalogCache.getOrPut(storeId) {
            val storeConfig = loadStoreConfig(storeId)
            loadCatalog(storeConfig)
        }
    }

    fun getStatus(): String = status.get()

    private fun loadCatalog(storeConfig: TenantStoreConfig): TenantMenuCatalog {
        val localOptionGroups = storeConfig.localOrderOptionsAssetPath?.let { assetPath ->
            runCatching {
                parseOptionGroupMap(readAsset(assetPath))
            }.getOrElse { emptyMap() }
        }.orEmpty()
        val localCatalog = storeConfig.localMenuAssetPath?.let { assetPath ->
            runCatching {
                parseMenuCatalog(readAsset(assetPath), localOptionGroups)
            }.getOrNull()
        }

        return when (storeConfig.menuSourceMode) {
            "remote-only" -> loadRemoteCatalog(storeConfig) ?: localCatalog ?: emptyCatalog(storeConfig.storeId)
            "remote-first" -> loadRemoteCatalog(storeConfig) ?: localCatalog ?: emptyCatalog(storeConfig.storeId)
            else -> localCatalog ?: loadRemoteCatalog(storeConfig) ?: emptyCatalog(storeConfig.storeId)
        }
    }

    private fun loadRemoteCatalog(storeConfig: TenantStoreConfig): TenantMenuCatalog? {
        val url = storeConfig.remoteMenuUrl ?: return null
        return runCatching {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 1200
            connection.readTimeout = 1200
            connection.requestMethod = "GET"
            connection.inputStream.bufferedReader().use { reader ->
                val body = reader.readText()
                parseMenuCatalog(body, emptyMap()).also {
                    status.set("Remote tenant menu loaded for ${storeConfig.storeId}.")
                }
            }
        }.getOrElse {
            status.set("Remote menu load failed for ${storeConfig.storeId}; using local fallback if available.")
            null
        }
    }

    private fun emptyCatalog(storeId: String): TenantMenuCatalog {
        status.set("No tenant menu available for $storeId. Empty catalog loaded.")
        return TenantMenuCatalog(storeId = storeId, items = emptyList())
    }

    private fun parseResolverIndex(json: String): List<TenantResolverEntry> {
        val root = JSONObject(json)
        val stores = root.getJSONArray("stores")
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
                add(
                    TenantResolverEntry(
                        brandId = item.getString("brandId"),
                        storeId = item.getString("storeId"),
                        displayName = item.getString("displayName"),
                        tenantPath = item.getString("tenantPath"),
                        geofence = geofence,
                        beaconIds = item.optJSONArray("beaconIds").toStringList(),
                    ),
                )
            }
        }
    }

    private fun parseStoreConfig(tenantPath: String, json: String): TenantStoreConfig {
        val root = JSONObject(json)
        val geofence = root.optJSONObject("geofence")?.let {
            TenantGeoFence(
                latitude = it.getDouble("latitude"),
                longitude = it.getDouble("longitude"),
                radiusMeters = it.getDouble("radiusMeters"),
            )
        }
        return TenantStoreConfig(
            brandId = root.getString("brandId"),
            storeId = root.getString("storeId"),
            displayName = root.getString("displayName"),
            capabilities = root.getJSONArray("capabilities").toStoreCapabilities(),
            menuSourceMode = root.getJSONObject("menuSource").getString("mode"),
            localMenuAssetPath = root.getJSONObject("menuSource").optString("localMenuAssetPath").ifBlank { null },
            localOrderOptionsAssetPath = root.getJSONObject("menuSource").optString("localOrderOptionsAssetPath").ifBlank { null },
            remoteMenuUrl = root.getJSONObject("menuSource").optString("remoteMenuUrl").ifBlank { null },
            geofence = geofence,
            beaconIds = root.optJSONArray("beaconIds").toStringList(),
            tenantPath = tenantPath,
        )
    }

    private fun parseMenuCatalog(
        json: String,
        optionGroupMap: Map<String, MenuOptionGroup>,
    ): TenantMenuCatalog {
        val root = JSONObject(json)
        val items = root.getJSONArray("items")
        return TenantMenuCatalog(
            storeId = root.getString("storeId"),
            items = buildList {
                for (index in 0 until items.length()) {
                    val item = items.getJSONObject(index)
                    add(
                        MenuItem(
                            id = item.getString("id"),
                            name = item.getString("name"),
                            price = item.getInt("price"),
                            category = item.getString("category"),
                            available = item.optBoolean("available", true),
                            description = item.optString("description", ""),
                            quickOrderEligible = item.optBoolean("quickOrderEligible", false),
                            imageUrl = item.optString("imageUrl").ifBlank { null },
                            optionGroups = resolveOptionGroups(
                                inlineGroups = item.optJSONArray("optionGroups").toOptionGroups(),
                                referencedGroupIds = item.optJSONArray("optionGroupIds").toStringList(),
                                optionGroupMap = optionGroupMap,
                            ),
                        ),
                    )
                }
            },
        )
    }

    private fun parseOptionGroupMap(json: String): Map<String, MenuOptionGroup> {
        val root = JSONObject(json)
        return root.getJSONArray("optionGroups")
            .toOptionGroups()
            .associateBy { it.id }
    }

    private fun resolveOptionGroups(
        inlineGroups: List<MenuOptionGroup>,
        referencedGroupIds: List<String>,
        optionGroupMap: Map<String, MenuOptionGroup>,
    ): List<MenuOptionGroup> {
        if (inlineGroups.isNotEmpty()) {
            return inlineGroups
        }
        if (referencedGroupIds.isEmpty()) {
            return emptyList()
        }
        return referencedGroupIds.mapNotNull(optionGroupMap::get)
    }

    private fun distanceScore(entry: TenantResolverEntry, event: EntryTriggerEvent): Double {
        val geofence = entry.geofence ?: return Double.MAX_VALUE
        val latitude = event.latitude ?: return Double.MAX_VALUE
        val longitude = event.longitude ?: return Double.MAX_VALUE
        return (geofence.latitude - latitude).pow(2) + (geofence.longitude - longitude).pow(2)
    }

    private fun isWithinFence(entry: TenantResolverEntry, event: EntryTriggerEvent): Boolean {
        val geofence = entry.geofence ?: return false
        val latitude = event.latitude ?: return false
        val longitude = event.longitude ?: return false
        val roughMeters = kotlin.math.sqrt((geofence.latitude - latitude).pow(2) + (geofence.longitude - longitude).pow(2)) * 111_000
        return roughMeters <= geofence.radiusMeters
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                add(getString(index))
            }
        }
    }

    private fun JSONArray.toStoreCapabilities(): Set<StoreCapability> {
        return buildSet {
            for (index in 0 until length()) {
                add(StoreCapability.valueOf(getString(index)))
            }
        }
    }

    private fun JSONArray?.toOptionGroups(): List<MenuOptionGroup> {
        if (this == null) return emptyList()
        return buildList {
            for (groupIndex in 0 until length()) {
                val group = getJSONObject(groupIndex)
                add(
                    MenuOptionGroup(
                        id = group.getString("id"),
                        title = group.getString("title"),
                        required = group.optBoolean("required", false),
                        minSelections = group.optInt("minSelections", 0),
                        maxSelections = group.optInt("maxSelections", 1),
                        choices = group.getJSONArray("choices").toOptionChoices(),
                    ),
                )
            }
        }
    }

    private fun JSONArray.toOptionChoices(): List<MenuOptionChoice> {
        return buildList {
            for (choiceIndex in 0 until length()) {
                val choice = getJSONObject(choiceIndex)
                add(
                    MenuOptionChoice(
                        id = choice.getString("id"),
                        label = choice.getString("label"),
                        priceDelta = choice.optInt("priceDelta", 0),
                        imageUrl = choice.optString("imageUrl").ifBlank { null },
                    ),
                )
            }
        }
    }

    private fun readAsset(path: String): String {
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }
}
