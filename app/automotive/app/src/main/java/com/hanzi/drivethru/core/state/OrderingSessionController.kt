package com.hanzi.drivethru.core.state

import com.hanzi.drivethru.core.model.MenuItem
import com.hanzi.drivethru.core.model.OrderDraft
import com.hanzi.drivethru.core.model.OrderLineItem
import com.hanzi.drivethru.core.model.OrderingSession
import com.hanzi.drivethru.core.model.Store

class OrderingSessionController {
    private var activeSession: OrderingSession? = null

    fun startSession(store: Store, nowMillis: Long = System.currentTimeMillis()) {
        activeSession = OrderingSession(
            store = store,
            orderDraft = OrderDraft(
                storeName = store.name,
                items = emptyList(),
            ),
            startedAtMillis = nowMillis,
            lastUpdatedAtMillis = nowMillis,
        )
    }

    fun getActiveSession(): OrderingSession? = activeSession

    fun addMenuItem(menuItem: MenuItem, nowMillis: Long = System.currentTimeMillis()) {
        val session = activeSession ?: return
        val mutableItems = session.orderDraft.items.toMutableList()
        val existingIndex = mutableItems.indexOfFirst { it.menuItem.id == menuItem.id }
        if (existingIndex >= 0) {
            val existing = mutableItems[existingIndex]
            mutableItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            mutableItems += OrderLineItem(menuItem = menuItem, quantity = 1)
        }

        activeSession = session.copy(
            orderDraft = session.orderDraft.copy(items = mutableItems),
            lastUpdatedAtMillis = nowMillis,
        )
    }

    fun clearSession() {
        activeSession = null
    }

    fun hasActiveDraft(): Boolean = activeSession?.orderDraft?.items?.isNotEmpty() == true
}
