package com.bellon.statussaver

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bellon.statussaver.data.local.MediaFileDetails
import com.bellon.statussaver.data.local.MediaPreferencesManager
import com.bellon.statussaver.domain.usecases.GetWhatsAppStatusFilesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.util.concurrent.TimeUnit

@HiltWorker
class MediaUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val getWhatsAppStatusFilesUseCase: GetWhatsAppStatusFilesUseCase,
    private val mediaPreferencesManager: MediaPreferencesManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val updatedFiles = getWhatsAppStatusFilesUseCase()
            mediaPreferencesManager.saveMediaUris(updatedFiles)

            val mediaDetails = updatedFiles.map { uri ->
                MediaFileDetails(
                    uri = uri.toString(),
                    lastModified = getLastModifiedTime(uri),
                    fileType = getFileType(uri)
                )
            }
            mediaPreferencesManager.saveMediaDetails(mediaDetails)

            Log.d("MediaUpdateWorker", "Updated ${updatedFiles.size} media files")

            val intent = Intent("com.bellon.statussaver.ACTION_MEDIA_UPDATED")
            applicationContext.sendBroadcast(intent)

            Result.success()
        } catch (e: Exception) {
            Log.e("MediaUpdateWorker", "Error updating media files", e)
            Result.failure()
        }
    }

    private fun getLastModifiedTime(uri: Uri): Long {
        return try {
            val file = File(uri.path)
            file.lastModified()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun getFileType(uri: Uri): String {
        return when {
            uri.toString().endsWith(".jpg", true) -> "image"
            uri.toString().endsWith(".mp4", true) -> "video"
            else -> "unknown"
        }
    }

    companion object {
        fun enqueuePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<MediaUpdateWorker>(
                15, TimeUnit.MINUTES
            ).setConstraints(constraints).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "MediaUpdateWork",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}