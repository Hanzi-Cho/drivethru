package com.hanzi.drivethru.data.settings

import com.hanzi.drivethru.core.model.SettingEntry

class FakeSettingsRepository : SettingsRepository {
    override fun getSettings(): List<SettingEntry> {
        return listOf(
            SettingEntry(
                id = "payment_methods",
                title = "Payment methods",
                subtitle = "Set your default card and auto-pay options",
            ),
            SettingEntry(
                id = "transaction_history",
                title = "Transaction history",
                subtitle = "Review previous drive-thru orders",
            ),
            SettingEntry(
                id = "favorite_addresses",
                title = "Favorite addresses",
                subtitle = "Save home, office, and pickup preferences",
            ),
            SettingEntry(
                id = "account_management",
                title = "Account management",
                subtitle = "Manage profile, privacy, and connected services",
            ),
        )
    }
}
