package com.albertowisdom.wisdomspark.premium.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.albertowisdom.wisdomspark.R
import com.albertowisdom.wisdomspark.premium.model.PremiumFeature
import com.albertowisdom.wisdomspark.premium.model.PurchaseState
import com.albertowisdom.wisdomspark.premium.model.SubscriptionPlan

/**
 * Pantalla principal de WisdomSpark Premium
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showPurchaseDialog by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) }

    // Manejar estado de compra de forma más eficiente
    when (uiState.purchaseState) {
        is PurchaseState.Success -> {
            LaunchedEffect(Unit) { showPurchaseDialog = false }
        }
        is PurchaseState.Cancelled -> {
            LaunchedEffect(Unit) { showPurchaseDialog = false }
        }
        else -> { /* No action needed */ }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.premium_screen_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.content_description_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->

        if (uiState.isPremium) {
            // Usuario ya tiene Premium
            PremiumActiveContent(
                modifier = Modifier.padding(paddingValues),
                subscriptionStatus = uiState.subscriptionStatus,
                onRefreshPurchases = { viewModel.refreshPurchases() }
            )
        } else {
            // Mostrar opciones de compra
            PremiumPurchaseContent(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState,
                onPlanSelected = { plan ->
                    selectedPlan = plan
                    showPurchaseDialog = true
                },
                scrollState = scrollState,
                viewModel = viewModel
            )
        }
    }

    // Dialog de confirmación de compra
    if (showPurchaseDialog && selectedPlan != null) {
        PurchaseConfirmationDialog(
            plan = selectedPlan!!,
            purchaseState = uiState.purchaseState,
            formattedPrice = viewModel.getFormattedPrice(selectedPlan!!),
            onConfirm = {
                viewModel.purchaseSubscription(context as Activity, selectedPlan!!)
            },
            onDismiss = {
                showPurchaseDialog = false
                selectedPlan = null
            }
        )
    }
}

