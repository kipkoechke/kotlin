package com.bellon.statussaver.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import com.bellon.statussaver.models.MEDIA_TYPE_IMAGE
import com.bellon.statussaver.models.MEDIA_TYPE_VIDEO
import com.bellon.statussaver.models.MediaModel
import com.bellon.statussaver.utils.Constants
import com.bellon.statussaver.utils.SharedPrefKeys
import com.bellon.statussaver.utils.SharedPrefUtils
import com.bellon.statussaver.utils.getFileExtension
import com.bellon.statussaver.utils.isStatusExist

class StatusRepo(val context: Context) {
    val whatsAppStatusesLiveData = MutableLiveData<ArrayList<MediaModel>>()
    val whatsAppBusinessStatusesLiveData = MutableLiveData<ArrayList<MediaModel>>()
    val activity = context as Activity
    private val wpStatusesList = ArrayList<MediaModel>()
    private val wpBusinessStatusesList = ArrayList<MediaModel>()
    private val TAG = "StatusRepo"

    fun getAllStatuses(whatsAppType: String = Constants.TYPE_WHATSAPP_MAIN) {
        val treeUri = when (whatsAppType) {
            Constants.TYPE_WHATSAPP_MAIN -> {
                SharedPrefUtils.getPrefString(SharedPrefKeys.PREF_KEY_WP_TREE_URI, "")?.toUri()!!
            }

            else -> {
                SharedPrefUtils.getPrefString(SharedPrefKeys.PREF_KEY_WP_BUSINESS_TREE_URI, "")
                    ?.toUri()!!
            }
        }
//        if(treeUri.toString().isEmpty() || treeUri.toString() == "null") {
//            return;
//        }
        Log.d(TAG, "getAllStatuses: $treeUri")
        activity.contentResolver.takePersistableUriPermission(
            treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        val fileDocument = DocumentFile.fromTreeUri(activity, treeUri)
        val tempList = ArrayList<MediaModel>()
        fileDocument?.let {
            it.listFiles().forEach { file ->
                Log.d(TAG, "getAllStatuses: ${file.name}")
                if (file.name != ".nomedia" && file.isFile) {
                    file.uri?.let { uri ->
                        file.name?.let { name ->
                            val type = if (getFileExtension(name) == "mp4") {
                                MEDIA_TYPE_VIDEO
                            } else {
                                MEDIA_TYPE_IMAGE
                            }
                            val isDownloaded = context.isStatusExist(name)
                            val model = MediaModel(
                                pathUri = uri.toString(),
                                fileName = name,
                                type = type,
                                isDownloaded = isDownloaded
                            )
                            tempList.add(model)
                        }
                    }
                }
            }
        }
        when (whatsAppType) {
            Constants.TYPE_WHATSAPP_MAIN -> {
                wpStatusesList.clear()
                tempList.reversed().forEach { wpStatusesList.add(it) }
                Log.d(TAG, "getAllStatuses: Pushing Value to Wp live Data")
                whatsAppStatusesLiveData.postValue(wpStatusesList)
            }

            else -> {
                wpBusinessStatusesList.clear()
                tempList.reversed().forEach { wpBusinessStatusesList.add(it) }
                Log.d(TAG, "getAllStatuses: Pushing Value to Wp Business live Data")
                whatsAppBusinessStatusesLiveData.postValue(wpBusinessStatusesList)
            }
        }
    }
}



