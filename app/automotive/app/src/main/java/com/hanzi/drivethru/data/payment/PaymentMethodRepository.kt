package com.hanzi.drivethru.data.payment

import com.hanzi.drivethru.core.model.PaymentMethod

interface PaymentMethodRepository {
    fun getDefaultPaymentMethod(): PaymentMethod
}
