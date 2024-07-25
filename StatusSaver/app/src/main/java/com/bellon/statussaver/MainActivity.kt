@file:OptIn(ExperimentalFoundationApi::class)

package com.bellon.statussaver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.bellon.statussaver.ads.InterstitialAdManager
import com.bellon.statussaver.ui.screens.MediaViewModel
import com.bellon.statussaver.ui.theme.StatusSaverTheme
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity(), LifecycleObserver {
    var whatsAppStatusUri by mutableStateOf<Uri?>(null)
    private val viewModel: MediaViewModel by viewModels()
    private lateinit var mediaUpdateReceiver: BroadcastReceiver
    private lateinit var interstitialAdManager: InterstitialAdManager
    private val PERMISSION_REQUEST_CODE = 1001
    var isAdShowing by mutableStateOf(false)

    private val requestDirectoryAccess =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                handleNewUriPermission(uri)
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestWhatsAppStatusAccess() {
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

    private fun handleNewUriPermission(uri: Uri) {
        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        viewModel.setWhatsAppStatusUri(uri)
        whatsAppStatusUri = uri
        getSharedPreferences("WhatsAppStatus", Context.MODE_PRIVATE).edit()
            .putString("STATUS_URI", uri.toString())
            .apply()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                checkWhatsAppStatusAccess()
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                checkWhatsAppStatusAccess()
            }
        }
    }

    private fun checkWhatsAppStatusAccess() {
        val savedUriString = getSharedPreferences("WhatsAppStatus", Context.MODE_PRIVATE)
            .getString("STATUS_URI", null)

        val isUriPermissionValid = savedUriString?.let {
            checkUriPermission(Uri.parse(it))
        } ?: false

        if (isUriPermissionValid) {
            whatsAppStatusUri = Uri.parse(savedUriString)
            viewModel.setWhatsAppStatusUri(Uri.parse(savedUriString))
            lifecycleScope.launch {
                viewModel.refreshSavedMedia()
            }
        } else {
            // Clear the saved URI as it's no longer valid
            getSharedPreferences("WhatsAppStatus", Context.MODE_PRIVATE).edit().remove("STATUS_URI").apply()
            whatsAppStatusUri = null
            viewModel.setWhatsAppStatusUri(null)
        }
    }

    private fun checkUriPermission(uri: Uri): Boolean {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        return try {
            contentResolver.getPersistedUriPermissions().any { it.uri == uri && it.isReadPermission }
        } catch (e: SecurityException) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()

        // Register a receiver to handle media updates
        mediaUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.bellon.statussaver.ACTION_MEDIA_UPDATED") {
                    lifecycleScope.launch {
                        viewModel.loadSavedMediaDetails()
                    }
                }
            }
        }

        registerReceiver(
            mediaUpdateReceiver, IntentFilter("com.bellon.statussaver.ACTION_MEDIA_UPDATED"),
            RECEIVER_NOT_EXPORTED
        )

        // Initialize Facebook Ads SDK
        AdSettings.addTestDevice("0ddd78bc-ccdb-46e6-bde6-d680721d5ab5");
        AudienceNetworkAds.initialize(this)
        interstitialAdManager = InterstitialAdManager(this)

        lifecycleScope.launch {
            viewModel.savedMediaFiles.collect { savedFiles ->
                Log.d("MainActivity", "Collected ${savedFiles.size} saved media files")
                Log.d("MainActivity", "Saved media URIs: ${savedFiles.joinToString()}")
            }
        }

        setContent {
            val navController = rememberNavController()
            val imageLoader = ImageLoader.Builder(this)
                .components {
                    add(VideoFrameDecoder.Factory())
                }
                .build()
            StatusSaverTheme {
                WhatsAppStatusApp(
                    whatsAppStatusUri = whatsAppStatusUri,
                    onRequestAccess = { requestWhatsAppStatusAccess() },
                    onSaveMedia = { uri, isVideo ->
                        Log.d("MainActivity", "Saving media: $uri, isVideo: $isVideo")
                        viewModel.saveMedia(uri, isVideo)
                    },
                    isMediaSaved = { uri -> viewModel.isMediaSaved(uri) },
                    navController = navController,
                    viewModel = viewModel,
                    imageLoader = imageLoader,
                    isAdShowing = isAdShowing
                )
            }
        }
        // Enqueue periodic work for background updates
        MediaUpdateWorker.enqueuePeriodicWork(this)
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                checkWhatsAppStatusAccess()
            } else {
                // Handle permission denied
                whatsAppStatusUri = null
                viewModel.setWhatsAppStatusUri(null)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mediaUpdateReceiver)
        interstitialAdManager.destroy()
    }
}
val routesWithoutBottomBar = listOf(
    DetailsScreen.ImagePreview.route,
    DetailsScreen.VideoPreview.route
)

enum class DestinationScreen {
    STATUS, SAVED, SETTINGS, IMAGE_PREVIEW, VIDEO_PREVIEW
}

data class RepostShareAndSaveItem(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit = {}
)