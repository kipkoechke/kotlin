package com.bellon.statussaver

import androidx.annotation.DrawableRes

sealed class Screen(
    val route: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
    val title: String
) {
    object Status : Screen(
        "status",
        R.drawable.circle_down_solid,
        R.drawable.circle_down_thin,
        "Status"
    )

    object Saved : Screen(
        "saved",
        R.drawable.floppy_disk_solid,
        R.drawable.floppy_disk_thin,
        "Saved"
    )

    object Settings : Screen(
        "settings",
        R.drawable.gear_solid,
        R.drawable.gear_thin,
        "Settings"
    )
}

sealed class DetailsScreen(val route: String) {
    object ImagePreview : DetailsScreen("image_preview/{imageIndex}/{isStatus}") {
        fun createRoute(imageIndex: Int, isStatus: Boolean) = "image_preview/$imageIndex/$isStatus"
    }

    object VideoPreview : DetailsScreen("video_preview/{videoIndex}/{isStatus}") {
        fun createRoute(videoIndex: Int, isStatus: Boolean) = "video_preview/$videoIndex/$isStatus"
    }
}