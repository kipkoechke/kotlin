package com.bellon.statussaver.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bellon.statussaver.RepostShareAndSaveItem

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPreview(
    modifier: Modifier = Modifier,
    videoUris: List<Uri>,
    initialPage: Int,
    onDismiss: () -> Unit,
    onSave: ((Uri) -> Unit)? = null,
    onDelete: ((Uri) -> Unit)? = null,
    isMediaSaved: (Uri) -> Boolean
) {
    if (videoUris.isEmpty()) {
        onDismiss()
        return
    }

    val context = LocalContext.current
    var currentUris by remember { mutableStateOf(videoUris) }
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

    // Create a map to cache ExoPlayers
    val exoPlayerCache = remember { mutableMapOf<Uri, ExoPlayer>() }

    // Audio focus management
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val audioFocusRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .build()
                )
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            exoPlayerCache.values.forEach { it.play() }
                        }

                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                        AudioManager.AUDIOFOCUS_LOSS -> {
                            exoPlayerCache.values.forEach { it.pause() }
                        }
                    }
                }
                .build()
        } else {
            null
        }
    }

    // Function to request audio focus
    val requestAudioFocus = remember {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                ) ==
                        AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        }
    }

    // Function to abandon audio focus
    val abandonAudioFocus = remember {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
        }
    }


    // Function to get or create an ExoPlayer for a given URI
    val getOrCreateExoPlayer = remember<(Uri) -> ExoPlayer> {
        { uri ->
            exoPlayerCache.getOrPut(uri) {
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
                    prepare()
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            when (state) {
                                Player.STATE_ENDED -> {
                                    abandonAudioFocus()
                                }
                            }
                        }
                    })
                }
            }
        }
    }

    // Handle page changes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            val current = videoUris[page]
            val player = getOrCreateExoPlayer(current)
            if (requestAudioFocus()) {
                player.playWhenReady = true
            }

            // Pause other players
            exoPlayerCache.values.forEach {
                if (it != player) {
                    it.pause()
                }
            }
        }
    }

    // Clean up ExoPlayers and audio focus when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayerCache.values.forEach { it.release() }
            exoPlayerCache.clear()
            abandonAudioFocus()
        }
    }


    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                modifier = Modifier.clickable { onDismiss() },
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back Arrow"
            )
            Text(text = "Video Preview")
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                val uri = currentUris[page]
                val player = getOrCreateExoPlayer(uri)

                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = true
                            controllerAutoShow = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
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
                            Icons.Default.Check else item.icon, contentDescription = null,
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