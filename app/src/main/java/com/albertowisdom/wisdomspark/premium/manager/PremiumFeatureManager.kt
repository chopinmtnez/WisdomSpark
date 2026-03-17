package com.albertowisdom.wisdomspark.premium.manager

import android.content.Context
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.premium.billing.BillingManager
import com.albertowisdom.wisdomspark.premium.model.PremiumFeature
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para gestionar las funcionalidades Premium y sus restricciones
 */
@Singleton
class PremiumFeatureManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val billingManager: BillingManager,
    private val userPreferences: UserPreferences
) {

    /**
     * Flow que indica si el usuario tiene Premium activo
     */
    val isPremium: Flow<Boolean> = billingManager.subscriptionStatus.map { 
        val hasPremium = billingManager.isPremium() // Usar método que incluye modo testing
        android.util.Log.d("PremiumFeatureManager", "🔄 Estado Premium actualizado: $hasPremium")
        hasPremium
    }

    /**
     * Flow que combina el estado Premium con las preferencias del usuario
     */
    val premiumFeatures: Flow<PremiumFeatureState> = combine(
        billingManager.subscriptionStatus,
        userPreferences.isPremium
    ) { billingStatus, preferencesStatus ->
        val hasPremium = billingManager.isPremium() // Usar método que incluye modo testing
        android.util.Log.d("PremiumFeatureManager", "🔧 Calculando estado premium: billing=${billingStatus.isPremium}, preferences=${preferencesStatus}, final=${hasPremium}")
        
        PremiumFeatureState(
            isPremium = hasPremium,
            activePlan = billingStatus.activePlan,
            availableFeatures = if (hasPremium) {
                PremiumFeature.values().toList()
            } else {
                emptyList()
            }
        )
    }

    /**
     * Verificar si una característica específica está disponible
     */
    fun hasFeature(feature: PremiumFeature): Boolean {
        return billingManager.hasFeature(feature)
    }

    /**
     * Verificar si el usuario puede realizar una acción Premium
     */
    suspend fun canPerformPremiumAction(action: PremiumAction): PremiumActionResult {
        val isPremiumUser = billingManager.isPremium()
        
        return when (action) {
            PremiumAction.REMOVE_ADS -> {
                if (isPremiumUser) {
                    PremiumActionResult.Allowed
                } else {
                    PremiumActionResult.RequiresPremium(
                        title = "Sin Anuncios",
                        description = "Disfruta de WisdomSpark sin interrupciones con Premium",
                        requiredFeature = PremiumFeature.AD_FREE
                    )
                }
            }
            
            PremiumAction.USE_PREMIUM_THEMES -> {
                if (isPremiumUser) {
                    PremiumActionResult.Allowed
                } else {
                    PremiumActionResult.RequiresPremium(
                        title = "Temas Premium",
                        description = "Personaliza tu experiencia con temas exclusivos",
                        requiredFeature = PremiumFeature.PREMIUM_THEMES
                    )
                }
            }
            
            PremiumAction.UNLIMITED_FAVORITES -> {
                if (isPremiumUser) {
                    PremiumActionResult.Allowed
                } else {
                    // Permitir hasta 10 favoritos para usuarios gratuitos
                    PremiumActionResult.Limited(
                        title = "Favoritos Limitados",
                        description = "Los usuarios gratuitos pueden guardar hasta 10 favoritos. Actualiza a Premium para favoritos ilimitados.",
                        currentLimit = 10,
                        requiredFeature = PremiumFeature.UNLIMITED_FAVORITES
                    )
                }
            }
            
            PremiumAction.ACCESS_ADVANCED_CATEGORIES -> {
                if (isPremiumUser) {
                    PremiumActionResult.Allowed
                } else {
                    PremiumActionResult.RequiresPremium(
                        title = "Categorías Avanzadas",
                        description = "Accede a categorías especializadas con Premium",
                        requiredFeature = PremiumFeature.ADVANCED_CATEGORIES
                    )
                }
            }
            
            PremiumAction.CUSTOM_SHARING_STYLES -> {
                if (isPremiumUser) {
                    PremiumActionResult.Allowed
                } else {
                    PremiumActionResult.RequiresPremium(
                        title = "Estilos de Compartir",
                        description = "Comparte con diseños personalizados exclusivos de Premium",
                        requiredFeature = PremiumFeature.QUOTE_SHARING_STYLES
                    )
                }
            }
            
            PremiumAction.OFFLINE_ACCESS -> {
                if (isPremiumUser) {
                    PremiumActionResult.Allowed
                } else {
                    PremiumActionResult.RequiresPremium(
                        title = "Modo Offline",
                        description = "Accede a tus citas sin conexión a internet",
                        requiredFeature = PremiumFeature.OFFLINE_MODE
                    )
                }
            }
        }
    }

    /**
     * Obtener información de uso para límites
     */
    suspend fun getUsageInfo(feature: PremiumFeature): UsageInfo {
        return when (feature) {
            PremiumFeature.UNLIMITED_FAVORITES -> {
                // Aquí obtendrías el número actual de favoritos del usuario
                // Por simplicidad, retornamos un valor de ejemplo
                UsageInfo(
                    current = 0, // Esto vendría de la base de datos
                    limit = if (billingManager.isPremium()) null else 10,
                    isPremium = billingManager.isPremium()
                )
            }
            else -> {
                UsageInfo(
                    current = 0,
                    limit = null,
                    isPremium = billingManager.isPremium()
                )
            }
        }
    }

    /**
     * Obtener mensaje de promoción para una característica
     */
    fun getPromotionMessage(feature: PremiumFeature): String {
        return when (feature) {
            PremiumFeature.AD_FREE -> "✨ ¡Disfruta de WisdomSpark sin anuncios con Premium!"
            PremiumFeature.PREMIUM_THEMES -> "🎨 Personaliza tu experiencia con temas exclusivos"
            PremiumFeature.UNLIMITED_FAVORITES -> "⭐ Guarda todas las citas que quieras sin límites"
            PremiumFeature.ADVANCED_CATEGORIES -> "📚 Explora categorías especializadas y únicas"
            PremiumFeature.QUOTE_SHARING_STYLES -> "📤 Comparte con estilos únicos y personalizados"
            PremiumFeature.OFFLINE_MODE -> "📱 Accede a tus citas favoritas sin conexión"
            PremiumFeature.DAILY_QUOTES_CUSTOMIZATION -> "⏰ Personaliza completamente tus notificaciones"
            PremiumFeature.PRIORITY_SUPPORT -> "🎧 Obtén soporte rápido y personalizado"
        }
    }
}

