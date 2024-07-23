package com.bellon.statussaver.ui.screens

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bellon.statussaver.RepostShareAndSaveItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePreview(
    modifier: Modifier = Modifier,
    imageUris: List<Uri>,
    initialPage: Int,
    onDismiss: () -> Unit,
    onSave: ((Uri) -> Unit)? = null,
    onDelete: ((Uri) -> Unit)? = null,
    isMediaSaved: (Uri) -> Boolean
) {
    if (imageUris.isEmpty()) {
        onDismiss()
        return
    }

    val context = LocalContext.current
    var currentUris by remember { mutableStateOf(imageUris) }
    var currentPage by remember { mutableStateOf(initialPage.coerceIn(0, currentUris.size - 1)) }

    val pagerState = rememberPagerState(initialPage = currentPage) { currentUris.size }

    LaunchedEffect(currentUris) {
        if (currentUris.isEmpty()) {
            onDismiss()
        } else if (currentPage >= currentUris.size) {
            currentPage = currentUris.size - 1
            pagerState.scrollToPage(currentPage)
        }
    }

    val currentUri = currentUris.getOrNull(currentPage)
    if (currentUri == null) {
        onDismiss()
        return
    }

    var isSaved by remember(currentUri) { mutableStateOf(isMediaSaved(currentUri)) }

    val items = remember(isSaved, currentUri) {
        listOf(
            RepostShareAndSaveItem(Icons.Default.Favorite, "Repost"),
            RepostShareAndSaveItem(Icons.Default.Share, "Share"),
            if (onDelete != null)
                RepostShareAndSaveItem(
                    Icons.Default.Delete,
                    "Delete",
                    onClick = {
                        onDelete(currentUri)
                        currentUris = currentUris.filter { it != currentUri }
                        if (currentUris.isEmpty()) {
                            onDismiss()
                        } else {
                            currentPage = currentPage.coerceAtMost(currentUris.size - 1)
                        }
                    }
                )
            else
                RepostShareAndSaveItem(
                    if (isSaved) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    if (isSaved) "Saved" else "Save",
                    onClick = {
                        if (onSave != null && !isSaved) {
                            onSave(currentUri)
                            isSaved = true
                        }
                    }
                )
        )
    }

    Column(modifier = Modifier.background(Color.Black)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
               ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                modifier = Modifier.clickable { onDismiss() },
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back Arrow"
            )
            Text(text = "Status Saver 💯")
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) { page ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(currentUris[page])
                    .crossfade(true)
                    .build(),
                contentDescription = "Full-screen image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                currentPage = page
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth()
        ) {
            items.forEachIndexed { index, item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable(
                            enabled = !(item.title == "Save" && isSaved)
                        ) { item.onClick() }
                ) {
                    Icon(
                        imageVector = if (item.title == "Save" && isSaved)
                            Icons.Default.Check else item.icon,
                        contentDescription = null,
                        modifier = if (item.title == "Save" && !isSaved) {
                            Modifier.rotate(90f)
                        } else {
                            Modifier
                        }
                    )
                    Text(text = item.title)
                }
            }
        }
    }
}