package com.bellon.statussaver.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun getWhatsAppStatusFiles(): List<Uri>
    suspend fun saveMedia(uri: Uri, isVideo: Boolean): Uri?
    fun isMediaSaved(uri: Uri): Boolean
    suspend fun deleteMedia(uri: Uri): Boolean
    suspend fun getSavedMediaFromGallery(): List<Uri>
}