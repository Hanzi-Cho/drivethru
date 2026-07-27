package com.hanzi.drivethru.data.status

import com.hanzi.drivethru.core.model.GlobalStatus

interface StatusRepository {
    fun getStatus(): GlobalStatus
}
