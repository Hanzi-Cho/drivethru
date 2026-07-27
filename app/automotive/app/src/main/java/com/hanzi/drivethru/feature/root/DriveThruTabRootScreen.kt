package com.hanzi.drivethru.feature.root

import androidx.annotation.OptIn
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.annotations.ExperimentalCarApi
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Tab
import androidx.car.app.model.TabContents
import androidx.car.app.model.TabTemplate
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import com.hanzi.drivethru.core.model.AppDestination
import com.hanzi.drivethru.core.state.DriveThruStateStore
import com.hanzi.drivethru.feature.cart.CartTemplateRenderer
import com.hanzi.drivethru.feature.menu.MenuTemplateRenderer
import com.hanzi.drivethru.feature.order.OrderTemplateRenderer
import com.hanzi.drivethru.feature.settings.SettingsTemplateRenderer

@OptIn(ExperimentalCarApi::class)
class DriveThruTabRootScreen(
    carContext: CarContext,
    private val stateStore: DriveThruStateStore,
) : Screen(carContext) {
    private val tabs: List<Tab> = AppDestination.entries.map { destination ->
        Tab.Builder()
            .setContentId(destination.contentId)
            .setTitle(carContext.getString(destination.titleRes))
            .setIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(carContext, destination.iconRes),
                ).build(),
            )
            .build()
    }

    override fun onGetTemplate(): Template {
        val contentTemplate = when (stateStore.activeDestination) {
            AppDestination.MENU -> MenuTemplateRenderer(carContext, stateStore, ::invalidate).render()
            AppDestination.CART -> CartTemplateRenderer(carContext, stateStore, ::invalidate).render()
            AppDestination.ORDER -> OrderTemplateRenderer(carContext, stateStore, ::invalidate).render()
            AppDestination.SETTING -> SettingsTemplateRenderer(carContext, stateStore).render()
        }

        return TabTemplate.Builder(
            object : TabTemplate.TabCallback {
                override fun onTabSelected(tabContentId: String) {
                    stateStore.selectDestination(AppDestination.fromContentId(tabContentId))
                    invalidate()
                }
            },
        )
            .apply { tabs.forEach(this::addTab) }
            .setActiveTabContentId(stateStore.activeDestination.contentId)
            .setHeaderAction(Action.APP_ICON)
            .setTabContents(TabContents.Builder(contentTemplate).build())
            .build()
    }
}
