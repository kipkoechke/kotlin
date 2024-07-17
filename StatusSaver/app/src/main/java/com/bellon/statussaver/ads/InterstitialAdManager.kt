package com.bellon.statussaver.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener

class InterstitialAdManager(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private val handler = Handler(Looper.getMainLooper())
    private val adDisplayDelay = 1 * 60 * 1000L // 2 minutes in milliseconds

    init {
        loadAd()
        scheduleAdDisplay()
    }

    private fun loadAd() {
        interstitialAd = InterstitialAd(context, "1933718153761687_1933718947094941")
        val interstitialAdListener = object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {
                Log.d(TAG, "Interstitial ad displayed.")
            }

            override fun onInterstitialDismissed(ad: Ad) {
                Log.d(TAG, "Interstitial ad dismissed.")
                loadAd() // Reload the ad for next time
            }

            override fun onError(ad: Ad, adError: AdError) {
                Log.e(TAG, "Interstitial ad failed to load: ${adError.errorMessage}")
            }

            override fun onAdLoaded(ad: Ad) {
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!")
            }

            override fun onAdClicked(ad: Ad) {
                Log.d(TAG, "Interstitial ad clicked!")
            }

            override fun onLoggingImpression(ad: Ad) {
                Log.d(TAG, "Interstitial ad impression logged!")
            }
        }

        interstitialAd?.loadAd(
            interstitialAd?.buildLoadAdConfig()
                ?.withAdListener(interstitialAdListener)
                ?.build()
        )
    }

    private fun scheduleAdDisplay() {
        handler.postDelayed({
            showAd()
        }, adDisplayDelay)
    }

    fun showAd() {
        interstitialAd?.let { ad ->
            if (ad.isAdLoaded && !ad.isAdInvalidated) {
                ad.show()
            } else {
                Log.d(TAG, "Interstitial ad not ready to show.")
                loadAd() // Try to load a new ad
            }
        }
    }

    fun destroy() {
        handler.removeCallbacksAndMessages(null)
        interstitialAd?.destroy()
    }

    companion object {
        private const val TAG = "InterstitialAdManager"
    }
}
