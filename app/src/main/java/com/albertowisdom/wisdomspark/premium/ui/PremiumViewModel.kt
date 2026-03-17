package com.albertowisdom.wisdomspark.premium.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.premium.billing.BillingManager
import com.albertowisdom.wisdomspark.premium.model.PremiumFeature
import com.albertowisdom.wisdomspark.premium.model.PurchaseState
import com.albertowisdom.wisdomspark.premium.model.SubscriptionPlan
import com.albertowisdom.wisdomspark.premium.model.SubscriptionStatus
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestionar las funcionalidades Premium
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    init {
        observeBillingState()
    }

    /**
     * Observar estados del billing manager
     */
    private fun observeBillingState() {
        viewModelScope.launch {
            combine(
                billingManager.subscriptionStatus,
                billingManager.purchaseState,
                billingManager.availableProducts,
                billingManager.isConnected
            ) { subscriptionStatus, purchaseState, products, isConnected ->
                PremiumUiState(
                    subscriptionStatus = subscriptionStatus,
                    purchaseState = purchaseState,
                    availableProducts = products,
                    isConnected = isConnected,
                    isLoading = purchaseState is PurchaseState.Loading
                )
            }.collect { newState ->
                _uiState.value = newState
                
                // Sincronizar estado Premium con UserPreferences
                syncPremiumStatus(newState.subscriptionStatus)
            }
        }
    }

    /**
     * Sincronizar estado Premium con UserPreferences
     */
    private suspend fun syncPremiumStatus(status: SubscriptionStatus) {
        userPreferences.setPremiumStatus(status.isPremium)
        userPreferences.setPremiumPurchaseToken(status.purchaseToken)
        userPreferences.setPremiumPlan(status.activePlan?.productId)
        userPreferences.setPremiumPurchaseTime(status.purchaseTime?.toString())
        userPreferences.setPremiumExpiryTime(status.expiryTime?.toString())
    }

    /**
     * Iniciar compra de suscripción
     */
    fun purchaseSubscription(activity: Activity, plan: SubscriptionPlan) {
        billingManager.launchBillingFlow(activity, plan)
    }

    /**
     * Obtener precio formateado de un plan
     */
    fun getFormattedPrice(plan: SubscriptionPlan): String {
        return billingManager.getFormattedPrice(plan) ?: "---"
    }

    /**
     * Verificar si una característica está disponible
     */
    fun hasFeature(feature: PremiumFeature): Boolean {
        return billingManager.hasFeature(feature)
    }

    /**
     * Refrescar estado de compras
     */
    fun refreshPurchases() {
        billingManager.refreshPurchases()
    }

    /**
     * Restablecer estado de compra
     */
    fun resetPurchaseState() {
        // Note: BillingManager no expone un método para esto directamente
        // El estado se resetea automáticamente en transacciones futuras
    }

    /**
     * Obtener características incluidas en un plan
     */
    fun getFeaturesForPlan(plan: SubscriptionPlan): List<PremiumFeature> {
        return plan.features
    }

    /**
     * Obtener todas las características Premium disponibles
     */
    fun getAllPremiumFeatures(): List<PremiumFeature> {
        return PremiumFeature.values().toList()
    }

    /**
     * Verificar si el usuario tiene Premium activo
     */
    fun isPremium(): Boolean {
        return _uiState.value.subscriptionStatus.isPremium
    }

    /**
     * Obtener el plan activo actual
     */
    fun getActivePlan(): SubscriptionPlan? {
        return _uiState.value.subscriptionStatus.activePlan
    }

    /**
     * Obtener productos disponibles para compra
     */
    fun getAvailableProducts(): List<ProductDetails> {
        return _uiState.value.availableProducts
    }

    /**
     * Estado de la UI Premium
     */
    data class PremiumUiState(
        val subscriptionStatus: SubscriptionStatus = SubscriptionStatus(),
        val purchaseState: PurchaseState = PurchaseState.Loading,
        val availableProducts: List<ProductDetails> = emptyList(),
        val isConnected: Boolean = false,
        val isLoading: Boolean = true
    ) {
        val isPremium: Boolean get() = subscriptionStatus.isPremium
        val hasActiveSubscription: Boolean get() = subscriptionStatus.activePlan != null
        val canMakePurchases: Boolean get() = isConnected && availableProducts.isNotEmpty()
    }
}