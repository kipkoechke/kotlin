import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bellon.statussaver.models.MEDIA_TYPE_IMAGE
import com.bellon.statussaver.models.MEDIA_TYPE_VIDEO
import com.bellon.statussaver.models.MediaModel
import com.bellon.statussaver.utils.Constants
import com.bellon.statussaver.utils.SharedPrefKeys
import com.bellon.statussaver.utils.SharedPrefUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StatusViewModel(private val repo: StatusRepo) : ViewModel() {
    private val _whatsAppImagesFlow = MutableStateFlow<List<MediaModel>>(emptyList())
    val whatsAppImagesFlow: StateFlow<List<MediaModel>> = _whatsAppImagesFlow

    private val _whatsAppVideosFlow = MutableStateFlow<List<MediaModel>>(emptyList())
    val whatsAppVideosFlow: StateFlow<List<MediaModel>> = _whatsAppVideosFlow

    private val _whatsAppBusinessImagesFlow = MutableStateFlow<List<MediaModel>>(emptyList())
    val whatsAppBusinessImagesFlow: StateFlow<List<MediaModel>> = _whatsAppBusinessImagesFlow

    private val _whatsAppBusinessVideosFlow = MutableStateFlow<List<MediaModel>>(emptyList())
    val whatsAppBusinessVideosFlow: StateFlow<List<MediaModel>> = _whatsAppBusinessVideosFlow

    private val TAG = "StatusViewModel"

    private var isPermissionsGranted = false

    init {
        SharedPrefUtils.init(repo.context)

        val wpPermissions =
            SharedPrefUtils.getPrefBoolean(SharedPrefKeys.PREF_KEY_WP_PERMISSION_GRANTED, false)
        val wpBusinessPermissions = SharedPrefUtils.getPrefBoolean(
            SharedPrefKeys.PREF_KEY_WP_BUSINESS_PERMISSION_GRANTED,
            false
        )

        isPermissionsGranted = wpPermissions && wpBusinessPermissions
        Log.d(TAG, "Status View Model: isPermissions=> $isPermissionsGranted ")

        if (isPermissionsGranted) {
            Log.d(TAG, "Status View Model: Permissions Already Granted Getting Statuses ")
            getWhatsAppStatuses()
            getWhatsAppBusinessStatuses()
        }
    }

    fun getWhatsAppStatuses() {
        viewModelScope.launch {
            if (!isPermissionsGranted) {
                Log.d(TAG, "getWhatsAppStatuses: Requesting WP Statuses")
                repo.getAllStatuses().collect { statusList ->
                    _whatsAppImagesFlow.value = statusList.filter { it.type == MEDIA_TYPE_IMAGE }
                    _whatsAppVideosFlow.value = statusList.filter { it.type == MEDIA_TYPE_VIDEO }
                }
            }
        }
    }

    fun getWhatsAppBusinessStatuses() {
        viewModelScope.launch {
            if (!isPermissionsGranted) {
                Log.d(TAG, "getWhatsAppStatuses: Requesting WP Business Statuses")
                repo.getAllStatuses(Constants.TYPE_WHATSAPP_BUSINESS).collect { statusList ->
                    _whatsAppBusinessImagesFlow.value =
                        statusList.filter { it.type == MEDIA_TYPE_IMAGE }
                    _whatsAppBusinessVideosFlow.value =
                        statusList.filter { it.type == MEDIA_TYPE_VIDEO }
                }
            }
        }
    }
}

class StatusViewModelFactory(private val repo: StatusRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatusViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}