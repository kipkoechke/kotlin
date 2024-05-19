package com.chewatabets.coroutinesdemo.marsphotos.presentations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chewatabets.coroutinesdemo.marsphotos.domain.repository.MarsPhotosRepository
import com.chewatabets.coroutinesdemo.marsphotos.presentations.util.sendEvent
import com.chewatabets.coroutinesdemo.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarsPhotosViewModel @Inject constructor(
    private val marsPhotosRepository: MarsPhotosRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MarsPhotoViewState())
    val state: StateFlow<MarsPhotoViewState> = _state.asStateFlow()

    init {
        getPhotos()
    }

    private fun getPhotos() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            marsPhotosRepository.getMarsPhotos()
                .onRight { photos ->
                    _state.update { it.copy(photos = photos) }
                }
                .onLeft { error ->
                    _state.update { it.copy(error = error.error.message) }
                    sendEvent(Event.Toast(error.error.message))
                }
            _state.update { it.copy(isLoading = false) }
        }
    }
}