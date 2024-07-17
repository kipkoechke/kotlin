package com.bellon.statussaver.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bellon.statussaver.DetailsScreen
import com.bellon.statussaver.ui.screens.components.SavedMediaGallery

@Composable
fun SavedScreen(
    savedMediaFiles: List<Uri>,
    navController: NavHostController,
    onSaveMedia: (Uri, Boolean) -> Unit
) {
    val context = LocalContext.current
    Log.d("SavedScreen", "Received ${savedMediaFiles.size} saved media files")

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Images", "Videos")

    val imageFiles = remember(savedMediaFiles) {
        savedMediaFiles.filter { uri ->
            val mimeType = context.contentResolver.getType(uri)
            mimeType?.startsWith("image/") == true
        }
    }
    val videoFiles = remember(savedMediaFiles) {
        savedMediaFiles.filter { uri ->
            val mimeType = context.contentResolver.getType(uri)
            mimeType?.startsWith("video/") == true
        }
    }

    Column {
        Text(
            text = "Saved Statuses",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
        )
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }
        when (selectedTab) {
            0 -> SavedMediaGallery(
                mediaFiles = imageFiles,
                onMediaClick = { index ->
                    navController.navigate(DetailsScreen.ImagePreview.createRoute(index, false))
                },
                isImage = true
            )

            1 -> SavedMediaGallery(
                mediaFiles = videoFiles,
                onMediaClick = { index ->
                    navController.navigate(DetailsScreen.VideoPreview.createRoute(index, false))
                },
                isImage = false
            )
        }
    }
}