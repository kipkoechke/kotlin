package com.bellon.statussaver.di

import android.content.Context
import com.bellon.statussaver.data.local.MediaPreferencesManager
import com.bellon.statussaver.data.local.SavedMediaManager
import com.bellon.statussaver.data.repository.MediaRepositoryImpl
import com.bellon.statussaver.domain.repository.MediaRepository
import com.bellon.statussaver.domain.usecases.DeleteMediaUseCase
import com.bellon.statussaver.domain.usecases.GetSavedMediaFromGalleryUseCase
import com.bellon.statussaver.domain.usecases.GetWhatsAppStatusFilesUseCase
import com.bellon.statussaver.domain.usecases.IsMediaSavedUseCase
import com.bellon.statussaver.domain.usecases.SaveMediaUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideMediaPreferencesManager(context: Context): MediaPreferencesManager =
        MediaPreferencesManager(context)

    @Provides
    @Singleton
    fun provideSavedMediaManager(context: Context): SavedMediaManager =
        SavedMediaManager(context)

    @Provides
    @Singleton
    fun provideMediaRepository(
        context: Context,
        mediaPreferencesManager: MediaPreferencesManager,
        savedMediaManager: SavedMediaManager
    ): MediaRepository = MediaRepositoryImpl(context, mediaPreferencesManager, savedMediaManager)

    @Provides
    @Singleton
    fun provideGetWhatsAppStatusFilesUseCase(repository: MediaRepository): GetWhatsAppStatusFilesUseCase =
        GetWhatsAppStatusFilesUseCase(repository)

    @Provides
    @Singleton
    fun provideSaveMediaUseCase(repository: MediaRepository): SaveMediaUseCase =
        SaveMediaUseCase(repository)

    @Provides
    @Singleton
    fun provideIsMediaSavedUseCase(repository: MediaRepository): IsMediaSavedUseCase =
        IsMediaSavedUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteMediaUseCase(repository: MediaRepository): DeleteMediaUseCase =
        DeleteMediaUseCase(repository)

    @Provides
    @Singleton
    fun provideGetSavedMediaFromGalleryUseCase(repository: MediaRepository): GetSavedMediaFromGalleryUseCase =
        GetSavedMediaFromGalleryUseCase(repository)
}