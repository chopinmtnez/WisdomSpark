package com.albertowisdom.wisdomspark.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
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
        val REWARDED_AD_UNIT_ID: String get() = BuildConfig.ADMOB_REWARDED_ID

        const val INTERSTITIAL_FREQUENCY = 2
        private const val INTERSTITIAL_COOLDOWN_MS = 30_000L
    }

    private var isInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewarded = false
    private var interactionCount = 0
    private var lastInterstitialShownAt = 0L
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
                    loadRewardedAd(context)
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

        val now = System.currentTimeMillis()
        val cooldownOk = (now - lastInterstitialShownAt) >= INTERSTITIAL_COOLDOWN_MS
        val shouldShow = force || (interactionCount % INTERSTITIAL_FREQUENCY == 0)

        if (shouldShow && cooldownOk && interstitialAd != null) {
            try {
                interstitialAd?.show(activity)
                lastInterstitialShownAt = now
                Log.d("AdMob", "Interstitial mostrado (interaccion #$interactionCount)")
            } catch (e: Exception) {
                Log.e("AdMob", "Error mostrando interstitial: ${e.message}")
                loadInterstitialAd(activity)
            }
        } else if (shouldShow && !cooldownOk) {
            Log.d("AdMob", "Interstitial en cooldown, se omite")
        } else if (shouldShow) {
            Log.d("AdMob", "Interstitial no disponible, precargando...")
            loadInterstitialAd(activity)
        }
    }

    // ========== REWARDED ADS ==========

    /**
     * Cargar anuncio con recompensa
     */
    private fun loadRewardedAd(context: Context) {
        if (isPremiumUser || isLoadingRewarded) return

        isLoadingRewarded = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadingRewarded = false
                    Log.d("AdMob", "✅ Rewarded ad cargado")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoadingRewarded = false
                    Log.e("AdMob", "❌ Error cargando rewarded: ${error.message}")
                }
            }
        )
    }

    /**
     * Verificar si hay un rewarded ad listo para mostrar
     */
    fun hasRewardedAd(): Boolean = rewardedAd != null

    /**
     * Mostrar anuncio con recompensa.
     * @param activity Activity para mostrar el ad
     * @param onRewarded Callback que se ejecuta cuando el usuario completa el visionado
     * @param onFailed Callback si el ad no se pudo mostrar
     */
    fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onFailed: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad == null) {
            Log.d("AdMob", "Rewarded ad no disponible")
            onFailed()
            // Intentar precargar para la próxima vez
            loadRewardedAd(activity)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                // Precargar siguiente rewarded ad
                loadRewardedAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                Log.e("AdMob", "❌ Error mostrando rewarded: ${error.message}")
                onFailed()
                loadRewardedAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d("AdMob", "📱 Rewarded ad mostrado")
            }
        }

        ad.show(activity) { rewardItem ->
            Log.d("AdMob", "🎁 Recompensa obtenida: ${rewardItem.amount} ${rewardItem.type}")
            onRewarded()
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
            hasInterstitialLoaded = interstitialAd != null,
            hasRewardedLoaded = rewardedAd != null
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
    val hasInterstitialLoaded: Boolean,
    val hasRewardedLoaded: Boolean = false
)
