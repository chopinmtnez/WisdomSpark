package com.albertowisdom.wisdomspark.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import kotlin.math.*

/**
 * Indicador de pull-to-refresh elegante con animaciones spring-based
 */
@Composable
fun WisdomPullRefreshIndicator(
    refreshing: Boolean,
    @Suppress("UNUSED_PARAMETER") onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    strokeWidth: Dp = 4.dp
) {
    val transition = rememberInfiniteTransition(label = "refresh")
    
    // Animación de rotación durante loading
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Animación de escala al aparecer
    val scale by animateFloatAsState(
        targetValue = if (refreshing) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    // Animación de progreso
    val progress by animateFloatAsState(
        targetValue = if (refreshing) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progress"
    )

    Surface(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.radialGradient(
                colors = listOf(
                    WisdomGold.copy(alpha = 0.3f),
                    Color.White.copy(alpha = 0.1f)
                )
            )
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (refreshing) {
                // Estado de loading con rotación
                Canvas(
                    modifier = Modifier
                        .size(size - 16.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    rotate(rotation) {
                        drawRefreshingIndicator(
                            strokeWidthPx = strokeWidth.toPx(),
                            progress = progress
                        )
                    }
                }
            } else {
                // Estado de preparación
                Canvas(
                    modifier = Modifier
                        .size(size - 16.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    drawPullIndicator(
                        strokeWidthPx = strokeWidth.toPx(),
                        progress = progress
                    )
                }
            }
            
            // Sparkle effect cuando está refreshing
            if (refreshing) {
                SparkleEffect(
                    modifier = Modifier.size(size + 8.dp)
                )
            }
        }
    }
}

/**
 * Dibuja el indicador durante el estado de loading
 */
private fun DrawScope.drawRefreshingIndicator(
    strokeWidthPx: Float,
    progress: Float
) {
    val sweepAngle = 270f * progress
    val startAngle = -90f
    
    // Círculo de fondo
    drawCircle(
        color = WisdomChampagne.copy(alpha = 0.3f),
        radius = size.minDimension / 2 - strokeWidthPx / 2,
        style = Stroke(width = strokeWidthPx)
    )
    
    // Arco de progreso con gradiente
    drawArc(
        brush = Brush.sweepGradient(
            colors = listOf(
                WisdomGold,
                WisdomGold.copy(alpha = 0.7f),
                WisdomGold
            )
        ),
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(
            width = strokeWidthPx,
            cap = StrokeCap.Round
        )
    )
}

/**
 * Dibuja el indicador durante el estado de pull
 */
private fun DrawScope.drawPullIndicator(
    strokeWidthPx: Float,
    progress: Float
) {
    val sweepAngle = 360f * progress
    
    // Círculo de fondo suave
    drawCircle(
        color = WisdomBeige.copy(alpha = 0.4f),
        radius = size.minDimension / 2 - strokeWidthPx / 2,
        style = Stroke(width = strokeWidthPx * 0.5f)
    )
    
    // Arco de progreso
    drawArc(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color.Transparent,
                WisdomGold.copy(alpha = 0.8f),
                WisdomGold,
                WisdomGold.copy(alpha = 0.8f),
                Color.Transparent
            )
        ),
        startAngle = -90f,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(
            width = strokeWidthPx,
            cap = StrokeCap.Round
        )
    )
}

/**
 * Efecto de sparkles durante el refresh
 */
@Composable
private fun SparkleEffect(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "sparkle")
    
    val sparkleRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkleRotation"
    )
    
    val sparkleAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkleAlpha"
    )
    
    Canvas(
        modifier = modifier
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2 - 8.dp.toPx()
        
        // Dibuja múltiples sparkles alrededor del círculo
        for (i in 0 until 8) {
            val angle = (i * 45f + sparkleRotation) * PI / 180f
            val x = centerX + cos(angle) * radius
            val y = centerY + sin(angle) * radius
            
            drawCircle(
                color = WisdomGold.copy(alpha = sparkleAlpha),
                radius = 2.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat())
            )
        }
    }
}

/**
 * Hook para manejar el estado de pull-to-refresh
 */
@Composable
fun rememberPullRefreshState(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    refreshThreshold: Float = 80f
): PullRefreshState {
    return remember {
        PullRefreshState(
            refreshing = refreshing,
            onRefresh = onRefresh,
            refreshThreshold = refreshThreshold
        )
    }
}

/**
 * Estado del pull-to-refresh
 */
class PullRefreshState(
    val refreshing: Boolean,
    val onRefresh: () -> Unit,
    val refreshThreshold: Float
) {
    var pullProgress by mutableStateOf(0f)
        private set
    
    fun updatePullProgress(progress: Float) {
        pullProgress = progress.coerceIn(0f, 1f)
        
        if (progress >= 1f && !refreshing) {
            onRefresh()
        }
    }
}
