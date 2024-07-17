package com.bellon.statussaver.domain.usecases

import android.net.Uri
import com.bellon.statussaver.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWhatsAppStatusFilesUseCase @Inject constructor(private val repository: MediaRepository) {
    suspend operator fun invoke() = repository.getWhatsAppStatusFiles()
}

class SaveMediaUseCase @Inject constructor(private val repository: MediaRepository) {
    suspend operator fun invoke(uri: Uri, isVideo: Boolean) = repository.saveMedia(uri, isVideo)
}

class IsMediaSavedUseCase @Inject constructor(private val repository: MediaRepository) {
    operator fun invoke(uri: Uri) = repository.isMediaSaved(uri)
}

class DeleteMediaUseCase @Inject constructor(private val repository: MediaRepository) {
    suspend operator fun invoke(uri: Uri) = repository.deleteMedia(uri)
}

class GetSavedMediaFromGalleryUseCase @Inject constructor(private val repository: MediaRepository) {
    suspend operator fun invoke() = repository.getSavedMediaFromGallery()
}