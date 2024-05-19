package com.chewatabets.coroutinesdemo.marsphotos.domain.repository

import arrow.core.Either
import com.chewatabets.coroutinesdemo.marsphotos.domain.models.MarsPhotosModel
import com.chewatabets.coroutinesdemo.marsphotos.domain.models.NetworkError

interface MarsPhotosRepository {
    suspend fun getMarsPhotos(): Either<NetworkError, List<MarsPhotosModel>>
}