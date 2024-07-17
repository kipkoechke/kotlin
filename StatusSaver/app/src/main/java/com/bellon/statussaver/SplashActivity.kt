package com.bellon.statussaver

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bellon.statussaver.data.local.MediaPreferencesManager
import com.bellon.statussaver.data.local.SavedMediaManager
import com.bellon.statussaver.ui.theme.StatusSaverTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StatusSaverTheme {
                SplashScreen(
                    viewModel = viewModel,
                    onDataLoaded = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SplashScreen(onDataLoaded: () -> Unit, viewModel: SplashViewModel) {
    val dataLoaded by viewModel.dataLoaded.collectAsState()

    LaunchedEffect(dataLoaded) {
        if (dataLoaded) {
            onDataLoaded()
        }
    }

    // Your splash screen UI
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Status Saver", fontSize = 24.sp)
    }
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val mediaPreferencesManager: MediaPreferencesManager,
    private val savedMediaManager: SavedMediaManager
) : ViewModel() {
    private val _dataLoaded = MutableStateFlow(false)
    val dataLoaded: StateFlow<Boolean> = _dataLoaded.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            // Load saved media URIs
            val savedMediaUris = mediaPreferencesManager.getMediaUris()

            // Verify if the saved media still exists
            val existingMediaUris = savedMediaUris.filter { savedMediaManager.isMediaSaved(it) }

            // Update the preferences with existing media
            mediaPreferencesManager.saveMediaUris(existingMediaUris)

            _dataLoaded.value = true
        }
    }
}