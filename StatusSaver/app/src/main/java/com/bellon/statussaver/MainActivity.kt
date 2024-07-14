@file:OptIn(ExperimentalFoundationApi::class)

package com.bellon.statussaver

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bellon.statussaver.models.BottomNavigationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MediaPreferencesManager(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("MediaPreferences", Context.MODE_PRIVATE)

    fun saveMediaUris(uris: List<Uri>) {
        val uriStrings = uris.map { it.toString() }
        sharedPreferences.edit().putStringSet("media_uris", uriStrings.toSet()).apply()
    }

    fun getMediaUris(): List<Uri> {
        val uriStrings = sharedPreferences.getStringSet("media_uris", emptySet()) ?: emptySet()
        return uriStrings.map { Uri.parse(it) }
    }
}

class SavedMediaManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("SavedMedia", Context.MODE_PRIVATE)

    fun markAsSaved(uri: Uri, savedUri: Uri) {
        sharedPreferences.edit().putString(uri.toString(), savedUri.toString()).apply()
    }

    fun isMediaSaved(uri: Uri): Boolean {
        val savedUriString = sharedPreferences.getString(uri.toString(), null)
        if (savedUriString != null) {
            val savedUri = Uri.parse(savedUriString)
            val exists = doesFileExist(savedUri)
            if (!exists) {
                // If the file doesn't exist, remove it from SharedPreferences
                sharedPreferences.edit().remove(uri.toString()).apply()
            }
            return exists
        }
        return false
    }

    private fun doesFileExist(uri: Uri): Boolean {
        return try {
            val documentFile = DocumentFile.fromSingleUri(context, uri)
            documentFile?.exists() == true
        } catch (e: Exception) {
            false
        }
    }
}

