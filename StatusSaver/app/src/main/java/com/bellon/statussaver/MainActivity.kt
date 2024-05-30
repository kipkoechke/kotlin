@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package com.bellon.statussaver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bellon.statussaver.models.BottomNavigationItem
import com.bellon.statussaver.models.TabItem
import com.bellon.statussaver.presentations.CardsContent
import com.bellon.statussaver.presentations.ImagePreview
import com.bellon.statussaver.presentations.components.TabScreen
import com.bellon.statussaver.ui.theme.StatusSaverTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StatusSaverTheme {
                val navController = rememberNavController()
                var selectedItemIndex by rememberSaveable { mutableStateOf(0) }
                val items = listOf(
                    BottomNavigationItem(
                        title = "Status",
                        selectedIcon = Icons.Filled.Favorite,
                        unselectedIcon = Icons.Outlined.FavoriteBorder,
                        hasCount = false
                    ),
                    BottomNavigationItem(
                        title = "Videos",
                        selectedIcon = Icons.Filled.Info,
                        unselectedIcon = Icons.Outlined.Info,
                        hasCount = true,
                        badgeCount = 10
                    ),
                    BottomNavigationItem(
                        title = "Settings",
                        selectedIcon = Icons.Filled.Settings,
                        unselectedIcon = Icons.Outlined.Settings,
                        hasCount = false
                    )
                )

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                    bottomBar = {
                        NavigationBar() {

                            items.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    icon = {
                                        if (item.hasCount) {
                                            BadgedBox(badge = { Badge { Text(text = item.badgeCount.toString()) } }) {
                                                Icon(
                                                    imageVector = if (selectedItemIndex == index) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.title
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = if (selectedItemIndex == index) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.title
                                            )
                                        }
                                    },
                                    label = {
                                        Text(text = item.title)
                                    },
                                    selected = selectedItemIndex == index,
                                    onClick = {
                                        selectedItemIndex = index
                                        navController.navigate(DestinationScreen.entries[index].name)
                                    }
                                )
                            }
                        }
                    }
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = DestinationScreen.STATUS.name,
                        modifier = Modifier.padding(it),
                    ) {
                        composable(DestinationScreen.STATUS.name) {
//                            TabScreen(onNavigateToItemDetail = {
//                                navController.navigate(DestinationScreen.IMAGE_PREVIEW.name)
//
//                            })

                            TabScreen(
                                tabItems = listOf(
                                    TabItem(
                                        title = "STATUS",
                                        selectedIcon = Icons.Default.Favorite,
                                        unselectedIcon = Icons.Default.Favorite
                                    ),
                                    TabItem(
                                        title = "VIDEOS",
                                        selectedIcon = Icons.Default.Info,
                                        unselectedIcon = Icons.Default.Info
                                    )
                                ),
                                content = { page ->
                                    when (page) {
                                        0 -> CardsContent(onNavigateToItemDetail = {
                                            navController.navigate(
                                                DestinationScreen.IMAGE_PREVIEW.name
                                            )
                                        })

                                        1 -> {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "VIDEOS")
                                            }
                                        }
                                    }
                                },
                            )
                        }
                        composable(DestinationScreen.SAVED.name) {
                            TabScreen(
                                tabItems = listOf(
                                    TabItem(
                                        title = "SAVED IMAGES",
                                        selectedIcon = Icons.Default.Favorite,
                                        unselectedIcon = Icons.Default.Favorite
                                    ),
                                    TabItem(
                                        title = "SAVED VIDEOS",
                                        selectedIcon = Icons.Default.Info,
                                        unselectedIcon = Icons.Default.Info
                                    )
                                ),
                                content = { page ->
                                    when (page) {
                                        0 -> CardsContent(onNavigateToItemDetail = {
                                            navController.navigate(
                                                DestinationScreen.IMAGE_PREVIEW.name
                                            )
                                        })

                                        1 -> {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "SAVED VIDEOS")
                                            }
                                        }
                                    }
                                },
                            )
                        }
                        composable(DestinationScreen.SETTINGS.name) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Settings")
                            }
                        }
                        composable(DestinationScreen.IMAGE_PREVIEW.name) {
                            ImagePreview()
                        }
                        composable(DestinationScreen.VIDEO_PREVIEW.name) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "VIDEO PREVIEW")
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class DestinationScreen {
    STATUS, SAVED, SETTINGS, IMAGE_PREVIEW, VIDEO_PREVIEW
}

val STATUS_DIRECTORY =
    File("/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/.Statuses")
