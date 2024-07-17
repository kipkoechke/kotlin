package com.bellon.statussaver.ui.screens.components

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.ImageLoader

@Composable
fun MediaGallery(
    mediaFiles: List<Uri>,
    onMediaClick: (Int) -> Unit,
    onSaveMedia: (Uri) -> Unit,
    isMediaSaved: (Uri) -> Boolean,
    imageLoader: ImageLoader
) {
    if (mediaFiles.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No media files found")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(mediaFiles) { index, uri ->
                StatusItem(
                    uri = uri,
                    onClick = { onMediaClick(index) },
                    onSave = onSaveMedia,
                    isMediaSaved = { isMediaSaved(uri) },
                    imageLoader = imageLoader
                )
            }
        }
    }
}
