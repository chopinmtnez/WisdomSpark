package com.albertowisdom.wisdomspark.presentation.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.albertowisdom.wisdomspark.R
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.ads.BannerAdView
import com.albertowisdom.wisdomspark.presentation.ui.components.QuoteCard
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import com.albertowisdom.wisdomspark.utils.ShareUtils
import com.albertowisdom.wisdomspark.utils.DateUtils
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.stringResource

/**
 * Pantalla principal de WisdomSpark con integración AdMob
 */
@Composable
fun HomeScreen(
    adMobManager: AdMobManager,
    viewModel: HomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    userPreferences: com.albertowisdom.wisdomspark.data.preferences.UserPreferences? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Animación de entrada
    val enterAnimation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), 
        label = "enterAnimation"
    )

    // Inicializar AdMob al crear la pantalla
    LaunchedEffect(Unit) {
        adMobManager.initialize(context)
    }

    // Gradiente de fondo que respeta el tema
    val backgroundGradient = Brush.linearGradient(
        colors = getThemedGradientColors(),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(enterAnimation)
        ) {
            // Contenido scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Header
                HeaderSection()
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Contenido principal según estado
                when {
                    uiState.isLoading -> {
                        LoadingSection(
                            onRetryClick = { viewModel.refreshTodayQuote() }
                        )
                    }

                    uiState.error != null -> {
                        ErrorSection(
                            error = uiState.error!!,
                            onRetryClick = { viewModel.refreshTodayQuote() }
                        )
                    }

                    uiState.todayQuote != null -> {
                        val quote = uiState.todayQuote!!
                        QuoteSection(
                            quote = quote,
                            onFavoriteClick = {
                                viewModel.toggleFavorite()
                                if (context is androidx.activity.ComponentActivity) {
                                    adMobManager.showInterstitialAd(context)
                                }
                            },
                            onShareClick = {
                                ShareUtils.shareQuote(context, quote)
                                if (context is androidx.activity.ComponentActivity) {
                                    adMobManager.showInterstitialAd(context, force = true)
                                }
                            },
                            userPreferences = userPreferences
                        )

                        // Botón "Ver anuncio para otra cita" (solo free users)
                        if (adMobManager.shouldShowAds() && viewModel.hasRewardedAd()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    val activity = context as? android.app.Activity
                                    if (activity != null) {
                                        adMobManager.showRewardedAd(
                                            activity = activity,
                                            onRewarded = {
                                                viewModel.loadRandomQuote()
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.rewarded_new_quote_unlocked),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onFailed = {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.rewarded_ad_not_available),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🎬 " + stringResource(R.string.rewarded_watch_for_new_quote),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }

                    else -> {
                        ErrorSection(
                            error = "Algo no fue bien. Toca reintentar para cargar tu cita.",
                            onRetryClick = { viewModel.refreshTodayQuote() }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Banner Ad en la parte inferior (solo si no es premium)
            if (adMobManager.shouldShowAds()) {
                BannerAdView(
                    onAdLoaded = { adMobManager.onBannerAdLoaded() },
                    onAdFailedToLoad = { error ->
                        adMobManager.onBannerAdFailedToLoad(error)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Header con fecha y saludo
 */
@Composable
private fun HeaderSection() {
    val context = LocalContext.current
    val today = remember(context) {
        DateUtils.getCurrentDateFormatted(context)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WisdomSpark",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = today,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Sección de loading con retry
 */
@Composable
private fun LoadingSection(onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Preparando tu sabiduría diaria...",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onRetryClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reintentar",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

/**
 * Sección de error con retry
 */
@Composable
private fun ErrorSection(
    error: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Oops...",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetryClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reintentar",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

/**
 * Sección principal con la cita
 */
@Composable
private fun QuoteSection(
    quote: com.albertowisdom.wisdomspark.data.models.Quote,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    userPreferences: com.albertowisdom.wisdomspark.data.preferences.UserPreferences? = null
) {
    QuoteCard(
        quote = quote,
        onFavoriteClick = onFavoriteClick,
        onShareClick = onShareClick,
        modifier = Modifier.fillMaxWidth(),
        userPreferences = userPreferences
    )
}
