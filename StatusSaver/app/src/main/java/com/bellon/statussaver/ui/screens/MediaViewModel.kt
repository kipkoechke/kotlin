package com.bellon.statussaver.ui.screens

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bellon.statussaver.data.local.MediaFileDetails
import com.bellon.statussaver.data.local.MediaPreferencesManager
import com.bellon.statussaver.data.local.SavedMediaManager
import com.bellon.statussaver.data.repository.MediaRepositoryImpl
import com.bellon.statussaver.domain.repository.MediaRepository
import com.bellon.statussaver.domain.usecases.DeleteMediaUseCase
import com.bellon.statussaver.domain.usecases.GetSavedMediaFromGalleryUseCase
import com.bellon.statussaver.domain.usecases.GetWhatsAppStatusFilesUseCase
import com.bellon.statussaver.domain.usecases.IsMediaSavedUseCase
import com.bellon.statussaver.domain.usecases.SaveMediaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val getWhatsAppStatusFilesUseCase: GetWhatsAppStatusFilesUseCase,
    private val saveMediaUseCase: SaveMediaUseCase,
    private val isMediaSavedUseCase: IsMediaSavedUseCase,
    private val deleteMediaUseCase: DeleteMediaUseCase,
    private val getSavedMediaFromGalleryUseCase: GetSavedMediaFromGalleryUseCase,
    private val mediaPreferencesManager: MediaPreferencesManager,
    private val savedMediaManager: SavedMediaManager
) : ViewModel() {

    private val _mediaFiles = MutableStateFlow<List<Uri>>(emptyList())
    val mediaFiles: StateFlow<List<Uri>> = _mediaFiles.asStateFlow()

    private val _savedMediaFiles = MutableStateFlow<List<Uri>>(emptyList())
    val savedMediaFiles: StateFlow<List<Uri>> = _savedMediaFiles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var backgroundRefreshJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            refreshSavedMedia()
        }
        loadSavedMediaDetails()
        startBackgroundRefresh()
    }

    fun loadSavedMediaDetails() {
        val savedDetails = mediaPreferencesManager.getMediaDetails()
        _mediaFiles.value = savedDetails.map { Uri.parse(it.uri) }
    }

    private fun startBackgroundRefresh() {
        backgroundRefreshJob?.cancel()
        backgroundRefreshJob = viewModelScope.launch {
            while (isActive) {
                refreshMediaFiles()
                delay(REFRESH_DELAY)
            }
        }
    }

    private suspend fun refreshMediaFiles() {
        try {
            _isLoading.value = true
            val files = getWhatsAppStatusFilesUseCase()
            if (files != _mediaFiles.value) {
                _mediaFiles.value = files
                saveMediaDetails(files)
            }
        } catch (e: Exception) {
            Log.e("MediaViewModel", "Error refreshing media files", e)
        } finally {
            _isLoading.value = false
        }
    }

    private fun saveMediaDetails(files: List<Uri>) {
        mediaPreferencesManager.saveMediaUris(files)
        val mediaDetails = files.map { uri ->
            MediaFileDetails(
                uri = uri.toString(),
                lastModified = System.currentTimeMillis(),
                fileType = if (uri.toString().endsWith(".mp4", true)) "video" else "image"
            )
        }
        mediaPreferencesManager.saveMediaDetails(mediaDetails)
    }

    fun setWhatsAppStatusUri(uri: Uri?) {
        if (mediaRepository is MediaRepositoryImpl) {
            mediaRepository.setWhatsAppStatusUri(uri)
        }
    }

    fun saveMedia(uri: Uri, isVideo: Boolean) {
        viewModelScope.launch {
            if (!isMediaSavedUseCase(uri)) {
                saveMediaUseCase(uri, isVideo)?.let {
                    savedMediaManager.markAsSaved(uri, it)
                    refreshSavedMedia()
                }
            }

        }
    }

    fun isMediaSaved(uri: Uri): Boolean = isMediaSavedUseCase(uri)

    fun deleteMedia(uri: Uri, onDeleteComplete: () -> Unit) {
        viewModelScope.launch {
            if (deleteMediaUseCase(uri)) {
                refreshSavedMedia()
                onDeleteComplete()
            }
        }
    }

    suspend fun refreshSavedMedia() {
        _savedMediaFiles.value = getSavedMediaFromGalleryUseCase()
    }

    override fun onCleared() {
        super.onCleared()
        backgroundRefreshJob?.cancel()
    }

    companion object {
        private const val REFRESH_DELAY = 500L // 500 milliseconds delay between refreshes
    }
}