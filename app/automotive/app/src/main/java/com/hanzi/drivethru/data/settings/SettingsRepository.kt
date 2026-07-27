package com.hanzi.drivethru.data.settings

import com.hanzi.drivethru.core.model.SettingEntry

interface SettingsRepository {
    fun getSettings(): List<SettingEntry>
}
