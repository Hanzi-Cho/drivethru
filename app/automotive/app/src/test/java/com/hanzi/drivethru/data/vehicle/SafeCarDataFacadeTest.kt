package com.hanzi.drivethru.data.vehicle

import com.hanzi.drivethru.core.model.CarSignalSource
import com.hanzi.drivethru.core.model.CarSignalStatus
import com.hanzi.drivethru.core.model.CarSignalType
import org.junit.Assert.assertEquals
import org.junit.Test

class SafeCarDataFacadeTest {
    @Test
    fun `fallback facade is used when primary throws`() {
        val primary = object : CarDataFacade {
            override fun read(type: CarSignalType) = error("boom")
        }
        val fallback = FakeCarDataFacade()
        val safeFacade = SafeCarDataFacade(primary, fallback)

        val reading = safeFacade.read(CarSignalType.GEAR_SELECTION)

        assertEquals(CarSignalStatus.FALLBACK, reading.status)
        assertEquals(CarSignalSource.FAKE, reading.source)
    }
}
