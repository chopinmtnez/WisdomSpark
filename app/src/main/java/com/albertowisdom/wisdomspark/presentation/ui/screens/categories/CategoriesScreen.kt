package com.albertowisdom.wisdomspark.presentation.ui.screens.categories

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.ads.BannerAdView
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import com.albertowisdom.wisdomspark.utils.getCategoryEmoji

/**
 * Pantalla de categorías completamente funcional
 */
@Composable
fun CategoriesScreen(
    adMobManager: AdMobManager,
    onCategoryClick: (String) -> Unit = {},
    viewModel: CategoriesViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
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
                Text(
                    text = "📚 Categorías",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = WisdomCharcoal
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Explora citas por temática",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WisdomTaupe
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Contenido según estado
                when {
                    uiState.isLoading -> {
                        LoadingCategoriesSection()
                    }
                    
                    uiState.categories.isNotEmpty() -> {
                        CategoriesGrid(
                            categories = uiState.categories,
                            onCategoryClick = { category ->
                                onCategoryClick(category.name)
                                // Mostrar interstitial ocasionalmente
                                viewModel.onCategoryClicked()
                            }
                        )
                    }
                    
                    else -> {
                        EmptyCategoriesSection(
                            onRetryClick = { viewModel.loadCategories() }
                        )
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
private fun CategoriesGrid(
    categories: List<CategoryItem>,
    onCategoryClick: (CategoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(categories) { category ->
            CategoryCard(
                category = category,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = getCategoryEmoji(category.name),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WisdomCharcoal
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${category.count} citas",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WisdomTaupe
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingCategoriesSection() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = WisdomGold,
                strokeWidth = 3.dp
            )
            
            Text(
                text = "Cargando categorías...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WisdomTaupe
                )
            )
        }
    }
}

@Composable
private fun EmptyCategoriesSection(
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📚",
            style = MaterialTheme.typography.displayLarge
        )
        
        Text(
            text = "No hay categorías disponibles",
            style = MaterialTheme.typography.titleMedium.copy(
                color = WisdomCharcoal,
                fontWeight = FontWeight.Medium
            )
        )
        
        Button(
            onClick = onRetryClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = WisdomGold
            )
        ) {
            Text("Reintentar")
        }
    }
}

// Data classes
data class CategoryItem(
    val name: String,
    val count: Int
)
