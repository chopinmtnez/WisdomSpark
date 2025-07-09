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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.ads.BannerAdView
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import com.albertowisdom.wisdomspark.utils.ShareUtils
import com.albertowisdom.wisdomspark.utils.getCategoryEmoji
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

    // Inicializar el stack cuando carga la cita del día
    LaunchedEffect(uiState.todayQuote) {
        remainingQuotes = buildList {
            // Agregar la cita del día como primera si está disponible
            uiState.todayQuote?.let { add(it) }
            // Agregar citas de muestra
            addAll(sampleQuotes)
        }
    }

    // Función para remover la carta superior y avanzar
    fun removeTopCard() {
        if (remainingQuotes.isNotEmpty()) {
            remainingQuotes = remainingQuotes.drop(1)

            // Si se acaban las cartas, recargar
            if (remainingQuotes.isEmpty()) {
                viewModel.loadTodayQuote() // Esto disparará LaunchedEffect nuevamente
            }
        }
    }

    // Fondo con gradiente
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = getWisdomGradientColors()
                )
            )
    ) {
        // Patrones decorativos
        DecorativeBackground()

        // Contenido principal
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            SwipeableHeader()

            // Contenido según estado
            when {
                uiState.isLoading && remainingQuotes.isEmpty() -> {
                    LoadingStateSwipeable()
                }
                uiState.error != null && remainingQuotes.isEmpty() -> {
                    ErrorStateSwipeable(
                        error = uiState.error!!,
                        onRetryClick = { viewModel.loadTodayQuote() }
                    )
                }
                remainingQuotes.isNotEmpty() -> {
                    // Stack de cartas principal con z-index correcto
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp, bottom = 160.dp)
                    ) {
                        // Renderizar las cartas en orden inverso para z-index correcto
                        val visibleCards = remainingQuotes.take(3)

                        visibleCards.reversed().forEachIndexed { reverseIndex, quote ->
                            val cardIndex = visibleCards.size - 1 - reverseIndex
                            val stackLevel = cardIndex

                            SwipeableQuoteCard(
                                quote = quote,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex((10 - stackLevel).toFloat()), // Z-index correcto
                                isActive = stackLevel == 0,
                                stackLevel = stackLevel,
                                onSwipeLeft = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    removeTopCard()
                                },
                                onSwipeRight = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // Solo aplicar favorito si es la cita del día (primera en la lista original)
                                    val isQuoteOfTheDay = uiState.todayQuote?.let {
                                        it.id == quote.id
                                    } ?: false

                                    if (isQuoteOfTheDay) {
                                        viewModel.toggleFavorite()
                                    }
                                    removeTopCard()
                                },
                                onSwipeUp = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // Solo aplicar favorito si es la cita del día
                                    val isQuoteOfTheDay = uiState.todayQuote?.let {
                                        it.id == quote.id
                                    } ?: false

                                    if (isQuoteOfTheDay) {
                                        viewModel.toggleFavorite()
                                    }
                                    ShareUtils.shareQuote(context, quote)
                                    removeTopCard()
                                }
                            )
                        }
                    }

                    // Indicador de progreso dinámico
                    val totalQuotes = (if (uiState.todayQuote != null) 1 else 0) + sampleQuotes.size
                    val remainingCount = remainingQuotes.size
                    val currentIndex = totalQuotes - remainingCount

                    ProgressIndicator(
                        currentIndex = currentIndex,
                        total = totalQuotes,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                else -> {
                    // Estado cuando no hay cartas - mostrar mensaje
                    NoMoreCardsState(
                        onRefreshClick = { viewModel.loadTodayQuote() }
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
                            val isQuoteOfTheDay = uiState.todayQuote?.id == currentQuote.id

                            if (isQuoteOfTheDay) {
                                viewModel.toggleFavorite()
                            }
                            ShareUtils.shareQuote(context, currentQuote)
                        }
                        removeTopCard()
                    }
                )
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
private fun DecorativeBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path1 = Path().apply {
            moveTo(size.width * 0.8f, 0f)
            lineTo(size.width, size.height * 0.25f)
            lineTo(size.width, 0f)
            close()
        }
        drawPath(
            path = path1,
            color = WisdomGold.copy(alpha = 0.06f)
        )

        val path2 = Path().apply {
            moveTo(0f, size.height * 0.75f)
            lineTo(size.width * 0.2f, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = path2,
            color = WisdomGold.copy(alpha = 0.04f)
        )
    }
}

