package com.albertowisdom.wisdomspark.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor centralizado de AdMob para WisdomSpark
 * Maneja banners, intersticiales y lógica de monetización
 */
@Singleton
class AdMobManager @Inject constructor() {

    companion object {
        // Test Ad Unit IDs - Reemplazar con IDs reales en producción
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        
        // Configuración de frecuencia
        const val INTERSTITIAL_FREQUENCY = 3 // Mostrar cada 3 interacciones
    }

    private var isInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var interactionCount = 0
    
    // Estado premium (para testing o compras in-app)
    var isPremiumUser = false
        private set

    /**
     * Inicializar AdMob SDK
     */
    fun initialize(context: Context) {
        if (isInitialized) return

        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d("AdMob", "✅ AdMob inicializado: ${initializationStatus.adapterStatusMap}")
                isInitialized = true
                
                // Precargar primer interstitial
                loadInterstitialAd(context)
            }
        } catch (e: Exception) {
            Log.e("AdMob", "❌ Error inicializando AdMob: ${e.message}")
        }
    }

    /**
     * Verificar si se deben mostrar anuncios
     */
    fun shouldShowAds(): Boolean = !isPremiumUser

    /**
     * Cargar anuncio intersticial
     */
    private fun loadInterstitialAd(context: Context) {
        if (isPremiumUser) return

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d("AdMob", "✅ Interstitial cargado")
                    
                    // Configurar callback para cuando se cierre
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            // Precargar siguiente
                            loadInterstitialAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            interstitialAd = null
                            Log.e("AdMob", "❌ Error mostrando interstitial: ${error.message}")
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.e("AdMob", "❌ Error cargando interstitial: ${error.message}")
                }
            }
        )
    }

    /**
     * Mostrar anuncio intersticial
     */
    fun showInterstitialAd(activity: Activity, force: Boolean = false) {
        if (isPremiumUser) return

        interactionCount++
        
        val shouldShow = force || (interactionCount % INTERSTITIAL_FREQUENCY == 0)
        
        if (shouldShow && interstitialAd != null) {
            interstitialAd?.show(activity)
            Log.d("AdMob", "📱 Interstitial mostrado")
        } else if (shouldShow) {
            Log.d("AdMob", "⚠️ Interstitial no disponible, precargando...")
            loadInterstitialAd(activity)
        }
    }

    /**
     * Callbacks para banner ads
     */
    fun onBannerAdLoaded() {
        Log.d("AdMob", "✅ Banner ad cargado")
    }

    fun onBannerAdFailedToLoad(error: LoadAdError) {
        Log.e("AdMob", "❌ Error cargando banner: ${error.message}")
    }

    /**
     * Activar modo premium
     */
    fun activatePremium() {
        isPremiumUser = true
        Log.d("AdMob", "💎 Modo premium activado - Anuncios deshabilitados")
    }

    /**
     * Desactivar modo premium (para testing)
     */
    fun deactivatePremium() {
        isPremiumUser = false
        Log.d("AdMob", "📱 Modo premium desactivado - Anuncios habilitados")
    }

    /**
     * Reset contador de interacciones
     */
    fun resetInteractionCount() {
        interactionCount = 0
    }

    /**
     * Obtener estadísticas
     */
    fun getStats(): AdMobStats {
        return AdMobStats(
            isInitialized = isInitialized,
            isPremium = isPremiumUser,
            interactionCount = interactionCount,
            hasInterstitialLoaded = interstitialAd != null
        )
    }
}

/**
 * Estadísticas de AdMob para debug/analytics
 */
data class AdMobStats(
    val isInitialized: Boolean,
    val isPremium: Boolean,
    val interactionCount: Int,
    val hasInterstitialLoaded: Boolean
)
