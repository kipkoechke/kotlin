package com.chewatabets.coroutinesdemo.marsphotos.presentations

import com.chewatabets.coroutinesdemo.marsphotos.domain.models.MarsPhotosModel

data class MarsPhotoViewState(
    val isLoading: Boolean = false,
    val photos: List<MarsPhotosModel> = emptyList(),
    val error: String? = null
)
