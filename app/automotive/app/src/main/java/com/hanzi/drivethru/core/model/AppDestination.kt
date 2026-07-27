package com.hanzi.drivethru.core.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.hanzi.drivethru.R

enum class AppDestination(
    val contentId: String,
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
) {
    MENU(
        contentId = "menu",
        titleRes = R.string.destination_menu,
        iconRes = android.R.drawable.ic_menu_sort_by_size,
    ),
    CART(
        contentId = "cart",
        titleRes = R.string.destination_cart,
        iconRes = android.R.drawable.ic_menu_agenda,
    ),
    ORDER(
        contentId = "order",
        titleRes = R.string.destination_order,
        iconRes = android.R.drawable.ic_menu_send,
    ),
    SETTING(
        contentId = "setting",
        titleRes = R.string.destination_setting,
        iconRes = android.R.drawable.ic_menu_manage,
    ),
    ;

    companion object {
        fun fromContentId(contentId: String): AppDestination {
            return entries.firstOrNull { it.contentId == contentId } ?: MENU
        }
    }
}
