package com.hanzi.drivethru.core.state

import com.hanzi.drivethru.core.model.AppDestination
import com.hanzi.drivethru.core.model.CartLineItem
import com.hanzi.drivethru.core.model.CartSummary
import com.hanzi.drivethru.core.model.GlobalStatus
import com.hanzi.drivethru.core.model.MenuSection
import com.hanzi.drivethru.core.model.OrderReceipt
import com.hanzi.drivethru.core.model.PaymentMethod
import com.hanzi.drivethru.core.model.SettingEntry
import com.hanzi.drivethru.data.menu.MenuRepository
import com.hanzi.drivethru.data.payment.PaymentMethodRepository
import com.hanzi.drivethru.data.settings.SettingsRepository
import com.hanzi.drivethru.data.status.StatusRepository

class DriveThruStateStore(
    private val menuRepository: MenuRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val settingsRepository: SettingsRepository,
    private val statusRepository: StatusRepository,
) {
    private val cartQuantities = linkedMapOf<String, Int>()
    private val cartOptions = mutableMapOf<String, List<String>>()

    var activeDestination: AppDestination = AppDestination.MENU
        private set

    private var latestOrderReceipt: OrderReceipt? = null

    init {
        menuRepository.getSeededCart().forEach { seed ->
            cartQuantities[seed.menuItemId] = seed.quantity
            cartOptions[seed.menuItemId] = seed.selectedOptions
        }
    }

    fun selectDestination(destination: AppDestination) {
        activeDestination = destination
    }

    fun getGlobalStatus(): GlobalStatus = statusRepository.getStatus()

    fun getMenuSections(): List<MenuSection> = menuRepository.getMenuSections()

    fun addMenuItem(itemId: String) {
        if (menuRepository.findMenuItemById(itemId) == null) {
            return
        }
        cartQuantities[itemId] = (cartQuantities[itemId] ?: 0) + 1
        if (!cartOptions.containsKey(itemId)) {
            cartOptions[itemId] = listOf("Standard")
        }
    }

    fun getCartItems(): List<CartLineItem> {
        return cartQuantities
            .mapNotNull { (itemId, quantity) ->
                val menuItem = menuRepository.findMenuItemById(itemId) ?: return@mapNotNull null
                CartLineItem(
                    menuItem = menuItem,
                    quantity = quantity,
                    selectedOptions = cartOptions[itemId].orEmpty(),
                )
            }
    }

    fun hasCartItems(): Boolean = cartQuantities.isNotEmpty()

    fun incrementCartItem(itemId: String) {
        addMenuItem(itemId)
    }

    fun decrementCartItem(itemId: String) {
        val current = cartQuantities[itemId] ?: return
        if (current <= 1) {
            removeCartItem(itemId)
        } else {
            cartQuantities[itemId] = current - 1
        }
    }

    fun removeCartItem(itemId: String) {
        cartQuantities.remove(itemId)
        cartOptions.remove(itemId)
    }

    fun getCartSummary(): CartSummary {
        val items = getCartItems()
        val subtotal = items.sumOf { it.totalPrice }
        return CartSummary(
            subtotal = subtotal,
            discount = 0,
            total = subtotal,
            itemCount = items.sumOf { it.quantity },
        )
    }

    fun getDefaultPaymentMethod(): PaymentMethod = paymentMethodRepository.getDefaultPaymentMethod()

    fun submitOrder() {
        val cartItems = getCartItems()
        if (cartItems.isEmpty()) {
            return
        }

        val summary = getCartSummary()
        latestOrderReceipt = OrderReceipt(
            orderId = "DT-${System.currentTimeMillis().toString().takeLast(6)}",
            storeName = "Drive-Thru Service",
            items = cartItems,
            paymentMethod = getDefaultPaymentMethod(),
            totalAmount = summary.total,
            pickupMessage = "Move forward to the pickup zone.",
        )
        activeDestination = AppDestination.ORDER
    }

    fun getLatestOrderReceipt(): OrderReceipt? = latestOrderReceipt

    fun getSettings(): List<SettingEntry> = settingsRepository.getSettings()
}
