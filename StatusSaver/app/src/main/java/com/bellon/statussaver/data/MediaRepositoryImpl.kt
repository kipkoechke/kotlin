package com.bellon.statussaver.data

import android.net.Uri
import com.bellon.statussaver.MediaPreferencesManager
import com.bellon.statussaver.SavedMediaManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface MediaRepository {
    suspend fun getMediaUris(): Flow<List<Uri>>
    suspend fun saveMediaUris(uris: List<Uri>)
    suspend fun isMediaSaved(uri: Uri): Boolean
    suspend fun markAsSaved(uri: Uri, savedUri: Uri)
    suspend fun getSavedMediaUris(): Flow<List<Uri>>
}

class MediaRepositoryImpl @Inject constructor(
    private val mediaPreferencesManager: MediaPreferencesManager,
    private val savedMediaManager: SavedMediaManager
) : MediaRepository {
    override suspend fun getMediaUris(): Flow<List<Uri>> = flow {
        emit(mediaPreferencesManager.getMediaUris())
    }

    override suspend fun saveMediaUris(uris: List<Uri>) {
        mediaPreferencesManager.saveMediaUris(uris)
    }

    override suspend fun isMediaSaved(uri: Uri): Boolean {
        return savedMediaManager.isMediaSaved(uri)
    }

    override suspend fun markAsSaved(uri: Uri, savedUri: Uri) {
        savedMediaManager.markAsSaved(uri, savedUri)
    }

    override suspend fun getSavedMediaUris(): Flow<List<Uri>> = flow {
        // Implement this method to retrieve saved media URIs
    }
}