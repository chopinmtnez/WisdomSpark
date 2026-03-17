package com.albertowisdom.wisdomspark.presentation.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.albertowisdom.wisdomspark.R
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.ads.BannerAdView
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.presentation.ui.components.QuoteCard
import com.albertowisdom.wisdomspark.presentation.ui.components.ShareBottomSheet
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import com.albertowisdom.wisdomspark.utils.getCategoryEmoji
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de detalle de categoría - muestra todas las citas de una categoría específica
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryName: String,
    adMobManager: AdMobManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: CategoryDetailViewModel = hiltViewModel()
    
    // Estados para las nuevas funcionalidades
    var showShareBottomSheet by remember { mutableStateOf(false) }
    
    // Observar las citas de esta categoría (con filtrado por idioma automático)
    val quotes by viewModel.getQuotesByCategory(categoryName).collectAsState(initial = emptyList())
    var selectedQuoteForShare by remember { mutableStateOf<Quote?>(null) }
    var favoriteQuotes by remember { mutableStateOf(setOf<Long>()) }
    var showFavoriteAnimation by remember { mutableStateOf<Long?>(null) }
    
    // Mostrar el número de citas reales en el header
    val quotesCount = quotes.size
    
    // Cargar favoritos usando collectAsState para evitar memory leaks
    val favoriteQuotesList by viewModel.getFavoriteQuotesFlow().collectAsState(initial = emptyList())
    favoriteQuotes = favoriteQuotesList.map { it.id }.toSet()
    
    
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header con botón de retroceso
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de retroceso
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shadowElevation = 4.dp,
                        onClick = onNavigateBack
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Título de la categoría
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${getCategoryEmoji(categoryName)} $categoryName",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = if (quotesCount == 1) stringResource(R.string.quotes_available_singular, quotesCount) else stringResource(R.string.quotes_available_plural, quotesCount),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            
            // Lista de citas
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(
                    items = quotes,
                    key = { it.id }
                ) { quote ->
                    val isFavorite = favoriteQuotes.contains(quote.id)
                    val quoteWithFavorite = remember(quote.id, isFavorite) {
                        quote.copy(isFavorite = isFavorite)
                    }
                    QuoteCard(
                        quote = quoteWithFavorite,
                        onFavoriteClick = {
                            // Smart favorites: marca como favorito con animación y guarda en BD
                            scope.launch {
                                try {
                                    viewModel.toggleFavoriteQuote(quote)
                                    showFavoriteAnimation = quote.id
                                } catch (e: Exception) {
                                    // Error al guardar en BD
                                }
                            }
                        },
                        onShareClick = {
                            // Enhanced sharing con opciones múltiples
                            selectedQuoteForShare = quote
                            showShareBottomSheet = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Espaciado al final
                item {
                    Spacer(modifier = Modifier.height(16.dp))
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
        
        // Bottom Sheet para compartir
        if (showShareBottomSheet && selectedQuoteForShare != null) {
            ShareBottomSheet(
                quote = selectedQuoteForShare!!,
                onDismiss = { 
                    showShareBottomSheet = false 
                    selectedQuoteForShare = null
                }
            )
        }
        
        // Animación de favorito
        LaunchedEffect(showFavoriteAnimation) {
            if (showFavoriteAnimation != null) {
                delay(1000) // Duración de la animación
                showFavoriteAnimation = null
            }
        }
    }
}