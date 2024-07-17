package com.bellon.statussaver.data.local

import android.content.Context
import android.net.Uri
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPreferencesManager @Inject constructor(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("MediaPreferences", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveMediaUris(uris: List<Uri>) {
        val uriStrings = uris.map { it.toString() }
        sharedPreferences.edit().putString("media_uris", gson.toJson(uriStrings)).apply()
    }

    fun getMediaUris(): List<Uri> {
        val uriStrings = sharedPreferences.getString("media_uris", null)
        return if (uriStrings != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(uriStrings, type).map { Uri.parse(it) }
        } else {
            emptyList()
        }
    }

    fun saveMediaDetails(mediaDetails: List<MediaFileDetails>) {
        sharedPreferences.edit().putString("media_details", gson.toJson(mediaDetails)).apply()
    }

    fun getMediaDetails(): List<MediaFileDetails> {
        val json = sharedPreferences.getString("media_details", null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<List<MediaFileDetails>>() {}.type)
        } else {
            emptyList()
        }
    }
}

data class MediaFileDetails(
    val uri: String,
    val lastModified: Long,
    val fileType: String
)