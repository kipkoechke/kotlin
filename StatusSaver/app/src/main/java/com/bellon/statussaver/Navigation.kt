package com.bellon.statussaver

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val title: String
) {
    object Status :
        Screen("status", Icons.Default.ShoppingCart, Icons.Outlined.FavoriteBorder, "Status")

    object Saved :
        Screen("saved", Icons.AutoMirrored.Filled.ExitToApp, Icons.Outlined.Info, "Saved")

    object Settings :
        Screen("settings", Icons.Default.Settings, Icons.Outlined.Settings, "Settings")
}

sealed class DetailsScreen(val route: String) {
    object ImagePreview : DetailsScreen("image_preview/{imageIndex}/{isStatus}") {
        fun createRoute(imageIndex: Int, isStatus: Boolean) = "image_preview/$imageIndex/$isStatus"
    }

    object VideoPreview : DetailsScreen("video_preview/{videoIndex}/{isStatus}") {
        fun createRoute(videoIndex: Int, isStatus: Boolean) = "video_preview/$videoIndex/$isStatus"
    }
}