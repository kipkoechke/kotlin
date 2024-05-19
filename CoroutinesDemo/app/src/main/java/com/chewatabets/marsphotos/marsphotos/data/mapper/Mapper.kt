package com.chewatabets.coroutinesdemo.marsphotos.data.mapper


import com.chewatabets.coroutinesdemo.marsphotos.domain.models.ApiError
import com.chewatabets.coroutinesdemo.marsphotos.domain.models.NetworkError
import retrofit2.HttpException
import java.io.IOException


fun Throwable.toNetworkError(): NetworkError {
    val error = when (this) {
        is IOException -> ApiError.NetworkError
        is HttpException -> ApiError.UnknownResponse
        else -> ApiError.UnknownError

    }
    return NetworkError(error = error, t = this)
}