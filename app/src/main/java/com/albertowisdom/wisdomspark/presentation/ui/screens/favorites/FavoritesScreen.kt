package com.albertowisdom.wisdomspark.presentation.ui.screens.favorites

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.ads.BannerAdView
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.presentation.ui.components.QuoteCard
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import com.albertowisdom.wisdomspark.utils.ShareUtils

/**
 * Pantalla de favoritos completamente funcional
 */
@Composable
fun FavoritesScreen(
    adMobManager: AdMobManager,
    viewModel: FavoritesViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Contenido principal
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "❤️ Mis Favoritos",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = WisdomCharcoal
                            )
                        )
                        
                        Text(
                            text = "${uiState.favoriteQuotes.size} citas guardadas",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = WisdomTaupe
                            )
                        )
                    }
                    
                    // Botón refresh
                    IconButton(
                        onClick = { viewModel.refreshFavorites() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = WisdomGold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Contenido según estado
                when {
                    uiState.isLoading -> {
                        LoadingFavoritesSection()
                    }
                    
                    uiState.favoriteQuotes.isNotEmpty() -> {
                        FavoritesList(
                            favorites = uiState.favoriteQuotes,
                            onFavoriteClick = { quote ->
                                viewModel.toggleFavorite(quote)
                            },
                            onShareClick = { quote ->
                                ShareUtils.shareQuote(context, quote)
                                viewModel.onQuoteShared()
                            }
                        )
                    }
                    
                    else -> {
                        EmptyFavoritesSection()
                    }
                }
            }
            
            // Banner Ad
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

@Composable
private fun FavoritesList(
    favorites: List<Quote>,
    onFavoriteClick: (Quote) -> Unit,
    onShareClick: (Quote) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = favorites,
            key = { it.id }
        ) { quote ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                QuoteCard(
                    quote = quote,
                    onFavoriteClick = { onFavoriteClick(quote) },
                    onShareClick = { onShareClick(quote) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Espaciado al final para mejor UX
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LoadingFavoritesSection() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = WisdomCoral,
                strokeWidth = 3.dp
            )
            
            Text(
                text = "Cargando tus favoritos...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WisdomTaupe
                )
            )
        }
    }
}

@Composable
private fun EmptyFavoritesSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Animación de entrada para el contenido vacío
        val animatedScale by animateFloatAsState(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "emptyScale"
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = WisdomCoral.copy(alpha = 0.5f)
                )
                
                Text(
                    text = "No tienes favoritos aún",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = WisdomCharcoal
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Marca con ❤️ las citas que más te inspiren para guardarlas aquí",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WisdomTaupe
                    ),
                    textAlign = TextAlign.Center
                )
                
                Button(
                    onClick = { /* Navegar a home */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WisdomCoral
                    )
                ) {
                    Text("Explorar Citas")
                }
            }
        }
    }
}

