package com.albertowisdom.wisdomspark.presentation.ui.screens.home

import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.ads.BannerAdView
import com.albertowisdom.wisdomspark.presentation.ui.components.QuoteCard
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import com.albertowisdom.wisdomspark.utils.ShareUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla principal de WisdomSpark con integración AdMob
 */
@Composable
fun HomeScreen(
    adMobManager: AdMobManager,
    viewModel: HomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Log del estado para debug
    LaunchedEffect(uiState) {
        Log.d("HomeScreen", "🏠 Estado actual: $uiState")
    }
    
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
        Log.d("AdMob", "Inicializando AdMob...")
        adMobManager.initialize(context)
        Log.d("AdMob", "AdMob inicializado")
    }

    // Gradiente de fondo
    val backgroundGradient = Brush.linearGradient(
        colors = getWisdomGradientColors(),
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
                        Log.d("HomeScreen", "📱 Mostrando LoadingSection")
                        LoadingSection(
                            onRetryClick = { 
                                Log.d("HomeScreen", "🔄 Retry clicked")
                                viewModel.loadTodayQuote() 
                            }
                        )
                    }
                    
                    uiState.error != null -> {
                        Log.d("HomeScreen", "📱 Mostrando ErrorSection: ${uiState.error}")
                        ErrorSection(
                            error = uiState.error!!, // Safe cast ya que verificamos que no es null
                            onRetryClick = { 
                                Log.d("HomeScreen", "🔄 Error retry clicked")
                                viewModel.loadTodayQuote() 
                            }
                        )
                    }
                    
                    uiState.todayQuote != null -> {
                        Log.d("HomeScreen", "📱 Mostrando QuoteSection: ${uiState.todayQuote!!.text.take(30)}...")
                        QuoteSection(
                            quote = uiState.todayQuote!!, // Safe cast ya que verificamos que no es null
                            onFavoriteClick = { 
                                viewModel.toggleFavorite()
                                // Mostrar interstitial ocasionalmente
                                if (context is androidx.activity.ComponentActivity) {
                                    adMobManager.showInterstitialAd(context)
                                }
                            },
                            onShareClick = { 
                                ShareUtils.shareQuote(context, uiState.todayQuote!!) // Safe cast
                                // Mostrar interstitial al compartir
                                if (context is androidx.activity.ComponentActivity) {
                                    adMobManager.showInterstitialAd(context, force = true)
                                }
                            }
                        )
                    }
                    
                    else -> {
                        // Estado inesperado - mostrar loading con botón para debug
                        Log.w("HomeScreen", "📱 Estado inesperado: $uiState")
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Estado inesperado",
                                style = MaterialTheme.typography.headlineSmall,
                                color = WisdomError
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Debug: isLoading=${uiState.isLoading}, error=${uiState.error}, quote=${uiState.todayQuote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = WisdomTaupe
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { viewModel.loadTodayQuote() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WisdomGold
                                )
                            ) {
                                Text("Cargar Cita")
                            }
                        }
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
    val today = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES"))
            .format(Date())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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
                color = WisdomCharcoal
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = today,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = WisdomTaupe,
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
            color = WisdomGold,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Preparando tu sabiduría diaria...",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WisdomTaupe
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onRetryClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = WisdomGold
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
                color = WisdomError,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WisdomTaupe
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetryClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = WisdomGold,
                contentColor = Color.White
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
    onShareClick: () -> Unit
) {
    QuoteCard(
        quote = quote,
        onFavoriteClick = onFavoriteClick,
        onShareClick = onShareClick,
        modifier = Modifier.fillMaxWidth()
    )
}
