package com.bellon.statussaver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun InterstitialAdManager(
    onShowAd: () -> Unit
) {
    val viewModel: InterstitialAdViewModel = viewModel()
    val isAdLoaded by viewModel.isAdLoaded.collectAsState()

    if (isAdLoaded) {
        onShowAd()
        viewModel.showAd()
    }
}