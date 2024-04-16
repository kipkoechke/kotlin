package com.example.architecturesample.store.presentations.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.architecturesample.util.EventBus
import kotlinx.coroutines.launch

fun ViewModel.sendEvent(event: Any) {
    viewModelScope.launch { EventBus.sendEvent(event) }
}