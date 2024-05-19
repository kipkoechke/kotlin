package com.chewatabets.coroutinesdemo.marsphotos.presentations.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chewatabets.coroutinesdemo.util.EventBus
import kotlinx.coroutines.launch

fun ViewModel.sendEvent(event: Any) {
    viewModelScope.launch { EventBus.sendEvent(event) }
}