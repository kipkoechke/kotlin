package com.bellon.statussaver.di

import android.content.Context
import com.bellon.statussaver.MediaPreferencesManager
import com.bellon.statussaver.data.MediaRepository
import com.bellon.statussaver.data.MediaRepositoryImpl
import com.bellon.statussaver.SavedMediaManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideMediaPreferencesManager(@ApplicationContext context: Context): MediaPreferencesManager {
        return MediaPreferencesManager(context)
    }

    @Provides
    fun provideSavedMediaManager(@ApplicationContext context: Context): SavedMediaManager {
        return SavedMediaManager(context)
    }

    @Provides
    fun provideMediaRepository(
        mediaPreferencesManager: MediaPreferencesManager,
        savedMediaManager: SavedMediaManager
    ): MediaRepository {
        return MediaRepositoryImpl(mediaPreferencesManager, savedMediaManager)
    }
}