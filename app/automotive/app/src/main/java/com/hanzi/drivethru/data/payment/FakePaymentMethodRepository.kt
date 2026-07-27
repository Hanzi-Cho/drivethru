package com.hanzi.drivethru.data.payment

import com.hanzi.drivethru.core.model.PaymentMethod

class FakePaymentMethodRepository : PaymentMethodRepository {
    override fun getDefaultPaymentMethod(): PaymentMethod {
        return PaymentMethod(
            displayName = "Hyundai Card M",
            maskedNumber = "**** **** **** 4291",
            autoPayLabel = "Auto pay enabled",
        )
    }
}
