package com.hanzi.drivethru.data.status

import com.hanzi.drivethru.core.model.GlobalStatus

class FakeStatusRepository : StatusRepository {
    override fun getStatus(): GlobalStatus {
        return GlobalStatus(
            cautionLabel = "Drive carefully",
            dataLevel = "LTE full",
            wifiLevel = "Wi-Fi full",
            batteryLevel = "Battery 82%",
            timeLabel = "12:45 PM",
        )
    }
}