@Composable
private fun SwipeableHeader() {
    val today = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES"))
            .format(Date())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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
            Column {
                Text(
                    text = "WisdomSpark",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = WisdomCharcoal
                    )
                )
                Text(
                    text = "Desliza para descubrir",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WisdomTaupe
                    )
                )
            }

            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                IconButton(onClick = { /* settings */ }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = WisdomCharcoal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = today,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = WisdomTaupe,
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
                containerColor = Color.White.copy(alpha = 0.95f)
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
                        color = WisdomGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Preparando sabiduría...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = WisdomTaupe
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
                containerColor = Color.White.copy(alpha = 0.95f)
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
                        tint = WisdomError
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Oops...",
                        style = MaterialTheme.typography.headlineSmall,
                        color = WisdomError
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WisdomTaupe,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onRetryClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WisdomGold
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}

@Composable
private fun NoMoreCardsState(
    onRefreshClick: () -> Unit
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
                containerColor = Color.White.copy(alpha = 0.95f)
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
                        tint = WisdomGold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¡Increíble!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WisdomCharcoal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Has revisado toda la sabiduría de hoy",
                        style = MaterialTheme.typography.bodyLarge,
                        color = WisdomTaupe,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onRefreshClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WisdomGold
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Más Sabiduría")
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
                        color = if (index < currentIndex) WisdomGold else WisdomGold.copy(alpha = 0.3f),
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
            containerColor = Color.White,
            contentColor = WisdomError,
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
            containerColor = WisdomGold,
            contentColor = Color.White,
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
            containerColor = WisdomSuccess,
            contentColor = Color.White,
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

    // Calcular transformaciones del stack
    val stackOffset = (stackLevel * 8).dp
    val stackScale = 1f - (stackLevel * 0.04f)
    val stackAlpha = if (stackLevel == 0) 1f else 0.8f - (stackLevel * 0.2f)

    // Animaciones para reset automático
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "offsetX"
    )

    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "offsetY"
    )

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
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
                            when {
                                abs(offsetX) > swipeThreshold -> {
                                    if (offsetX > 0) {
                                        onSwipeRight()
                                    } else {
                                        onSwipeLeft()
                                    }
                                    // Reset inmediato
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotation = 0f
                                }
                                offsetY < -swipeThreshold -> {
                                    onSwipeUp()
                                    // Reset inmediato
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotation = 0f
                                }
                                else -> {
                                    // Reset suave
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotation = 0f
                                }
                            }
                        }
                    ) { change, _ ->
                        offsetX += change.position.x
                        offsetY += change.position.y
                        rotation = (offsetX / 20f).coerceIn(-15f, 15f)
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
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
                                WisdomGold.copy(alpha = 0.08f),
                                Color.Transparent,
                                WisdomChampagne.copy(alpha = 0.12f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Category badge
                Surface(
                    color = WisdomGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "${getCategoryEmoji(quote.category)} ${quote.category}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = WisdomCharcoal,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Quote text with quotes decoration
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\"",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 80.sp,
                            lineHeight = 60.sp
                        ),
                        color = WisdomGold.copy(alpha = 0.3f),
                        modifier = Modifier.offset(x = (-10).dp, y = 10.dp)
                    )

                    Text(
                        text = quote.text,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 20.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        ),
                        color = WisdomCharcoal,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Author with decorative line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(2.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        WisdomGold,
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = quote.author,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.02.em
                        ),
                        color = WisdomTaupe,
                        textAlign = TextAlign.Center
                    )
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
                            WisdomError.copy(alpha = 0.9f),
                            CircleShape
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Favorited",
                        tint = Color.White,
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
                    WisdomSuccess.copy(alpha = 0.1f),
                    CircleShape
                )
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = "Like",
                tint = WisdomSuccess,
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
                    WisdomError.copy(alpha = 0.1f),
                    CircleShape
                )
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Pass",
                tint = WisdomError,
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
                    WisdomGold.copy(alpha = 0.1f),
                    CircleShape
                )
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = "Super Like",
                tint = WisdomGold,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}