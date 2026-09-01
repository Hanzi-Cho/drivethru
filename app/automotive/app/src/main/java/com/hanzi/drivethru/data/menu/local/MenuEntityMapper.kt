package com.hanzi.drivethru.data.menu.local

import com.hanzi.drivethru.core.model.MenuItem
import com.hanzi.drivethru.core.model.MenuOptionChoice
import com.hanzi.drivethru.core.model.MenuOptionGroup
import org.json.JSONArray
import org.json.JSONObject

fun MenuItem.toEntity(storeId: String): MenuItemEntity {
    return MenuItemEntity(
        storeId = storeId,
        id = id,
        name = name,
        price = price,
        category = category,
        available = available,
        description = description,
        quickOrderEligible = quickOrderEligible,
        imageUrl = imageUrl,
        optionGroupsJson = encodeOptionGroups(optionGroups),
    )
}

fun MenuItemEntity.toDomain(): MenuItem {
    return MenuItem(
        id = id,
        name = name,
        price = price,
        category = category,
        available = available,
        description = description,
        quickOrderEligible = quickOrderEligible,
        imageUrl = imageUrl,
        optionGroups = decodeOptionGroups(optionGroupsJson),
    )
}

private fun encodeOptionGroups(groups: List<MenuOptionGroup>): String {
    val array = JSONArray()
    groups.forEach { group ->
        val choices = JSONArray()
        group.choices.forEach { choice ->
            choices.put(
                JSONObject().apply {
                    put("id", choice.id)
                    put("label", choice.label)
                    put("priceDelta", choice.priceDelta)
                    put("imageUrl", choice.imageUrl)
                },
            )
        }
        array.put(
            JSONObject().apply {
                put("id", group.id)
                put("title", group.title)
                put("required", group.required)
                put("minSelections", group.minSelections)
                put("maxSelections", group.maxSelections)
                put("choices", choices)
            },
        )
    }
    return array.toString()
}

private fun decodeOptionGroups(json: String): List<MenuOptionGroup> {
    if (json.isBlank()) {
        return emptyList()
    }
    val array = JSONArray(json)
    return buildList {
        for (groupIndex in 0 until array.length()) {
            val group = array.getJSONObject(groupIndex)
            val choices = group.getJSONArray("choices")
            add(
                MenuOptionGroup(
                    id = group.getString("id"),
                    title = group.getString("title"),
                    required = group.getBoolean("required"),
                    minSelections = group.getInt("minSelections"),
                    maxSelections = group.getInt("maxSelections"),
                    choices = buildList {
                        for (choiceIndex in 0 until choices.length()) {
                            val choice = choices.getJSONObject(choiceIndex)
                            add(
                                MenuOptionChoice(
                                    id = choice.getString("id"),
                                    label = choice.getString("label"),
                                    priceDelta = choice.getInt("priceDelta"),
                                    imageUrl = choice.optString("imageUrl").ifBlank { null },
                                ),
                            )
                        }
                    },
                ),
            )
        }
    }
}
