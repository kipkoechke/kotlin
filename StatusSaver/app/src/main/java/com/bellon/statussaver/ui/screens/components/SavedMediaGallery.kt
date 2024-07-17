package com.bellon.statussaver.ui.screens.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SavedMediaGallery(
    mediaFiles: List<Uri>,
    onMediaClick: (Int) -> Unit,
    isImage: Boolean
) {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(mediaFiles) {
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
           CircularProgressIndicator()
        }
    } else if (mediaFiles.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No saved ${if (isImage) "images" else "videos"} found")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(mediaFiles) { index, uri ->
                Log.d("SavedMediaGallery", "Displaying item $index: $uri")
                SavedStatusItem(
                    uri = uri,
                    onClick = { onMediaClick(index) },
                    isVideo = !isImage
                )
            }
        }
    }
}
