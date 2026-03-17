package com.albertowisdom.wisdomspark.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.albertowisdom.wisdomspark.BuildConfig
import com.albertowisdom.wisdomspark.premium.billing.BillingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor centralizado de AdMob para WisdomSpark
 * Maneja banners, intersticiales y lógica de monetización
 */
@Singleton
class AdMobManager @Inject constructor(
    private val billingManager: BillingManager
) {

    companion object {
        val BANNER_AD_UNIT_ID: String get() = BuildConfig.ADMOB_BANNER_ID
        val INTERSTITIAL_AD_UNIT_ID: String get() = BuildConfig.ADMOB_INTERSTITIAL_ID

        const val INTERSTITIAL_FREQUENCY = 3
    }

    private var isInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var interactionCount = 0
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Estado premium se obtiene del BillingManager
    private var isPremiumUser = false

    /**
     * Inicializar AdMob SDK
     */
    fun initialize(context: Context) {
        if (isInitialized) return

        // Observar estado Premium (incluyendo modo testing)
        scope.launch {
            billingManager.subscriptionStatus.collect { status ->
                val newPremiumStatus = billingManager.isPremium() // Usar método que incluye testing
                if (isPremiumUser != newPremiumStatus) {
                    isPremiumUser = newPremiumStatus
                    Log.d("AdMob", "Estado Premium actualizado: $isPremiumUser")
                    
                    // Si cambia de no-premium a premium, cancelar anuncios
                    if (isPremiumUser) {
                        interstitialAd = null
                        Log.d("AdMob", "🚫 Anuncios deshabilitados - Usuario Premium activo")
                    }
                }
            }
        }

        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d("AdMob", "✅ AdMob inicializado: ${initializationStatus.adapterStatusMap}")
                isInitialized = true
                
                // Solo precargar si no es Premium
                if (!isPremiumUser) {
                    loadInterstitialAd(context)
                }
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
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d("AdMob", "📱 Interstitial mostrado en pantalla completa")
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
            try {
                interstitialAd?.show(activity)
                Log.d("AdMob", "📱 Interstitial mostrado")
            } catch (e: Exception) {
                Log.e("AdMob", "❌ Error mostrando interstitial: ${e.message}")
                // Intentar recargar el anuncio
                loadInterstitialAd(activity)
            }
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
     * Verificar si el usuario es Premium
     */
    fun isPremium(): Boolean = isPremiumUser

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
