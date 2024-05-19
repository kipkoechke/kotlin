package com.chewatabets.coroutinesdemo.marsphotos.data.repository

import arrow.core.Either
import com.chewatabets.coroutinesdemo.marsphotos.data.mapper.toNetworkError
import com.chewatabets.coroutinesdemo.marsphotos.data.remote.MarsPhotosApi
import com.chewatabets.coroutinesdemo.marsphotos.domain.models.MarsPhotosModel
import com.chewatabets.coroutinesdemo.marsphotos.domain.models.NetworkError
import com.chewatabets.coroutinesdemo.marsphotos.domain.repository.MarsPhotosRepository
import javax.inject.Inject

class MarsPhotosRepositoryImpl @Inject constructor(private val marsPhotosApi: MarsPhotosApi) :
    MarsPhotosRepository {
    override suspend fun getMarsPhotos(): Either<NetworkError, List<MarsPhotosModel>> {
        return Either.catch { marsPhotosApi.getMarsPhotos() }.mapLeft { it.toNetworkError() }
    }
}