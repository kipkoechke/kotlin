package com.chewatabets.coroutinesdemo.di

import com.chewatabets.coroutinesdemo.marsphotos.data.repository.MarsPhotosRepositoryImpl
import com.chewatabets.coroutinesdemo.marsphotos.domain.repository.MarsPhotosRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindsMarsPhotosRepository(impl: MarsPhotosRepositoryImpl): MarsPhotosRepository
}