package com.hanzi.drivethru.data.vehicle

import android.content.Context
import com.hanzi.drivethru.core.model.CarSignalReading
import com.hanzi.drivethru.core.model.CarSignalSource
import com.hanzi.drivethru.core.model.CarSignalStatus
import com.hanzi.drivethru.core.model.CarSignalType

class CarPropertyManagerCarDataFacade(
    private val context: Context,
) : CarDataFacade {
    override fun read(type: CarSignalType): CarSignalReading {
        return try {
            val carClass = Class.forName("android.car.Car")
            val propertyIdsClass = Class.forName("android.car.VehiclePropertyIds")
            val propertyService = carClass.getField("PROPERTY_SERVICE").get(null) as String
            val createCar = carClass.getMethod("createCar", Context::class.java)
            val carInstance = createCar.invoke(null, context)
            val getCarManager = carClass.getMethod("getCarManager", String::class.java)
            val manager = getCarManager.invoke(carInstance, propertyService)
            val propertyId = propertyIdsClass.getField(type.propertyFieldName).getInt(null)
            val rawValue = readSignalValue(manager, type, propertyId)
            CarSignalReading(
                type = type,
                rawValue = rawValue,
                source = CarSignalSource.CAR_PROPERTY_MANAGER,
                status = CarSignalStatus.OK,
                timestampMillis = System.currentTimeMillis(),
                detail = "CarPropertyManager reflection read",
            )
        } catch (throwable: Throwable) {
            CarSignalReading(
                type = type,
                rawValue = type.defaultValue,
                source = CarSignalSource.CAR_PROPERTY_MANAGER,
                status = CarSignalStatus.UNAVAILABLE,
                timestampMillis = System.currentTimeMillis(),
                detail = throwable.javaClass.simpleName,
            )
        }
    }

    private fun readSignalValue(manager: Any, type: CarSignalType, propertyId: Int): Any {
        return when (type.valueKind) {
            com.hanzi.drivethru.core.model.CarSignalValueKind.INT -> {
                manager.javaClass.getMethod("getIntProperty", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                    .invoke(manager, propertyId, 0) as Int
            }

            com.hanzi.drivethru.core.model.CarSignalValueKind.FLOAT -> {
                manager.javaClass.getMethod("getFloatProperty", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                    .invoke(manager, propertyId, 0) as Float
            }

            com.hanzi.drivethru.core.model.CarSignalValueKind.BOOLEAN -> {
                manager.javaClass.getMethod("getBooleanProperty", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                    .invoke(manager, propertyId, 0) as Boolean
            }
        }
    }
}
