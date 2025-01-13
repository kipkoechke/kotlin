package com.bellon.statussaver.models

import androidx.annotation.DrawableRes

data class BottomNavigationItem(
    val title: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
    val hasCount: Boolean,
    val badgeCount: Int? = null
    )