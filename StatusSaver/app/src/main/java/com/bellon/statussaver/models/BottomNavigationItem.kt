package com.bellon.statussaver.models

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasCount: Boolean,
    val badgeCount: Int? = null
)