@Composable
private fun PremiumActiveContent(
    modifier: Modifier = Modifier,
    subscriptionStatus: com.albertowisdom.wisdomspark.premium.model.SubscriptionStatus,
    onRefreshPurchases: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Icono de Premium activo
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFFA500)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\uD83D\uDC51",
                fontSize = 48.sp
            )
        }

        Text(
            text = stringResource(R.string.premium_active_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.premium_active_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Información de la suscripción
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.premium_subscription_details),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                subscriptionStatus.activePlan?.let { plan ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.premium_plan_label))
                        Text(
                            text = when (plan) {
                                SubscriptionPlan.MONTHLY -> stringResource(R.string.subscription_monthly)
                                SubscriptionPlan.YEARLY -> stringResource(R.string.subscription_yearly)
                                SubscriptionPlan.LIFETIME -> stringResource(R.string.subscription_lifetime)
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (subscriptionStatus.purchaseTime != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.premium_purchase_date))
                        Text(
                            text = java.time.Instant.ofEpochMilli(subscriptionStatus.purchaseTime)
                                .atZone(java.time.ZoneId.systemDefault())
                                .format(java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.premium_auto_renewal))
                    Text(
                        text = if (subscriptionStatus.isAutoRenewing)
                            stringResource(R.string.premium_auto_renewal_active)
                        else
                            stringResource(R.string.premium_auto_renewal_inactive),
                        fontWeight = FontWeight.Medium,
                        color = if (subscriptionStatus.isAutoRenewing)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Características Premium disponibles
        PremiumFeaturesGrid(
            features = PremiumFeature.values().toList(),
            isUnlocked = true
        )

        // Botón de actualizar
        OutlinedButton(
            onClick = onRefreshPurchases,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.premium_refresh_subscription))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PremiumPurchaseContent(
    modifier: Modifier = Modifier,
    uiState: PremiumViewModel.PremiumUiState,
    onPlanSelected: (SubscriptionPlan) -> Unit,
    scrollState: androidx.compose.foundation.ScrollState,
    viewModel: PremiumViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header Premium
        PremiumHeader()

        // Características Premium
        PremiumFeaturesGrid(
            features = viewModel.getAllPremiumFeatures(),
            isUnlocked = false
        )

        // Planes de suscripción
        Text(
            text = stringResource(R.string.premium_choose_plan),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        SubscriptionPlansGrid(
            plans = SubscriptionPlan.values().toList(),
            uiState = uiState,
            onPlanSelected = onPlanSelected,
            viewModel = viewModel
        )

        // Información adicional
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.premium_important_info),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.premium_info_bullets),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PremiumHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icono Premium
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFFA500)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\uD83D\uDC51",
                fontSize = 32.sp
            )
        }

        Text(
            text = stringResource(R.string.premium_unlock_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.premium_unlock_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PremiumFeaturesGrid(
    features: List<PremiumFeature>,
    isUnlocked: Boolean
) {
    Text(
        text = if (isUnlocked) stringResource(R.string.premium_your_features) else stringResource(R.string.premium_features_title),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    features.chunked(2).forEach { rowFeatures ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rowFeatures.forEach { feature ->
                PremiumFeatureCard(
                    feature = feature,
                    isUnlocked = isUnlocked,
                    modifier = Modifier.weight(1f)
                )
            }

            // Relleno si la fila no está completa
            if (rowFeatures.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PremiumFeatureCard(
    feature: PremiumFeature,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = feature.icon,
                fontSize = 24.sp
            )

            Text(
                text = when (feature) {
                    PremiumFeature.AD_FREE -> stringResource(R.string.premium_feature_ad_free)
                    PremiumFeature.PREMIUM_THEMES -> stringResource(R.string.premium_feature_themes)
                    PremiumFeature.UNLIMITED_FAVORITES -> stringResource(R.string.premium_feature_unlimited_favorites)
                    PremiumFeature.ADVANCED_CATEGORIES -> stringResource(R.string.premium_feature_advanced_categories)
                    PremiumFeature.QUOTE_SHARING_STYLES -> stringResource(R.string.premium_feature_sharing_styles)
                    PremiumFeature.OFFLINE_MODE -> stringResource(R.string.premium_feature_offline)
                    PremiumFeature.DAILY_QUOTES_CUSTOMIZATION -> stringResource(R.string.premium_feature_daily_customization)
                    PremiumFeature.PRIORITY_SUPPORT -> stringResource(R.string.premium_feature_priority_support)
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = when (feature) {
                    PremiumFeature.AD_FREE -> stringResource(R.string.premium_desc_ad_free)
                    PremiumFeature.PREMIUM_THEMES -> stringResource(R.string.premium_desc_themes)
                    PremiumFeature.UNLIMITED_FAVORITES -> stringResource(R.string.premium_desc_favorites)
                    PremiumFeature.ADVANCED_CATEGORIES -> stringResource(R.string.premium_desc_categories)
                    PremiumFeature.QUOTE_SHARING_STYLES -> stringResource(R.string.premium_desc_sharing)
                    PremiumFeature.OFFLINE_MODE -> stringResource(R.string.premium_desc_offline)
                    PremiumFeature.DAILY_QUOTES_CUSTOMIZATION -> stringResource(R.string.premium_desc_daily)
                    PremiumFeature.PRIORITY_SUPPORT -> stringResource(R.string.premium_desc_support)
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isUnlocked) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.premium_unlocked),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SubscriptionPlansGrid(
    plans: List<SubscriptionPlan>,
    uiState: PremiumViewModel.PremiumUiState,
    onPlanSelected: (SubscriptionPlan) -> Unit,
    viewModel: PremiumViewModel
) {
    plans.forEach { plan ->
        SubscriptionPlanCard(
            plan = plan,
            formattedPrice = viewModel.getFormattedPrice(plan),
            isLoading = uiState.isLoading,
            canPurchase = uiState.canMakePurchases,
            onPlanSelected = onPlanSelected
        )
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    formattedPrice: String,
    isLoading: Boolean,
    canPurchase: Boolean,
    onPlanSelected: (SubscriptionPlan) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (plan.isPopular) {
                    Modifier.padding(4.dp)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (plan.isPopular)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (plan.isPopular)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (plan.isPopular) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header del plan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (plan) {
                            SubscriptionPlan.MONTHLY -> stringResource(R.string.premium_plan_monthly_title)
                            SubscriptionPlan.YEARLY -> stringResource(R.string.premium_plan_yearly_title)
                            SubscriptionPlan.LIFETIME -> stringResource(R.string.premium_plan_lifetime_title)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (plan.isPopular) {
                        Text(
                            text = stringResource(R.string.premium_popular_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Descripción
            Text(
                text = when (plan) {
                    SubscriptionPlan.MONTHLY -> stringResource(R.string.subscription_monthly_desc)
                    SubscriptionPlan.YEARLY -> stringResource(R.string.subscription_yearly_desc)
                    SubscriptionPlan.LIFETIME -> stringResource(R.string.subscription_lifetime_desc)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Botón de compra
            Button(
                onClick = { onPlanSelected(plan) },
                modifier = Modifier.fillMaxWidth(),
                enabled = canPurchase && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (plan.isPopular)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = when (plan) {
                            SubscriptionPlan.MONTHLY -> stringResource(R.string.premium_subscribe_monthly)
                            SubscriptionPlan.YEARLY -> stringResource(R.string.premium_subscribe_yearly)
                            SubscriptionPlan.LIFETIME -> stringResource(R.string.premium_buy_lifetime)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PurchaseConfirmationDialog(
    plan: SubscriptionPlan,
    purchaseState: PurchaseState,
    formattedPrice: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val planName = when (plan) {
        SubscriptionPlan.MONTHLY -> stringResource(R.string.subscription_monthly)
        SubscriptionPlan.YEARLY -> stringResource(R.string.subscription_yearly)
        SubscriptionPlan.LIFETIME -> stringResource(R.string.subscription_lifetime)
    }

    AlertDialog(
        onDismissRequest = { if (purchaseState !is PurchaseState.Loading) onDismiss() },
        title = {
            Text(
                text = stringResource(R.string.premium_confirm_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.premium_confirm_body)
                )

                Text(
                    text = stringResource(R.string.premium_plan_with_label, planName),
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = stringResource(R.string.premium_price_label, formattedPrice),
                    fontWeight = FontWeight.Medium
                )

                when (purchaseState) {
                    is PurchaseState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Text(stringResource(R.string.premium_processing))
                        }
                    }
                    is PurchaseState.Error -> {
                        Text(
                            text = stringResource(R.string.premium_error_prefix, purchaseState.message),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = purchaseState !is PurchaseState.Loading
            ) {
                Text(stringResource(R.string.premium_confirm_button))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = purchaseState !is PurchaseState.Loading
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