/**
 * Estado de las características Premium
 */
data class PremiumFeatureState(
    val isPremium: Boolean,
    val activePlan: com.albertowisdom.wisdomspark.premium.model.SubscriptionPlan?,
    val availableFeatures: List<PremiumFeature>
)

/**
 * Acciones Premium que pueden requerir verificación
 */
enum class PremiumAction {
    REMOVE_ADS,
    USE_PREMIUM_THEMES,
    UNLIMITED_FAVORITES,
    ACCESS_ADVANCED_CATEGORIES,
    CUSTOM_SHARING_STYLES,
    OFFLINE_ACCESS
}

/**
 * Resultado de verificación de acción Premium
 */
sealed class PremiumActionResult {
    object Allowed : PremiumActionResult()
    
    data class RequiresPremium(
        val title: String,
        val description: String,
        val requiredFeature: PremiumFeature
    ) : PremiumActionResult()
    
    data class Limited(
        val title: String,
        val description: String,
        val currentLimit: Int,
        val requiredFeature: PremiumFeature
    ) : PremiumActionResult()
}

/**
 * Información de uso de una característica
 */
data class UsageInfo(
    val current: Int,
    val limit: Int?, // null = ilimitado
    val isPremium: Boolean
) {
    val isAtLimit: Boolean get() = limit != null && current >= limit
    val remainingUses: Int? get() = limit?.let { it - current }
}