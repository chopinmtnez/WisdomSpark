package com.albertowisdom.wisdomspark.premium.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.albertowisdom.wisdomspark.premium.model.PurchaseState
import com.albertowisdom.wisdomspark.premium.model.SubscriptionPlan
import com.albertowisdom.wisdomspark.premium.model.SubscriptionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de facturación y suscripciones Premium
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : DefaultLifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener {

    private val tag = "BillingManager"
    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + supervisorJob)
    private var retryCount = 0
    private val maxRetries = 5
    private val connectionTimeoutMs = 15_000L // Timeout para conexión inicial

    // BillingClient
    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    // Estados
    private val _subscriptionStatus = MutableStateFlow(SubscriptionStatus())
    val subscriptionStatus: StateFlow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Loading)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val availableProducts: StateFlow<List<ProductDetails>> = _availableProducts.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    init {
        startConnection()
    }

    /**
     * Conectar al Google Play Billing con timeout de seguridad
     */
    private fun startConnection() {
        if (!billingClient.isReady) {
            Log.d(tag, "Iniciando conexión con Google Play Billing...")
            billingClient.startConnection(this)

            // Timeout: si tras connectionTimeoutMs el estado sigue en Loading, pasar a Idle
            // para que la UI no quede bloqueada indefinidamente
            scope.launch {
                delay(connectionTimeoutMs)
                if (_purchaseState.value is PurchaseState.Loading) {
                    Log.w(tag, "⏰ Timeout de conexión ($connectionTimeoutMs ms). Desbloqueando UI.")
                    _purchaseState.value = PurchaseState.Idle
                }
            }
        }
    }

    /**
     * Callback cuando la conexión está lista
     */
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        Log.d(tag, "onBillingSetupFinished: ${billingResult.responseCode}")
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                _isConnected.value = true
                retryCount = 0
                Log.d(tag, "Conexión con Google Play Billing exitosa")
                
                // Cargar productos disponibles
                queryProductDetails()
                
                // Verificar compras existentes
                queryPurchases()
            }
            BillingResponseCode.BILLING_UNAVAILABLE -> {
                _isConnected.value = false
                Log.w(tag, "⚠️ Google Play Billing no disponible. Activando modo sin Premium.")
                // En emulador sin Google Play Services, continuar sin Premium
                _subscriptionStatus.value = SubscriptionStatus(isPremium = false)
                _purchaseState.value = PurchaseState.Error("Billing no disponible en este dispositivo")
            }
            BillingResponseCode.SERVICE_UNAVAILABLE -> {
                _isConnected.value = false
                if (retryCount < maxRetries) {
                    retryCount++
                    val delayMs = (3000L * retryCount).coerceAtMost(30_000L) // Backoff: 3s, 6s, 9s... max 30s
                    Log.w(tag, "Servicio no disponible. Reintento $retryCount/$maxRetries en ${delayMs}ms")
                    scope.launch {
                        kotlinx.coroutines.delay(delayMs)
                        startConnection()
                    }
                } else {
                    Log.w(tag, "Máximo de reintentos alcanzado ($maxRetries). Billing deshabilitado.")
                    _purchaseState.value = PurchaseState.Error("Billing no disponible")
                }
            }
            else -> {
                _isConnected.value = false
                val message = getErrorMessage(billingResult.responseCode)
                Log.e(tag, "❌ Error en conexión: $message (${billingResult.debugMessage})")
                _purchaseState.value = PurchaseState.Error("Error de conexión: $message")
            }
        }
    }

    /**
     * Callback cuando se desconecta
     */
    override fun onBillingServiceDisconnected() {
        _isConnected.value = false
        Log.d(tag, "Conexión con Google Play Billing perdida, reconectando...")
        // Google recomienda reconectar en este callback
        retryCount = 0 // Reset retries al ser desconexión (no fallo)
        startConnection()
    }

    /**
     * Consultar detalles de productos disponibles
     */
    private fun queryProductDetails() {
        val allProducts = mutableListOf<ProductDetails>()

        // Consultar suscripciones (MONTHLY, YEARLY)
        val subPlans = SubscriptionPlan.entries.filter { !it.isOneTimePurchase }
        if (subPlans.isNotEmpty()) {
            val subProductList = subPlans.map { plan ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(plan.productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }
            val subParams = QueryProductDetailsParams.newBuilder().setProductList(subProductList).build()
            billingClient.queryProductDetailsAsync(subParams) { billingResult, productDetailsResult ->
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    allProducts.addAll(productDetailsResult.productDetailsList)
                    Log.d(tag, "Suscripciones cargadas: ${productDetailsResult.productDetailsList.size}")
                }
                queryInAppProducts(allProducts)
            }
        } else {
            queryInAppProducts(allProducts)
        }
    }

    /**
     * Consultar productos INAPP (compras únicas como LIFETIME)
     */
    private fun queryInAppProducts(existingProducts: MutableList<ProductDetails>) {
        val inAppPlans = SubscriptionPlan.entries.filter { it.isOneTimePurchase }
        if (inAppPlans.isNotEmpty()) {
            val inAppProductList = inAppPlans.map { plan ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(plan.productId)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            }
            val inAppParams = QueryProductDetailsParams.newBuilder().setProductList(inAppProductList).build()
            billingClient.queryProductDetailsAsync(inAppParams) { billingResult, productDetailsResult ->
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    existingProducts.addAll(productDetailsResult.productDetailsList)
                    Log.d(tag, "Productos INAPP cargados: ${productDetailsResult.productDetailsList.size}")
                } else {
                    Log.w(tag, "⚠️ Error cargando productos INAPP: ${billingResult.debugMessage}")
                }
                _availableProducts.value = existingProducts.toList()
                // Todos los productos consultados → desbloquear UI
                markProductsReady()
            }
        } else {
            _availableProducts.value = existingProducts.toList()
            markProductsReady()
        }
    }

    /**
     * Marcar que la consulta de productos ha terminado.
     * Transita de Loading a Idle para desbloquear los botones de compra.
     */
    private fun markProductsReady() {
        if (_purchaseState.value is PurchaseState.Loading) {
            _purchaseState.value = PurchaseState.Idle
            Log.d(tag, "✅ Productos cargados. Estado → Idle (UI desbloqueada)")
        }
    }

    /**
     * Consultar compras existentes
     */
    private fun queryPurchases() {
        val allPurchases = mutableListOf<Purchase>()

        // Consultar suscripciones
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        billingClient.queryPurchasesAsync(subsParams) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                allPurchases.addAll(purchasesList)
            }
            // Después consultar INAPP (para LIFETIME)
            val inAppParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            billingClient.queryPurchasesAsync(inAppParams) { inAppResult, inAppPurchases ->
                if (inAppResult.responseCode == BillingResponseCode.OK) {
                    allPurchases.addAll(inAppPurchases)
                }
                Log.d(tag, "Total compras encontradas: ${allPurchases.size}")
                processPurchases(allPurchases)
            }
        }
    }

    /**
     * Procesar lista de compras
     */
    private fun processPurchases(purchases: List<Purchase>) {
        var activePremium: SubscriptionStatus? = null

        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // Verificar si es un producto Premium válido
                val plan = SubscriptionPlan.entries.find { 
                    purchase.products.contains(it.productId) 
                }
                
                if (plan != null) {
                    activePremium = SubscriptionStatus(
                        isPremium = true,
                        activePlan = plan,
                        purchaseTime = purchase.purchaseTime,
                        isAutoRenewing = purchase.isAutoRenewing,
                        purchaseToken = purchase.purchaseToken
                    )
                    
                    // Acknowledge la compra si no ha sido acknowledged
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                    
                    Log.d(tag, "Suscripción Premium activa: ${plan.productId}")
                    break
                }
            }
        }

        _subscriptionStatus.value = activePremium ?: SubscriptionStatus()
    }

    /**
     * Acknowledge una compra
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                Log.d(tag, "Compra acknowledged correctamente")
            } else {
                Log.e(tag, "Error acknowledging compra: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Iniciar compra de suscripción
     */
    fun launchBillingFlow(activity: Activity, subscriptionPlan: SubscriptionPlan) {
        val productDetails = _availableProducts.value.find {
            it.productId == subscriptionPlan.productId
        }

        if (productDetails == null) {
            Log.e(tag, "Producto no encontrado: ${subscriptionPlan.productId}")
            _purchaseState.value = PurchaseState.Error("Producto no disponible")
            return
        }

        // Para INAPP (Lifetime) no se necesita offerToken; para SUBS sí
        val productDetailsParams = if (subscriptionPlan.isOneTimePurchase) {
            // Compra única (INAPP) - sin offerToken
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        } else {
            // Suscripción (SUBS) - requiere offerToken
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken == null) {
                Log.e(tag, "Offer token no encontrado para: ${subscriptionPlan.productId}")
                _purchaseState.value = PurchaseState.Error("Oferta no disponible")
                return
            }
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        _purchaseState.value = PurchaseState.Loading
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

        if (billingResult.responseCode != BillingResponseCode.OK) {
            Log.e(tag, "Error iniciando billing flow: ${billingResult.debugMessage}")
            _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
        }
    }

    /**
     * Callback cuando se actualizan las compras
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        Log.d(tag, "onPurchasesUpdated: ${billingResult.responseCode}")
        
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                purchases?.let { processPurchases(it) }
                _purchaseState.value = PurchaseState.Success
            }
            BillingResponseCode.USER_CANCELED -> {
                Log.d(tag, "Compra cancelada por el usuario")
                _purchaseState.value = PurchaseState.Cancelled
            }
            BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(tag, "El usuario ya posee este producto")
                queryPurchases() // Refrescar estado
                _purchaseState.value = PurchaseState.Success
            }
            else -> {
                Log.e(tag, "Error en compra: ${billingResult.debugMessage}")
                _purchaseState.value = PurchaseState.Error(
                    billingResult.debugMessage ?: "Error desconocido"
                )
            }
        }
    }

    /**
     * Obtener precio formateado de un plan
     */
    fun getFormattedPrice(subscriptionPlan: SubscriptionPlan): String? {
        val productDetails = _availableProducts.value.find {
            it.productId == subscriptionPlan.productId
        }
        return if (subscriptionPlan.isOneTimePurchase) {
            // INAPP: precio de compra única
            productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
        } else {
            // SUBS: precio de la primera fase
            productDetails?.subscriptionOfferDetails?.firstOrNull()
                ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
        }
    }

    /**
     * Verificar si el usuario tiene Premium
     * MODO DE PRUEBA TEMPORAL: Activar premium para testing
     */
    fun isPremium(): Boolean {
        // Testing mode SOLO en builds de debug
        if (com.albertowisdom.wisdomspark.BuildConfig.DEBUG) {
            val isTestingMode = try {
                com.albertowisdom.wisdomspark.premium.debug.PremiumDebugHelper.isTestingModeEnabled(context)
            } catch (e: Exception) {
                false
            }
            if (isTestingMode) {
                Log.d(tag, "MODO TESTING: Premium activado (solo debug)")
                return true
            }
        }
        return _subscriptionStatus.value.isPremium
    }

    /**
     * Verificar si una característica Premium está disponible
     */
    fun hasFeature(feature: com.albertowisdom.wisdomspark.premium.model.PremiumFeature): Boolean {
        val hasPremium = isPremium()
        Log.d(tag, "🔍 Verificando característica ${feature.name}: $hasPremium")
        return hasPremium
    }

    /**
     * Restablecer el estado de compra a Idle (listo para nueva operación)
     */
    fun resetToIdle() {
        _purchaseState.value = PurchaseState.Idle
    }

    /**
     * Refrescar estado de compras y también modo testing
     */
    fun refreshPurchases() {
        if (billingClient.isReady) {
            queryPurchases()
        } else {
            startConnection()
        }
    }
    
    /**
     * Forzar actualización del estado premium (útil para testing)
     */
    fun forceUpdatePremiumStatus() {
        val currentStatus = isPremium()
        Log.d(tag, "🔄 Estado premium forzado: $currentStatus")
        
        // Emitir cambio en el flow para notificar a observadores
        _subscriptionStatus.value = _subscriptionStatus.value.copy(
            isPremium = currentStatus
        )
    }

    /**
     * Obtener mensaje de error legible
     */
    private fun getErrorMessage(responseCode: Int): String {
        return when (responseCode) {
            BillingResponseCode.SERVICE_TIMEOUT -> "Tiempo de espera agotado"
            BillingResponseCode.FEATURE_NOT_SUPPORTED -> "Función no soportada"
            BillingResponseCode.SERVICE_DISCONNECTED -> "Servicio desconectado"
            BillingResponseCode.USER_CANCELED -> "Compra cancelada por el usuario"
            BillingResponseCode.SERVICE_UNAVAILABLE -> "Servicio no disponible"
            BillingResponseCode.BILLING_UNAVAILABLE -> "Billing no disponible"
            BillingResponseCode.ITEM_UNAVAILABLE -> "Producto no disponible"
            BillingResponseCode.DEVELOPER_ERROR -> "Error de desarrollador"
            BillingResponseCode.ERROR -> "Error general"
            BillingResponseCode.ITEM_ALREADY_OWNED -> "Producto ya comprado"
            BillingResponseCode.ITEM_NOT_OWNED -> "Producto no comprado"
            3 -> "API version muy antigua (requiere Google Play Services actualizado)"
            else -> "Error desconocido ($responseCode)"
        }
    }

    /**
     * Limpiar recursos
     */
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        supervisorJob.cancel() // Cancelar todas las coroutines (incluidos retries)
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}