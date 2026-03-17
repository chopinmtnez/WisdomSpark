package com.albertowisdom.wisdomspark.presentation.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.albertowisdom.wisdomspark.R
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.ads.BannerAdView
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import com.albertowisdom.wisdomspark.utils.ShareUtils
import com.albertowisdom.wisdomspark.utils.getCategoryEmoji
import com.albertowisdom.wisdomspark.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * SwipeableHomeScreen - Stack de cartas completamente funcional
 * Solucionado el problema de solapamiento con z-index apropiado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableHomeScreen(
    adMobManager: AdMobManager,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    // Stack de citas para demostración expandido
    val sampleQuotes = remember {
        listOf(
            Quote(
                id = 1,
                text = "El éxito es la suma de pequeños esfuerzos repetidos día tras día.",
                author = "Robert Collier",
                category = "Motivación",
                isFavorite = false
            ),
            Quote(
                id = 2,
                text = "No esperes por el momento perfecto. Toma el momento y hazlo perfecto.",
                author = "Anónimo",
                category = "Vida",
                isFavorite = false
            ),
            Quote(
                id = 3,
                text = "La mejor manera de predecir el futuro es creándolo.",
                author = "Peter Drucker",
                category = "Sueños",
                isFavorite = false
            ),
            Quote(
                id = 4,
                text = "Las oportunidades no suceden. Las creas.",
                author = "Chris Grosser",
                category = "Éxito",
                isFavorite = false
            ),
            Quote(
                id = 5,
                text = "El único modo de hacer un gran trabajo es amar lo que haces.",
                author = "Steve Jobs",
                category = "Perseverancia",
                isFavorite = false
            ),
            Quote(
                id = 6,
                text = "La vida es 10% lo que te sucede y 90% cómo reaccionas a ello.",
                author = "Charles R. Swindoll",
                category = "Felicidad",
                isFavorite = false
            )
        )
    }

    // Estado para manejar las cartas restantes en el stack
    var remainingQuotes by remember { mutableStateOf(listOf<Quote>()) }

    // Inicializar el stack cuando carga la cita del día (solo si cambia el ID)
    LaunchedEffect(uiState.todayQuote?.id) {
        if (uiState.todayQuote != null) {
            remainingQuotes = buildList {
                // Agregar la cita del día como primera si está disponible
                add(uiState.todayQuote!!)
                // Agregar citas de muestra
                addAll(sampleQuotes)
            }
        }
    }

    // Función para remover la carta superior y avanzar
    fun removeTopCard() {
        if (remainingQuotes.isNotEmpty()) {
            remainingQuotes = remainingQuotes.drop(1)

            // Si se acaban las cartas, recargar
            if (remainingQuotes.isEmpty()) {
                viewModel.generateNewQuotes() // Esto disparará LaunchedEffect nuevamente
            }
        }
    }

    // Fondo con gradiente
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = getThemedGradientColors()
                )
            )
    ) {
        // Patrones decorativos
        DecorativeBackground()

        // Contenido principal - independiente del banner
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp) // Espacio reservado para el banner
        ) {
            // Header
            SwipeableHeader(onNavigateToSettings = onNavigateToSettings)

            // Contenido según estado
            when {
                uiState.isLoading && remainingQuotes.isEmpty() -> {
                    LoadingStateSwipeable()
                }
                uiState.error != null && remainingQuotes.isEmpty() -> {
                    ErrorStateSwipeable(
                        error = uiState.error!!,
                        onRetryClick = { viewModel.generateNewQuotes() }
                    )
                }
                remainingQuotes.isNotEmpty() -> {
                    // Stack de cartas principal con z-index correcto
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp)
                    ) {
                        // Renderizar las cartas en orden inverso para z-index correcto
                        val visibleCards = remainingQuotes.take(3)

                        visibleCards.reversed().forEachIndexed { reverseIndex, quote ->
                            val cardIndex = visibleCards.size - 1 - reverseIndex
                            val stackLevel = cardIndex

                            // Key para estabilidad de Compose
                            key(quote.id) {
                                SwipeableQuoteCard(
                                    quote = quote,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .zIndex((10 - stackLevel).toFloat()) // Z-index correcto
                                        .testTag("swipeable_quote_card"),
                                    isActive = stackLevel == 0,
                                    stackLevel = stackLevel,
                                    onSwipeLeft = remember {
                                        {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            // Swipe izquierda: solo pasar/rechazar
                                            removeTopCard()
                                        }
                                    },
                                    onSwipeRight = remember {
                                        {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            // Swipe derecha: agregar a favoritos
                                            val isQuoteOfTheDay = uiState.todayQuote?.let {
                                                it.id == quote.id
                                            } ?: false

                                            if (isQuoteOfTheDay) {
                                                viewModel.toggleFavorite()
                                            }
                                            removeTopCard()
                                        }
                                    },
                                    onSwipeUp = remember {
                                        {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            // Swipe arriba: compartir quote
                                            ShareUtils.shareQuote(context, quote)
                                            removeTopCard()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Indicador de progreso dinámico - optimizado con derivedStateOf
                    val progressState by remember {
                        derivedStateOf {
                            val totalQuotes = (if (uiState.todayQuote != null) 1 else 0) + sampleQuotes.size
                            val remainingCount = remainingQuotes.size
                            val currentIndex = totalQuotes - remainingCount
                            Triple(currentIndex, totalQuotes, remainingCount)
                        }
                    }
                    val (currentIndex, totalQuotes, remainingCount) = progressState

                    ProgressIndicator(
                        currentIndex = currentIndex,
                        total = totalQuotes,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .testTag("progress_indicator")
                    )
                }
                else -> {
                    // Estado cuando no hay cartas - mostrar mensaje
                    NoMoreCardsState(
                        onRefreshClick = { viewModel.generateNewQuotes() },
                        modifier = Modifier.testTag("no_more_cards")
                    )
                }
            }

            // Bottom action buttons - solo mostrar si hay cartas
            if (remainingQuotes.isNotEmpty()) {
                BottomActionButtons(
                    onPassClick = {
                        removeTopCard()
                    },
                    onLikeClick = {
                        val currentQuote = remainingQuotes.firstOrNull()
                        val isQuoteOfTheDay = currentQuote?.let { quote ->
                            uiState.todayQuote?.id == quote.id
                        } ?: false

                        if (isQuoteOfTheDay) {
                            viewModel.toggleFavorite()
                        }
                        removeTopCard()
                    },
                    onSuperLikeClick = {
                        val currentQuote = remainingQuotes.firstOrNull()
                        if (currentQuote != null) {
                            // Super like: solo compartir
                            ShareUtils.shareQuote(context, currentQuote)
                        }
                        removeTopCard()
                    }
                )
            }
        }

        // Banner Ad - posicionado de forma absoluta en la parte inferior
        if (adMobManager.shouldShowAds()) {
            BannerAdView(
                onAdLoaded = { adMobManager.onBannerAdLoaded() },
                onAdFailedToLoad = { error ->
                    adMobManager.onBannerAdFailedToLoad(error)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DecorativeBackground() {
    val decorativeColor1 = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    val decorativeColor2 = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path1 = Path().apply {
            moveTo(size.width * 0.8f, 0f)
            lineTo(size.width, size.height * 0.25f)
            lineTo(size.width, 0f)
            close()
        }
        drawPath(
            path = path1,
            color = decorativeColor1
        )

        val path2 = Path().apply {
            moveTo(0f, size.height * 0.75f)
            lineTo(size.width * 0.2f, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = path2,
            color = decorativeColor2
        )
    }
}

@Composable
private fun SwipeableHeader(onNavigateToSettings: () -> Unit = {}) {
    val context = LocalContext.current
    val today = remember(context) {
        DateUtils.getCurrentDateFormatted(context)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = stringResource(R.string.app_title),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = stringResource(R.string.swipe_to_discover),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = today,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun LoadingStateSwipeable() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.preparing_wisdom_ellipsis),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorStateSwipeable(
    error: String,
    onRetryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.oops),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onRetryClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun NoMoreCardsState(
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "Complete",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.amazing),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.reviewed_all_wisdom),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onRefreshClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.more_wisdom))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressIndicator(
    currentIndex: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(0.6f)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(minOf(total, 8)) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .background(
                        color = if (index < currentIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
private fun BottomActionButtons(
    onPassClick: () -> Unit,
    onLikeClick: () -> Unit,
    onSuperLikeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pass button
        FloatingActionButton(
            onClick = onPassClick,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(56.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp
            )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Pass",
                modifier = Modifier.size(24.dp)
            )
        }

        // Super like button (center, bigger)
        FloatingActionButton(
            onClick = onSuperLikeClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(72.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 12.dp
            )
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = "Super Like",
                modifier = Modifier.size(32.dp)
            )
        }

        // Like button
        FloatingActionButton(
            onClick = onLikeClick,
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(56.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp
            )
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = "Like",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SwipeableQuoteCard(
    quote: Quote,
    modifier: Modifier = Modifier,
    isActive: Boolean,
    stackLevel: Int,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotation by remember { mutableFloatStateOf(0f) }

    val swipeThreshold = 200f

    // Calcular transformaciones del stack - memoizadas para evitar cálculos repetidos
    val stackOffset = remember(stackLevel) { (stackLevel * 8).dp }
    val stackScale = remember(stackLevel) { 1f - (stackLevel * 0.04f) }
    val stackAlpha = remember(stackLevel) { 
        if (stackLevel == 0) 1f else 0.8f - (stackLevel * 0.2f) 
    }

    // Animaciones para reset automático - memoizadas para mejor rendimiento
    val springAnimSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    }
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = springAnimSpec,
        label = "offsetX"
    )

    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = springAnimSpec,
        label = "offsetY"
    )

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = springAnimSpec,
        label = "rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(500.dp)
            .offset(y = stackOffset) // Offset del stack
            .scale(stackScale) // Escala del stack
            .alpha(stackAlpha) // Alpha del stack
            .offset {
                // Solo aplicar offset de drag a la carta activa
                if (isActive) {
                    IntOffset(
                        animatedOffsetX.roundToInt(),
                        animatedOffsetY.roundToInt()
                    )
                } else {
                    IntOffset.Zero
                }
            }
            .graphicsLayer {
                // Solo aplicar rotación a la carta activa
                if (isActive) {
                    rotationZ = animatedRotation
                }
            }
            .pointerInput(isActive) {
                if (isActive) {
                    detectDragGestures(
                        onDragEnd = {
                            val swipeDistance = kotlin.math.sqrt(offsetX * offsetX + offsetY * offsetY)
                            
                            when {
                                // Swipe arriba (compartir)
                                offsetY < -swipeThreshold && kotlin.math.abs(offsetY) > kotlin.math.abs(offsetX) -> {
                                    // Resetear inmediatamente antes de la acción
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotation = 0f
                                    onSwipeUp()
                                }
                                // Swipe derecha (favorito)
                                offsetX > swipeThreshold && kotlin.math.abs(offsetX) > kotlin.math.abs(offsetY) -> {
                                    // Resetear inmediatamente antes de la acción
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotation = 0f
                                    onSwipeRight()
                                }
                                // Swipe izquierda (pasar)
                                offsetX < -swipeThreshold && kotlin.math.abs(offsetX) > kotlin.math.abs(offsetY) -> {
                                    // Resetear inmediatamente antes de la acción
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotation = 0f
                                    onSwipeLeft()
                                }
                                // Si no alcanza el threshold, resetear suavemente
                                else -> {
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotation = 0f
                                }
                            }
                        }
                    ) { _, dragAmount ->
                        // Actualizar posición durante el drag
                        offsetX += dragAmount.x * 0.8f // Reducir sensibilidad
                        offsetY += dragAmount.y * 0.8f
                        
                        // Calcular rotación basada en posición X
                        rotation = (offsetX / 10f).coerceIn(-45f, 45f)
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 16.dp else (8 - stackLevel * 2).dp
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                Color.Transparent,
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.12f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Category badge at top
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "${getCategoryEmoji(quote.category)} ${quote.category}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quote text - direct approach for maximum compatibility
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Background for better contrast on problematic devices
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = quote.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 18.sp,
                                lineHeight = 26.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Author at bottom - guaranteed visible with background
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Simple decorative line
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Author with background for visibility
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = quote.author,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Swipe indicators - solo mostrar en carta activa
            if (isActive) {
                SwipeIndicators(offsetX, offsetY)
            }

            // Favorite indicator
            if (quote.isFavorite) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                            CircleShape
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Favorited",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.SwipeIndicators(offsetX: Float, offsetY: Float) {
    if (offsetX > 50) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(32.dp)
                .background(
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                    CircleShape
                )
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = "Like",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .size(48.dp)
                    .rotate(15f)
            )
        }
    }

    if (offsetX < -50) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(32.dp)
                .background(
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    CircleShape
                )
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Pass",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(48.dp)
                    .rotate(-15f)
            )
        }
    }

    if (offsetY < -50) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(32.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    CircleShape
                )
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = "Super Like",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}