package com.bellon.statussaver.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.bellon.statussaver.data.local.MediaPreferencesManager
import com.bellon.statussaver.data.local.SavedMediaManager
import com.bellon.statussaver.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val context: Context,
    private val mediaPreferencesManager: MediaPreferencesManager,
    private val savedMediaManager: SavedMediaManager
) : MediaRepository {

    private var whatsAppStatusUri: Uri? = null

    fun setWhatsAppStatusUri(uri: Uri?) {
        whatsAppStatusUri = uri
    }

    override suspend fun getWhatsAppStatusFiles(): List<Uri> = withContext(Dispatchers.IO) {
        try {
            val uri = whatsAppStatusUri ?: return@withContext emptyList()
            val documentFile = DocumentFile.fromTreeUri(context, uri)
            val files = documentFile?.listFiles()
                ?.filter {
                    it.name?.endsWith(".jpg", true) == true ||
                            it.name?.endsWith(".mp4", true) == true ||
                            it.name?.endsWith(".nomedia", true) == true // Include .nomedia files
                }
                ?.sortedByDescending { it.lastModified() }
                ?.mapNotNull { it.uri }
                ?: emptyList()
            Log.d("MainActivity", "Found ${files.size} WhatsApp status files")
            files
        } catch (e: Exception) {
            Log.e("MainActivity", "Error getting WhatsApp status files", e)
            emptyList()
        }
    }

    override suspend fun saveMedia(uri: Uri, isVideo: Boolean): Uri? = withContext(Dispatchers.IO) {
        return@withContext withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null

                // Get the original filename
                val originalFileName = getOriginalFileName(uri)

                val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (isVideo) MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    else MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, originalFileName)
                    put(
                        MediaStore.MediaColumns.MIME_TYPE,
                        if (isVideo) "video/mp4" else "image/jpeg"
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/Status Saver"
                        )
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    } else {
                        val directory = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "Status Saver"
                        )
                        if (!directory.exists()) {
                            directory.mkdirs()
                        }
                        put(
                            MediaStore.MediaColumns.DATA,
                            File(directory, originalFileName).absolutePath
                        )
                    }
                }

                val outputUri = contentResolver.insert(collection, contentValues)
                outputUri?.let { savedUri ->
                    contentResolver.openOutputStream(savedUri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        contentResolver.update(savedUri, contentValues, null, null)
                    }
                }
                outputUri
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override fun isMediaSaved(uri: Uri): Boolean {
        return savedMediaManager.isMediaSaved(uri)
    }

    override suspend fun deleteMedia(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.delete(uri, null, null)
            true
        } catch (e: Exception) {
            Log.e("MediaRepositoryImpl", "Error deleting media", e)
            false
        }
    }

    override suspend fun getSavedMediaFromGallery(): List<Uri> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<Uri>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.MIME_TYPE
        )
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%Status Saver%")

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val path = cursor.getString(pathColumn)
                val mimeType = cursor.getString(mimeTypeColumn)

                if (mimeType.startsWith("image/") || mimeType.startsWith("video/")) {
                    val contentUri = ContentUris.withAppendedId(collection, id)
                    mediaList.add(contentUri)
                    Log.d("MediaRepository", "Found media: $contentUri, path: $path, mimeType: $mimeType")
                }
            }
        }

        Log.d("MediaRepository", "Total saved media found: ${mediaList.size}")
        mediaList
    }

    private fun getOriginalFileName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    return it.getString(displayNameIndex)
                }
            }
        }
        // Fallback to a generic name if unable to get the original filename
        return "Status_${System.currentTimeMillis()}.${
            if (uri.toString().endsWith(".mp4", true)) "mp4" else "jpg"
        }"
    }

}