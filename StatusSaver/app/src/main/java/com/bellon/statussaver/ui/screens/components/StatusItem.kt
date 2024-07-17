package com.bellon.statussaver.ui.screens.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bellon.statussaver.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun StatusItem(
    uri: Uri,
    onClick: () -> Unit,
    onSave: (Uri) -> Unit,
    isMediaSaved: (Uri) -> Boolean,
    imageLoader: ImageLoader
) {
    var isSaved by remember(uri) { mutableStateOf(isMediaSaved(uri)) }
    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
    var isVideo by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            val mimeType = context.contentResolver.getType(uri)
            isVideo = mimeType?.startsWith("video/") == true
            if (isVideo) {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    thumbnail = retriever.frameAtTime
                } catch (e: Exception) {
                    Log.e("StatusItem", "Error getting video thumbnail", e)
                } finally {
                    retriever.release()
                }
            } else {
                // Preload image
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .size(300, 300)
                    .build()
                imageLoader.enqueue(request)
            }
        }
    }

    Card(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box {
            if (isVideo && thumbnail != null) {
                Image(
                    bitmap = thumbnail!!.asImageBitmap(),
                    contentDescription = "Video Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Media",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (isVideo) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(Color.Transparent, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clickable {
                        if (!isSaved) {
                            onSave(uri)
                            isSaved = true
                        }
                    }
                    .size(32.dp)
                    .padding(bottom = 4.dp, end = 4.dp)
                    .background(
                        color = colorResource(id = R.color.colorPrimaryLight),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = if (!isSaved) colorResource(id = R.color.colorPrimaryLight) else colorResource(id = R.color.colorPrimaryLight),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = if (isSaved) "Saved" else "Download icon",
                    tint = colorResource(id = R.color.white),
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center)
                        .rotate(degrees = if (isSaved) 0f else 90f)

                )
            }
        }
    }
}
