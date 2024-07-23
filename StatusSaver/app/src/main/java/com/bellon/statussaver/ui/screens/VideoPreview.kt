package com.bellon.statussaver.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bellon.statussaver.RepostShareAndSaveItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    isMediaSaved: (Uri) -> Boolean,
    isInterrupted: Boolean = false
) {
    if (videoUris.isEmpty()) {
        onDismiss()
        return
    }

    val context = LocalContext.current
    var currentUris by remember { mutableStateOf(videoUris) }
    var currentPage by remember { mutableStateOf(initialPage.coerceIn(0, currentUris.size - 1)) }

    val pagerState = rememberPagerState(initialPage = currentPage) { currentUris.size }

    val lifecycleOwner = LocalLifecycleOwner.current
    val isAppInForeground = remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    var showControls by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var autoHideJob by remember { mutableStateOf<Job?>(null) }
    var shouldPlay by remember { mutableStateOf(true) }

    fun showControlsWithTimer() {
        showControls = true
        autoHideJob?.cancel()
        autoHideJob = coroutineScope.launch {
            delay(3000) // Hide controls after 3 seconds
            showControls = false
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> isAppInForeground.value = false
                Lifecycle.Event.ON_START -> isAppInForeground.value = true
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                            exoPlayerCache[currentUris[currentPage]]?.play()
                        }

                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                        AudioManager.AUDIOFOCUS_LOSS -> {
                            exoPlayerCache[currentUris[currentPage]]?.pause()
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
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
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

    // Function to navigate to the next video
    val navigateToNextVideo: () -> Unit = remember {
        {
            val nextPage = (currentPage + 1) % currentUris.size
            coroutineScope.launch {
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    // Function to navigate to the previous video
    val navigateToPreviousVideo: () -> Unit = remember {
        {
            val previousPage = (currentPage - 1 + currentUris.size) % currentUris.size
            coroutineScope.launch {
                pagerState.animateScrollToPage(previousPage)
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
                                    // Play next video
                                    val nextPage = (currentPage + 1) % currentUris.size
                                    if (nextPage != currentPage) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(nextPage)
                                        }
                                    }
                                }
                            }
                        }
                    })
                }
            }
        }
    }

    fun updatePlaybackState(play: Boolean) {
        shouldPlay = play
        val currentPlayer = exoPlayerCache[currentUris[currentPage]]
        if (play) {
            if (requestAudioFocus()) {
                currentPlayer?.play()
            }
        } else {
            currentPlayer?.pause()
            abandonAudioFocus()
        }
    }

    LaunchedEffect(isInterrupted) {
        updatePlaybackState(!isInterrupted && isAppInForeground.value)
    }

    // Handle page changes
    LaunchedEffect(pagerState, isAppInForeground.value, shouldPlay) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            currentPage = page
            val current = currentUris.getOrNull(page) ?: return@collect
            val player = getOrCreateExoPlayer(current)
            updatePlaybackState(isAppInForeground.value && shouldPlay)

            // Pause other players
            exoPlayerCache.values.forEach {
                if (it != player) {
                    it.pause()
                }
            }
        }
    }

    // An effect to pause all players when app goes to background
    LaunchedEffect(isAppInForeground.value) {
        updatePlaybackState(isAppInForeground.value && shouldPlay)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Update every second
            val player = exoPlayerCache[currentUris[currentPage]]
            if (player != null) {
                currentPosition = player.currentPosition
                duration = player.duration
            }
        }
    }

    // Clean up ExoPlayers and audio focus when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayerCache.values.forEach { it.release() }
            exoPlayerCache.clear()
            abandonAudioFocus()
            autoHideJob?.cancel()
        }
    }

    Column(modifier = Modifier.background(Color.Black)) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showControlsWithTimer() }
            ) {
                val uri = currentUris[page]
                val player = getOrCreateExoPlayer(uri)

                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Custom controls overlay
                AnimateView(
                    visible = showControls,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .pointerInput(Unit) {
                                detectTapGestures { showControlsWithTimer() }
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            // Control buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                IconButton(onClick = navigateToPreviousVideo) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Previous video",
                                        tint = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        updatePlaybackState(!shouldPlay)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (shouldPlay) Icons.Default.Place else Icons.Default.PlayArrow,
                                        contentDescription = if (shouldPlay) "Pause" else "Play",
                                        tint = Color.White
                                    )
                                }

                                IconButton(onClick = navigateToNextVideo) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Next video",
                                        tint = Color.White
                                    )
                                }
                            }

                            // Time display
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = formatDuration(currentPosition), color = Color.White)
                                // Progress bar
                                Slider(
                                    value = currentPosition.toFloat(),
                                    onValueChange = { player.seekTo(it.toLong()) },
                                    valueRange = 0f..duration.coerceAtLeast(1).toFloat(),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(text = formatDuration(duration), color = Color.White)
                            }
                        }
                    }
                }
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

// Helper function to format duration
fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = durationMs / (1000 * 60 * 60)

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

@Composable
fun AnimateView(
    modifier: Modifier = Modifier,
    visible: Boolean, // This comes from the mutableState<Boolean>
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = enter,
        exit = exit
    ) {
        content()
    }
}