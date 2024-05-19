package com.chewatabets.coroutinesdemo.marsphotos.data.remote

import com.chewatabets.coroutinesdemo.marsphotos.domain.models.MarsPhotosModel
import retrofit2.http.GET

interface MarsPhotosApi {
    @GET("photos")
    suspend fun getMarsPhotos(): List<MarsPhotosModel>
}