class MainActivity : ComponentActivity(), LifecycleObserver {
    private var whatsAppStatusUri by mutableStateOf<Uri?>(null)
    private val _mediaFiles = MutableStateFlow<List<Uri>>(emptyList())
    val mediaFiles: StateFlow<List<Uri>> = _mediaFiles.asStateFlow()
    private val viewModel: MediaViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MediaViewModel(applicationContext) as T
            }
        }
    }

    private lateinit var mediaPreferencesManager: MediaPreferencesManager

    private val requestDirectoryAccess =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                whatsAppStatusUri = uri
                getSharedPreferences("WhatsAppStatus", Context.MODE_PRIVATE).edit()
                    .putString("STATUS_URI", uri.toString())
                    .apply()
                refreshStatusesInBackground()
            }
        }

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            if (whatsAppStatusUri != null) {
                refreshStatusesInBackground()
            }
        }
    }

    private fun refreshStatusesInBackground() {
        lifecycleScope.launch(Dispatchers.IO) {
            val updatedFiles = getWhatsAppStatusFiles()
            Log.d("MainActivity", "Refreshed ${updatedFiles.size} WhatsApp status files")
            _mediaFiles.value = updatedFiles
            mediaPreferencesManager.saveMediaUris(updatedFiles)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(lifecycleObserver)

        mediaPreferencesManager = MediaPreferencesManager(this)

        // Load saved media files immediately
        _mediaFiles.value = mediaPreferencesManager.getMediaUris()

        // Retrieve previously granted URI
        val savedUriString = getSharedPreferences("WhatsAppStatus", Context.MODE_PRIVATE)
            .getString("STATUS_URI", null)
        savedUriString?.let {
            whatsAppStatusUri = Uri.parse(it)
            refreshStatusesInBackground()
        }

        lifecycleScope.launch {
            viewModel.savedMediaFiles.collect { savedFiles ->
                Log.d("MainActivity", "Collected ${savedFiles.size} saved media files")
                Log.d("MainActivity", "Saved media URIs: ${savedFiles.joinToString()}")
            }
        }

        setContent {
            val navController = rememberNavController()

            WhatsAppStatusApp(
                whatsAppStatusUri = whatsAppStatusUri,
                onRequestAccess = { requestWhatsAppStatusAccess() },
                mediaFiles = mediaFiles,
                savedMediaFiles = viewModel.savedMediaFiles,
                onSaveMedia = { uri, isVideo ->
                    Log.d("MainActivity", "Saving media: $uri, isVideo: $isVideo")
                    viewModel.saveMedia(uri, isVideo)
                },
                isMediaSaved = { uri -> viewModel.isMediaSaved(uri) },
                navController = navController,
                viewModel = viewModel
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(lifecycleObserver)
    }

    override fun onPause() {
        super.onPause()
        if (whatsAppStatusUri != null) {
            refreshStatusesInBackground()
        }
    }

    override fun onResume() {
        super.onResume()
        if (whatsAppStatusUri != null) {
            refreshStatusesInBackground()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestWhatsAppStatusAccess() {
        val storageManager = application.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
        val targetDir = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
        var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
        var scheme = uri.toString().replace("/root/", "/document/")
        scheme += "%3A$targetDir"
        uri = Uri.parse(scheme)
        intent.putExtra("android.provider.extra.INITIAL_URI", uri)
        requestDirectoryAccess.launch(intent)
    }

    private suspend fun getWhatsAppStatusFiles(): List<Uri> = withContext(Dispatchers.IO) {
        try {
            val uri = whatsAppStatusUri ?: return@withContext emptyList()
            val documentFile = DocumentFile.fromTreeUri(this@MainActivity, uri)
            val files = documentFile?.listFiles()
                ?.filter {
                    it.name?.endsWith(".jpg", true) == true || it.name?.endsWith(
                        ".mp4",
                        true
                    ) == true
                }
                ?.sortedByDescending { it.lastModified() }
                ?.mapNotNull { it.uri }
                ?: emptyList()
            Log.d("MainActivity", "Found ${files.size} WhatsApp status files")
            files
        } catch (e: Exception) {
            Log.e("MainActivity", "Error getting WhatsApp status files", e)
            emptyList()
        }
    }

}

@Composable
fun WhatsAppStatusApp(
    viewModel: MediaViewModel,
    whatsAppStatusUri: Uri?,
    onRequestAccess: () -> Unit,
    mediaFiles: StateFlow<List<Uri>>,
    savedMediaFiles: StateFlow<List<Uri>>,
    onSaveMedia: (Uri, Boolean) -> Unit,
    isMediaSaved: (Uri) -> Boolean,
    navController: NavHostController,

    ) {
    val currentMediaFiles by mediaFiles.collectAsState()
    val currentSavedMediaFiles by savedMediaFiles.collectAsState()
    val context = LocalContext.current
    Scaffold(
        bottomBar = {
            if (whatsAppStatusUri != null) {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
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
                } else if (currentMediaFiles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    StatusScreen(
                        mediaFiles = currentMediaFiles,
                        navController = navController,
                        onSaveMedia = onSaveMedia,
                        isMediaSaved = isMediaSaved
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


enum class DestinationScreen {
    STATUS, SAVED, SETTINGS, IMAGE_PREVIEW, VIDEO_PREVIEW
}

@Composable
fun RequestAccessScreen(onRequestAccess: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onRequestAccess) {
            Text("Request WhatsApp Status Access")
        }
        Text("Please grant access to WhatsApp Status folder")
    }
}

@Composable
fun StatusScreen(
    mediaFiles: List<Uri>,
    navController: NavHostController,
    onSaveMedia: (Uri, Boolean) -> Unit,
    isMediaSaved: (Uri) -> Boolean
) {
    val tabs = listOf("Images", "Videos")
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { tabs.size }, initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    Column {
        Text(
            text = "Status Saver",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
        )
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
                    isMediaSaved = isMediaSaved
                )

                1 -> MediaGallery(
                    mediaFiles = mediaFiles.filter { it.toString().endsWith(".mp4", true) },
                    onMediaClick = { index ->
                        navController.navigate(DetailsScreen.VideoPreview.createRoute(index, true))
                    },
                    onSaveMedia = { uri -> onSaveMedia(uri, true) },
                    isMediaSaved = isMediaSaved
                )
            }
        }
    }
}


@Composable
fun SettingsScreen() {
    Text("Settings Screen")
}

@Composable
fun StatusItem(
    uri: Uri,
    onClick: () -> Unit,
    onSave: (Uri) -> Unit,
    isMediaSaved: (Uri) -> Boolean
) {
    var isSaved by remember(uri) { mutableStateOf(isMediaSaved(uri)) }
    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
    val isVideo = uri.toString().endsWith(".mp4", true)
    val context = LocalContext.current

    LaunchedEffect(uri) {
        if (isVideo) {
            withContext(Dispatchers.IO) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                thumbnail = retriever.frameAtTime
                retriever.release()
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
                        color = colorResource(id = R.color.colorPrimary),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = if (!isSaved) Color.Gray else colorResource(id = R.color.colorPrimary),
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

@Composable
fun MediaGallery(
    mediaFiles: List<Uri>,
    onMediaClick: (Int) -> Unit,
    onSaveMedia: (Uri) -> Unit,
    isMediaSaved: (Uri) -> Boolean
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
                    isMediaSaved = { isMediaSaved(uri) }
                )
            }
        }
    }
}

class MediaViewModel(private val appContext: Context) : ViewModel() {
    private val savedMediaManager = SavedMediaManager(appContext)
    private val _savedMediaFiles = MutableStateFlow<List<Uri>>(emptyList())
    val savedMediaFiles: StateFlow<List<Uri>> = _savedMediaFiles.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            refreshSavedMedia()
        }
    }

    private suspend fun refreshSavedMedia() {
        _savedMediaFiles.value = getSavedMediaFromGallery()
        Log.d("MediaViewModel", "Refreshed saved media. Total: ${_savedMediaFiles.value.size}")
    }

    fun saveMedia(uri: Uri, isVideo: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!isMediaSaved(uri)) {
                val savedUri = saveMediaToGallery(uri, isVideo)
                savedUri?.let {
                    Log.d("MediaViewModel", "Saved new media: $it")
                    savedMediaManager.markAsSaved(uri, it)
                    refreshSavedMedia()
                }
            }
        }
    }

    fun isMediaSaved(uri: Uri): Boolean {
        return savedMediaManager.isMediaSaved(uri)
    }

    fun deleteMedia(uri: Uri, onDeleteComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                appContext.contentResolver.delete(uri, null, null)
                refreshSavedMedia()
                withContext(Dispatchers.Main) {
                    onDeleteComplete()
                }
            } catch (e: Exception) {
                Log.e("MediaViewModel", "Error deleting media", e)
            }
        }
    }

    private suspend fun getSavedMediaFromGallery(): List<Uri> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<Uri>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.MIME_TYPE
        )
        val selection =
            "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? AND (${MediaStore.MediaColumns.MIME_TYPE} LIKE ? OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE ?)"
        val selectionArgs = arrayOf("%Bellon Saver%", "image/%", "video/%")

        val uri = MediaStore.Files.getContentUri("external")

        appContext.contentResolver.query(
            uri, projection, selection, selectionArgs, null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val path = cursor.getString(pathColumn)
                val mimeType = cursor.getString(mimeTypeColumn)
                val contentUri = ContentUris.withAppendedId(uri, id)
                mediaList.add(contentUri)
                Log.d(
                    "MediaViewModel",
                    "Found media: $contentUri, path: $path, mimeType: $mimeType"
                )
            }
        }

        Log.d("MediaViewModel", "Total saved media found: ${mediaList.size}")
        mediaList
    }

    private suspend fun saveMediaToGallery(uri: Uri, isVideo: Boolean): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = appContext.contentResolver
                val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null

                // Get the original filename
                val originalFileName = getOriginalFileName(uri)

                val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (isVideo) MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    else MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, originalFileName)
                    put(
                        MediaStore.MediaColumns.MIME_TYPE,
                        if (isVideo) "video/mp4" else "image/jpeg"
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/Bellon Saver"
                        )
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    } else {
                        val directory = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "Bellon Saver"
                        )
                        if (!directory.exists()) {
                            directory.mkdirs()
                        }
                        put(
                            MediaStore.MediaColumns.DATA,
                            File(directory, originalFileName).absolutePath
                        )
                    }
                }

                val outputUri = contentResolver.insert(collection, contentValues)
                outputUri?.let { savedUri ->
                    contentResolver.openOutputStream(savedUri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        contentResolver.update(savedUri, contentValues, null, null)
                    }
                }
                outputUri
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun getOriginalFileName(uri: Uri): String {
        val cursor = appContext.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    return it.getString(displayNameIndex)
                }
            }
        }
        // Fallback to a generic name if unable to get the original filename
        return "Status_${System.currentTimeMillis()}.${
            if (uri.toString().endsWith(".mp4", true)) "mp4" else "jpg"
        }"
    }
}

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

    if (currentUris.isEmpty()) {
        return
    }

    val currentUri = currentUris[currentPage]
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
                        modifier = Modifier.rotate(degrees = if (isSaved) 0f else 90f)
                    )
                    Text(text = item.title)
                }
            }
        }
    }
}

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

    if (currentUris.isEmpty()) {
        return
    }

    val currentUri = currentUris[currentPage]
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

    // Function to get or create an ExoPlayer for a given URI
    val getOrCreateExoPlayer = remember<(Uri) -> ExoPlayer> {
        { uri ->
            exoPlayerCache.getOrPut(uri) {
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(uri))
                    prepare()
                }
            }
        }
    }

    // Handle page changes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            val current = videoUris[page]
            val player = getOrCreateExoPlayer(current)
            player.playWhenReady = true

            // Pause other players
            exoPlayerCache.values.forEach {
                if (it != player) {
                    it.pause()
                    it.seekTo(0)
                }
            }
        }
    }

    // Clean up ExoPlayers when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayerCache.values.forEach { it.release() }
            exoPlayerCache.clear()
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
                            Icons.Default.Check else item.icon, contentDescription = null
                    )
                    Text(text = item.title)
                }
            }
        }
    }
}


data class RepostShareAndSaveItem(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit = {}
)

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

@Composable
fun SavedMediaGallery(
    mediaFiles: List<Uri>,
    onMediaClick: (Int) -> Unit,
    isImage: Boolean
) {
    if (mediaFiles.isEmpty()) {
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

@Composable
fun SavedStatusItem(uri: Uri, onClick: () -> Unit, isVideo: Boolean) {
    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(uri) {
        if (isVideo) {
            withContext(Dispatchers.IO) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                thumbnail = retriever.frameAtTime
                retriever.release()
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
        }
    }
}
