package com.chewatabets.coroutinesdemo.di

import com.chewatabets.coroutinesdemo.marsphotos.data.remote.MarsPhotosApi
import com.chewatabets.coroutinesdemo.util.Constant.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providesMarsPhotoApi(): MarsPhotosApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarsPhotosApi::class.java)
    }
}