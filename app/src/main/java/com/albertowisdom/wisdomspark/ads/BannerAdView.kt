package com.albertowisdom.wisdomspark.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.albertowisdom.wisdomspark.presentation.ui.theme.WisdomBeige
import com.albertowisdom.wisdomspark.presentation.ui.theme.WisdomChampagne

/**
 * Banner Ad Composable integrado con el diseño premium de WisdomSpark
 */
@Composable
fun BannerAdView(
    adUnitId: String = AdMobManager.BANNER_AD_UNIT_ID,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: (LoadAdError) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    
    // Calcular tamaño adaptativo
    val screenWidthDp = configuration.screenWidthDp
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = WisdomBeige.copy(alpha = 0.3f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    // Configurar tamaño adaptativo
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        context, 
                        screenWidthDp - 16 // Restar padding
                    ))
                    
                    this.adUnitId = adUnitId
                    setBackgroundColor(WisdomChampagne.copy(alpha = 0.1f).toArgb())
                    
                    // Configurar listeners
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            onAdLoaded()
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            super.onAdFailedToLoad(error)
                            onAdFailedToLoad(error)
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                        }

                        override fun onAdClosed() {
                            super.onAdClosed()
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                        }

                        override fun onAdOpened() {
                            super.onAdOpened()
                        }
                    }
                    
                    // Cargar anuncio
                    loadAd(AdRequest.Builder().build())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    }
}

/**
 * Banner Ad con diseño glassmorphic premium
 */
@Composable
fun PremiumBannerAdView(
    adUnitId: String = AdMobManager.BANNER_AD_UNIT_ID,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: (LoadAdError) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = WisdomBeige.copy(alpha = 0.2f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        context, 
                        screenWidthDp - 24
                    ))
                    
                    this.adUnitId = adUnitId
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            onAdLoaded()
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            super.onAdFailedToLoad(error)
                            onAdFailedToLoad(error)
                        }
                    }
                    
                    loadAd(AdRequest.Builder().build())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    }
}
