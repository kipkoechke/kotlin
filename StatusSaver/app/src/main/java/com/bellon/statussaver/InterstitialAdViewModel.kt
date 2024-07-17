package com.bellon.statussaver

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.ads.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InterstitialAdViewModel(application: Application) : AndroidViewModel(application) {

    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded = _isAdLoaded.asStateFlow()

    private var interstitialAd: InterstitialAd? = null

    init {
        loadAd()
    }

    private fun loadAd() {
        interstitialAd = InterstitialAd(getApplication(), "1933718153761687_1933718947094941")

        val interstitialAdListener = object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {}

            override fun onInterstitialDismissed(ad: Ad) {
                loadAd() // Load the next ad
            }

            override fun onError(ad: Ad, adError: AdError) {
                _isAdLoaded.value = false
            }

            override fun onAdLoaded(ad: Ad) {
                _isAdLoaded.value = true
            }

            override fun onAdClicked(ad: Ad) {}

            override fun onLoggingImpression(ad: Ad) {}
        }

        interstitialAd?.loadAd(
            interstitialAd?.buildLoadAdConfig()
                ?.withAdListener(interstitialAdListener)
                ?.build()
        )
    }

    fun showAd() {
        viewModelScope.launch {
            if (_isAdLoaded.value) {
                interstitialAd?.show()
                _isAdLoaded.value = false
            }
        }
    }

    override fun onCleared() {
        interstitialAd?.destroy()
        super.onCleared()
    }
}