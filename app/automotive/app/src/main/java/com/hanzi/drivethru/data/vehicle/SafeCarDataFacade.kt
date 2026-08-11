package com.hanzi.drivethru.data.vehicle

import com.hanzi.drivethru.core.model.CarSignalReading
import com.hanzi.drivethru.core.model.CarSignalSource
import com.hanzi.drivethru.core.model.CarSignalStatus
import com.hanzi.drivethru.core.model.CarSignalType

class SafeCarDataFacade(
    private val primary: CarDataFacade,
    private val fallback: CarDataFacade,
) : CarDataFacade {
    override fun read(type: CarSignalType): CarSignalReading {
        return try {
            val primaryReading = primary.read(type)
            if (primaryReading.status == CarSignalStatus.OK) {
                primaryReading
            } else {
                fallback.read(type).copy(
                    status = CarSignalStatus.FALLBACK,
                    source = CarSignalSource.FAKE,
                    detail = "Primary unavailable: ${primaryReading.detail}",
                )
            }
        } catch (throwable: Throwable) {
            fallback.read(type).copy(
                status = CarSignalStatus.FALLBACK,
                source = CarSignalSource.FAKE,
                detail = "Primary exception: ${throwable.javaClass.simpleName}",
            )
        }
    }
}
