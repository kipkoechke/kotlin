import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.bellon.statussaver.models.MEDIA_TYPE_IMAGE
import com.bellon.statussaver.models.MEDIA_TYPE_VIDEO
import com.bellon.statussaver.models.MediaModel
import com.bellon.statussaver.utils.Constants
import com.bellon.statussaver.utils.SharedPrefKeys
import com.bellon.statussaver.utils.SharedPrefUtils
import com.bellon.statussaver.utils.getFileExtension
import com.bellon.statussaver.utils.isStatusExist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow

class StatusRepo(val context: Context) {
    private val _whatsAppStatusesFlow = MutableStateFlow<List<MediaModel>>(emptyList())
    val whatsAppStatusesFlow: StateFlow<List<MediaModel>> = _whatsAppStatusesFlow

    private val _whatsAppBusinessStatusesFlow = MutableStateFlow<List<MediaModel>>(emptyList())
    val whatsAppBusinessStatusesFlow: StateFlow<List<MediaModel>> = _whatsAppBusinessStatusesFlow

    private val activity = context as Activity
    private val TAG = "StatusRepo"

    fun getAllStatuses(whatsAppType: String = Constants.TYPE_WHATSAPP_MAIN): Flow<List<MediaModel>> {
        val treeUri = when (whatsAppType) {
            Constants.TYPE_WHATSAPP_MAIN -> SharedPrefUtils.getPrefString(
                SharedPrefKeys.PREF_KEY_WP_TREE_URI,
                ""
            )?.toUri()

            else -> SharedPrefUtils.getPrefString(SharedPrefKeys.PREF_KEY_WP_BUSINESS_TREE_URI, "")
                ?.toUri()
        } ?: return emptyFlow()

        Log.d(TAG, "getAllStatuses: $treeUri")
        activity.contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val fileDocument = DocumentFile.fromTreeUri(activity, treeUri)
        val tempList = mutableListOf<MediaModel>()

        fileDocument?.listFiles()?.forEach { file ->
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

        return when (whatsAppType) {
            Constants.TYPE_WHATSAPP_MAIN -> {
                _whatsAppStatusesFlow.value = tempList.reversed()
                _whatsAppStatusesFlow.asStateFlow()
            }

            else -> {
                _whatsAppBusinessStatusesFlow.value = tempList.reversed()
                _whatsAppBusinessStatusesFlow.asStateFlow()
            }
        }
    }
}