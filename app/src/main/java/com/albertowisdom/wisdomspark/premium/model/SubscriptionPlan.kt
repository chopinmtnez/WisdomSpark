package com.albertowisdom.wisdomspark.premium.model

/**
 * Planes de suscripción disponibles
 */
enum class SubscriptionPlan(
    val productId: String,
    val nameKey: String,
    val descriptionKey: String,
    val features: List<PremiumFeature>,
    val isPopular: Boolean = false,
    val isOneTimePurchase: Boolean = false
) {
    MONTHLY(
        productId = "wisdomspark_premium_monthly",
        nameKey = "subscription_monthly",
        descriptionKey = "subscription_monthly_desc",
        features = PremiumFeature.entries
    ),
    YEARLY(
        productId = "wisdomspark_premium_yearly",
        nameKey = "subscription_yearly",
        descriptionKey = "subscription_yearly_desc",
        features = PremiumFeature.entries,
        isPopular = true
    ),
    LIFETIME(
        productId = "wisdomspark_premium_lifetime",
        nameKey = "subscription_lifetime",
        descriptionKey = "subscription_lifetime_desc",
        features = PremiumFeature.entries,
        isOneTimePurchase = true // INAPP, no SUBS
    )
}

/**
 * Estado actual de la suscripción del usuario
 */
data class SubscriptionStatus(
    val isPremium: Boolean = false,
    val activePlan: SubscriptionPlan? = null,
    val purchaseTime: Long? = null,
    val expiryTime: Long? = null,
    val isAutoRenewing: Boolean = false,
    val purchaseToken: String? = null
)

/**
 * Estado de una compra
 */
sealed class PurchaseState {
    /** Estado inicial: esperando conexión con Google Play Billing */
    object Loading : PurchaseState()
    /** Conectado y listo para compras, sin operación en curso */
    object Idle : PurchaseState()
    /** Compra completada con éxito */
    object Success : PurchaseState()
    /** Error en la conexión o la compra */
    data class Error(val message: String) : PurchaseState()
    /** Compra cancelada por el usuario */
    object Cancelled : PurchaseState()
    /** Compra pendiente de confirmación (ej: pago lento) */
    object Pending : PurchaseState()
}