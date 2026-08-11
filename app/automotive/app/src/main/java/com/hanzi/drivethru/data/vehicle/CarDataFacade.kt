package com.hanzi.drivethru.data.vehicle

import com.hanzi.drivethru.core.model.CarSignalCatalog
import com.hanzi.drivethru.core.model.CarSignalReading
import com.hanzi.drivethru.core.model.CarSignalType

interface CarDataFacade {
    fun read(type: CarSignalType): CarSignalReading

    fun observe(type: CarSignalType): CarSignalReading = read(type)

    fun snapshot(types: List<CarSignalType> = CarSignalCatalog.defaultSignals): List<CarSignalReading> {
        return types.map(::read)
    }
}
