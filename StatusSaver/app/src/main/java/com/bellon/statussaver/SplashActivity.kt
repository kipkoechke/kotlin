package com.bellon.statussaver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bellon.statussaver.data.local.MediaPreferencesManager
import com.bellon.statussaver.data.local.SavedMediaManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SplashViewModel @Inject constructor(
    private val mediaPreferencesManager: MediaPreferencesManager,
    private val savedMediaManager: SavedMediaManager
) : ViewModel() {
    private val _dataLoaded = MutableStateFlow(false)
    val dataLoaded = _dataLoaded.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Load saved media URIs
            val savedMediaUris = mediaPreferencesManager.getMediaUris()

            // Verify if the saved media still exists
            val existingMediaUris = savedMediaUris.filter {
                savedMediaManager.isMediaSaved(it)
            }

            // Update the preferences with existing media
            mediaPreferencesManager.saveMediaUris(existingMediaUris)

            _dataLoaded.value = true
        }
    }
}