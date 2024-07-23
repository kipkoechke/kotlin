package com.bellon.statussaver

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import coil.ImageLoader
import com.bellon.statussaver.ads.FacebookBannerAd
import com.bellon.statussaver.models.BottomNavigationItem
import com.bellon.statussaver.ui.screens.EmptyStateScreen
import com.bellon.statussaver.ui.screens.ImagePreview
import com.bellon.statussaver.ui.screens.MediaViewModel
import com.bellon.statussaver.ui.screens.RequestAccessScreen
import com.bellon.statussaver.ui.screens.SavedScreen
import com.bellon.statussaver.ui.screens.SettingsScreen
import com.bellon.statussaver.ui.screens.StatusScreen
import com.bellon.statussaver.ui.screens.VideoPreview

@SuppressLint("ResourceAsColor")
@Composable
fun WhatsAppStatusApp(
    viewModel: MediaViewModel,
    whatsAppStatusUri: Uri?,
    onRequestAccess: () -> Unit,
    onSaveMedia: (Uri, Boolean) -> Unit,
    isMediaSaved: (Uri) -> Boolean,
    navController: NavHostController,
    imageLoader: ImageLoader,
    isAdShowing: Boolean
) {
    val currentMediaFiles by viewModel.mediaFiles.collectAsState()
    val currentSavedMediaFiles by viewModel.savedMediaFiles.collectAsState()
    val isInitialLoading by viewModel.isInitialLoading.collectAsState()
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            Column {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                if (whatsAppStatusUri != null && currentRoute !in routesWithoutBottomBar) {
                    NavigationBar(
                        modifier = Modifier.height(64.dp),
                    ) {
                        var selectedItemIndex by rememberSaveable { mutableStateOf(0) }
                        val items = listOf(
                            BottomNavigationItem(
                                title = Screen.Status.title,
                                selectedIcon = Screen.Status.selectedIcon,
                                unselectedIcon = Screen.Status.unselectedIcon,
                                hasCount = false
                            ),
                            BottomNavigationItem(
                                title = Screen.Saved.title,
                                selectedIcon = Screen.Saved.selectedIcon,
                                unselectedIcon = Screen.Saved.unselectedIcon,
                                hasCount = false,
                            ),
                            BottomNavigationItem(
                                title = Screen.Settings.title,
                                selectedIcon = Screen.Settings.selectedIcon,
                                unselectedIcon = Screen.Settings.unselectedIcon,
                                hasCount = false
                            )
                        )
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                colors = NavigationBarItemDefaults.colors()
                                    .copy(selectedIndicatorColor = Color.Transparent),
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

                FacebookBannerAd(
                    placementId = "IMG_16_9_APP_INSTALL#1933718153761687_1933718793761623",
                    modifier = Modifier.fillMaxWidth()
                )

            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Status.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Status.route) {
                if (whatsAppStatusUri == null) {
                    RequestAccessScreen(onRequestAccess)
                } else if (isInitialLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (currentMediaFiles.isEmpty()) {
                    EmptyStateScreen("No status updates found")
                } else {
                    StatusScreen(
                        mediaFiles = currentMediaFiles,
                        navController = navController,
                        onSaveMedia = { uri, isVideo -> viewModel.saveMedia(uri, isVideo) },
                        isMediaSaved = { uri -> viewModel.isMediaSaved(uri) },
                        imageLoader = imageLoader
                    )
                }
            }
            composable(Screen.Saved.route) {
                SavedScreen(
                    savedMediaFiles = currentSavedMediaFiles,
                    navController = navController,
                    onSaveMedia = onSaveMedia
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(
                route = DetailsScreen.ImagePreview.route,
                arguments = listOf(
                    navArgument("imageIndex") { type = NavType.IntType },
                    navArgument("isStatus") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val imageIndex = backStackEntry.arguments?.getInt("imageIndex") ?: 0
                val isStatus = backStackEntry.arguments?.getBoolean("isStatus") ?: true
                val imageFiles = if (isStatus) {
                    currentMediaFiles.filter { it.toString().endsWith(".jpg", true) }
                } else {
                    currentSavedMediaFiles.filter {
                        val mimeType = context.contentResolver.getType(it)
                        mimeType?.startsWith("image/") == true
                    }
                }
                if (imageFiles.isNotEmpty()) {
                    ImagePreview(
                        imageUris = imageFiles,
                        initialPage = imageIndex,
                        onDismiss = { navController.navigateUp() },
                        isMediaSaved = isMediaSaved,
                        onSave = if (isStatus) {
                            { uri -> onSaveMedia(uri, false) }
                        } else null,
                        onDelete = if (!isStatus) {
                            { uri ->
                                viewModel.deleteMedia(uri) {
                                    if (imageFiles.size == 1) {
                                        navController.navigateUp()
                                    }
                                }
                            }
                        } else null
                    )
                }
            }

            composable(
                route = DetailsScreen.VideoPreview.route,
                arguments = listOf(
                    navArgument("videoIndex") { type = NavType.IntType },
                    navArgument("isStatus") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val videoIndex = backStackEntry.arguments?.getInt("videoIndex") ?: 0
                val isStatus = backStackEntry.arguments?.getBoolean("isStatus") ?: true
                val videoFiles = if (isStatus) {
                    currentMediaFiles.filter { it.toString().endsWith(".mp4", true) }
                } else {
                    currentSavedMediaFiles.filter {
                        val mimeType = context.contentResolver.getType(it)
                        mimeType?.startsWith("video/") == true
                    }
                }
                if (videoFiles.isNotEmpty()) {
                    VideoPreview(
                        videoUris = videoFiles,
                        initialPage = videoIndex,
                        onDismiss = { navController.navigateUp() },
                        isMediaSaved = isMediaSaved,
                        isInterrupted = isAdShowing,
                        onSave = if (isStatus) {
                            { uri -> onSaveMedia(uri, true) }
                        } else null,
                        onDelete = if (!isStatus) {
                            { uri ->
                                viewModel.deleteMedia(uri) {
                                    if (videoFiles.size == 1) {
                                        navController.navigateUp()
                                    }
                                }
                            }
                        } else null
                    )
                }
            }
        }
    }
}
