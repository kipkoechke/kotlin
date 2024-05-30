package com.bellon.statussaver.utils

import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Build
import android.util.Log

object Constants {

    const val TYPE_WHATSAPP_MAIN = "com.whatsapp"
    const val TYPE_WHATSAPP_BUSINESS = "com.whatsapp.w4b"

    const val MEDIA_TYPE_WHATSAPP_IMAGES = "com.whatsapp.images"
    const val MEDIA_TYPE_WHATSAPP_VIDEOS = "com.whatsapp.videos"

    const val MEDIA_TYPE_WHATSAPP_BUSINESS_IMAGES = "com.whatsapp.w4b.images"
    const val MEDIA_TYPE_WHATSAPP_BUSINESS_VIDEOS = "com.whatsapp.w4b.videos"

    const val MEDIA_LIST_KEY = "MEDIA_LIST"
    const val MEDIA_SCROLL_KEY = "MEDIA_SCROLL"
    const val MEDIA_TYPE_KEY = "MEDIA_TYPE"

    const val FRAGMENT_TYPE_KEY = "TYPE"

    // URIs
    val WHATSAPP_PATH_URI_ANDROID =
        Uri.parse("content://com.android.externalstorage.documents/document/primary%3AWhatsApp%2FMedia%2F.Statuses")

        val WHATSAPP_PATH_URI_ANDROID_11 =
        Uri.parse("content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses")
//    val WHATSAPP_PATH_URI_ANDROID_11 =
//        Uri.parse("/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/.Statuses")


    val WHATSAPP_BUSINESS_PATH_URI_ANDROID =
        Uri.parse("content://com.android.externalstorage.documents/document/primary%3AWhatsApp%20Business%2FMedia%2F.Statuses")
    val WHATSAPP_BUSINESS_PATH_URI_ANDROID_11 =
        Uri.parse("content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp%20Business%2FMedia%2F.Statuses")

    fun getWhatsappUri(): Uri {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WHATSAPP_PATH_URI_ANDROID_11
        } else {
            WHATSAPP_PATH_URI_ANDROID
        }
        Log.d(TAG, "getWhatsappUri: $uri")
        return uri
    }

    fun getWhatsappBusinessUri(): Uri {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WHATSAPP_BUSINESS_PATH_URI_ANDROID_11
        } else {
            WHATSAPP_BUSINESS_PATH_URI_ANDROID
        }
        Log.d(TAG, "getWhatsappBusinessUri: $uri")
        return uri
    }

}