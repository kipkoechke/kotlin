package com.bellon.statussaver.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.facebook.ads.AdSize
import com.facebook.ads.AdView

@Composable
fun FacebookBannerAd(
    placementId: String,
    modifier: Modifier = Modifier
) {
    var adView: AdView? by remember { mutableStateOf(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context, placementId, AdSize.BANNER_HEIGHT_50).also {
                adView = it
                it.loadAd()
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            adView?.destroy()
        }
    }
}
