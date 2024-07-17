package com.bellon.statussaver.data.local

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import javax.inject.Inject

class SavedMediaManager @Inject constructor(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("SavedMedia", Context.MODE_PRIVATE)
    private val appContext = context.applicationContext // Store application context for later use

    fun markAsSaved(uri: Uri, savedUri: Uri) {
        sharedPreferences.edit().putString(uri.toString(), savedUri.toString()).apply()
    }

    fun isMediaSaved(uri: Uri): Boolean {
        val savedUriString = sharedPreferences.getString(uri.toString(), null)
        if (savedUriString != null) {
            val savedUri = Uri.parse(savedUriString)
            val exists = doesFileExist(savedUri)
            if (!exists) {
                // If the file doesn't exist, remove it from SharedPreferences
                sharedPreferences.edit().remove(uri.toString()).apply()
            }
            return exists
        }
        return false
    }

    private fun doesFileExist(uri: Uri): Boolean {
        return try {
            val documentFile = DocumentFile.fromSingleUri(appContext, uri)
            documentFile?.exists() == true
        } catch (e: Exception) {
            false
        }
    }
}