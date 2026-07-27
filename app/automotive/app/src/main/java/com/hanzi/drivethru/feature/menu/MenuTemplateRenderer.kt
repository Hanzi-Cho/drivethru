package com.hanzi.drivethru.feature.menu

import androidx.car.app.CarContext
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.hanzi.drivethru.R
import com.hanzi.drivethru.core.model.AppDestination
import com.hanzi.drivethru.core.model.MenuItem
import com.hanzi.drivethru.core.state.DriveThruStateStore
import com.hanzi.drivethru.feature.common.DriveThruTemplateSupport

class MenuTemplateRenderer(
    private val carContext: CarContext,
    private val stateStore: DriveThruStateStore,
    private val onStateChanged: () -> Unit,
) {
    fun render(): Template {
        return buildListTemplate()
    }

    private fun buildListTemplate(): Template {
        val list = ItemList.Builder()
            .addItem(DriveThruTemplateSupport.buildStatusRow(stateStore.getGlobalStatus()))

        val summary = stateStore.getCartSummary()
        list.addItem(
            Row.Builder()
                .setTitle(carContext.getString(R.string.section_order_summary))
                .addText(
                    carContext.getString(
                        R.string.menu_summary_title,
                        DriveThruTemplateSupport.formatPrice(summary.total),
                    ),
                )
                .addText(
                    carContext.getString(
                        R.string.menu_summary_subtitle,
                        summary.itemCount.toString(),
                    ),
                )
                .setOnClickListener {
                    stateStore.selectDestination(AppDestination.CART)
                    onStateChanged()
                }
                .build(),
        )

        stateStore.getMenuSections().forEach { section ->
            list.addItem(
                Row.Builder()
                    .setTitle(section.title)
                    .addText(carContext.getString(R.string.menu_category_hint))
                    .build(),
            )
            section.items.forEach { item ->
                list.addItem(buildMenuRow(item))
            }
        }

        return ListTemplate.Builder()
            .setSingleList(list.build())
            .build()
    }

    private fun buildMenuRow(item: MenuItem): Row {
        return Row.Builder()
            .setTitle(item.name)
            .addText("${DriveThruTemplateSupport.formatPrice(item.price)} · ${item.description}")
            .setOnClickListener {
                stateStore.addMenuItem(item.id)
                onStateChanged()
            }
            .build()
    }
}
