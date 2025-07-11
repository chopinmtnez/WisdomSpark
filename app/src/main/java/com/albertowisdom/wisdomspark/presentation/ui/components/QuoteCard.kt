package com.albertowisdom.wisdomspark.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import com.albertowisdom.wisdomspark.utils.ShareUtils
import kotlinx.coroutines.delay

@Composable
fun QuoteCard(
    quote: Quote,
    onFavoriteClick: () -> Unit,
    onShareClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    userPreferences: UserPreferences? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    // Obtener el estado de las preferencias de feedback háptico
    val isHapticEnabled by (userPreferences?.isHapticFeedbackEnabled?.collectAsState(initial = true) ?: remember { mutableStateOf(true) })
    
    // Estados de animación
    var isPressed by remember { mutableStateOf(false) }
    
    // Animaciones
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )
    
    val elevationAnimation by animateFloatAsState(
        targetValue = if (isPressed) 8f else 12f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "elevation"
    )

    // Gradientes dinámicos que respetan el tema
    val gradientColors = getThemedGradientColors()
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            gradientColors[0].copy(alpha = 0.8f),
            gradientColors[1].copy(alpha = 0.6f)
        )
    )
    
    val borderGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    )

    // Emoji dinámico por categoría
    val categoryEmoji = getCategoryEmoji(quote.category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clip(RoundedCornerShape(24.dp))
            .semantics {
                contentDescription = "Cita de ${quote.author}: ${quote.text}"
                role = Role.Button
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevationAnimation.dp
        ),
        border = BorderStroke(
            width = 1.5.dp,
            brush = borderGradient
        )
    ) {
        Box(
            modifier = Modifier
                .background(cardGradient)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header con categoría
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge de categoría con emoji
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = categoryEmoji,
                                fontSize = 16.sp
                            )
                            Text(
                                text = quote.category,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Texto principal de la cita con animación de entrada
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                    ) + fadeIn(),
                    label = "quoteTextAnimation"
                ) {
                    Text(
                        text = "\"${quote.text}\"",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Línea decorativa dorada
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(2.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(1.dp)
                        )
                )

                // Autor con estilo elegante
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )

                // Botones de acción con animaciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de favorito
                    ActionButton(
                        onClick = {
                            if (isHapticEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onFavoriteClick()
                        },
                        icon = if (quote.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (quote.isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (quote.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Botón de compartir (condicional)
                    if (onShareClick != null) {
                        ActionButton(
                            onClick = {
                                if (isHapticEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onShareClick()
                            },
                            icon = Icons.Default.Share,
                            contentDescription = "Compartir cita",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Botón de compartir con funcionalidad integrada
                        ActionButton(
                            onClick = {
                                if (isHapticEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                ShareUtils.shareQuote(context, quote)
                            },
                            icon = Icons.Default.Share,
                            contentDescription = "Compartir cita",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    // Estado local del botón
    var buttonPressed by remember { mutableStateOf(false) }
    
    val buttonScale by animateFloatAsState(
        targetValue = if (buttonPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )
    
    val surfaceColor by animateColorAsState(
        targetValue = if (buttonPressed) 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
        else 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        animationSpec = tween(150),
        label = "surfaceColor"
    )

    Surface(
        onClick = {
            buttonPressed = true
            onClick()
        },
        modifier = modifier
            .size(56.dp)
            .scale(buttonScale)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
    
    // Reset del estado después del click
    LaunchedEffect(buttonPressed) {
        if (buttonPressed) {
            delay(150)
            buttonPressed = false
        }
    }
}

// Función para obtener emoji por categoría
private fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "motivación" -> "🔥"
        "vida" -> "🌱"
        "sueños" -> "✨"
        "perseverancia" -> "💪"
        "educación" -> "📚"
        "creatividad" -> "🎨"
        "éxito" -> "🏆"
        "autenticidad" -> "💎"
        "felicidad" -> "😊"
        "sabiduría" -> "🦉"
        "confianza" -> "🌟"
        "progreso" -> "📈"
        "excelencia" -> "👑"
        "acción" -> "⚡"
        else -> "💫"
    }
}