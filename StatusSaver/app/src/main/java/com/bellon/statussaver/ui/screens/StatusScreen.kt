@file:OptIn(ExperimentalFoundationApi::class)

package com.bellon.statussaver.ui.screens

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.ImageLoader
import com.bellon.statussaver.DetailsScreen
import com.bellon.statussaver.ui.screens.components.MediaGallery
import kotlinx.coroutines.launch

@Composable
fun StatusScreen(
    mediaFiles: List<Uri>,
    navController: NavHostController,
    onSaveMedia: (Uri, Boolean) -> Unit,
    isMediaSaved: (Uri) -> Boolean,
    imageLoader: ImageLoader
) {
    val tabs = listOf("Images", "Videos")
    val pagerState = rememberPagerState(pageCount = { tabs.size }, initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Status Saver",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            IconButton(
                onClick = {},
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> MediaGallery(
                    mediaFiles = mediaFiles.filter { it.toString().endsWith(".jpg", true) },
                    onMediaClick = { index ->
                        navController.navigate(DetailsScreen.ImagePreview.createRoute(index, true))
                    },
                    onSaveMedia = { uri -> onSaveMedia(uri, false) },
                    isMediaSaved = isMediaSaved,
                    imageLoader = imageLoader
                )

                1 -> MediaGallery(
                    mediaFiles = mediaFiles.filter { it.toString().endsWith(".mp4", true) },
                    onMediaClick = { index ->
                        navController.navigate(DetailsScreen.VideoPreview.createRoute(index, true))
                    },
                    onSaveMedia = { uri -> onSaveMedia(uri, true) },
                    isMediaSaved = isMediaSaved,
                    imageLoader = imageLoader
                )
            }
        }
    }
}