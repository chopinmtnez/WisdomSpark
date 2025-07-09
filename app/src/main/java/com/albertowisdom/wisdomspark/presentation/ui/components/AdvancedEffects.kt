package com.albertowisdom.wisdomspark.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import com.albertowisdom.wisdomspark.utils.PerformanceUtils
import kotlin.math.*

/**
 * Efectos visuales avanzados para WisdomSpark
 */

/**
 * Gradiente animado con rotación sutil
 */
@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(WisdomPearl, WisdomBeige, WisdomChampagne),
    animationDurationMs: Int = 8000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_rotation")
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val optimizedColors = PerformanceUtils.rememberGradientColors(colors)

    Canvas(modifier = modifier.fillMaxSize()) {
        rotate(rotation.value, center) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = optimizedColors,
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
            )
        }
    }
}

/**
 * Parallax effect sutil para elementos de scroll
 */
@Composable
fun ParallaxCard(
    listState: LazyListState,
    index: Int,
    modifier: Modifier = Modifier,
    parallaxFactor: Float = 0.3f,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    
    val parallaxOffset by remember {
        derivedStateOf {
            val itemInfo = listState.layoutInfo.visibleItemsInfo
                .find { it.index == index }
            
            if (itemInfo != null) {
                val scrollOffset = itemInfo.offset.toFloat()
                val itemHeight = itemInfo.size.toFloat()
                val viewportCenter = listState.layoutInfo.viewportSize.height / 2f
                
                ((scrollOffset - viewportCenter + itemHeight / 2f) * parallaxFactor)
                    .coerceIn(-itemHeight / 2f, itemHeight / 2f)
            } else {
                0f
            }
        }
    }

    Box(
        modifier = modifier.offset(y = with(density) { parallaxOffset.toDp() })
    ) {
        content()
    }
}

/**
 * Efecto de profundidad con múltiples layers
 */
@Composable
fun DepthEffectCard(
    modifier: Modifier = Modifier,
    elevation: Float = 1f,
    content: @Composable () -> Unit
) {
    val animatedElevation by animateFloatAsState(
        targetValue = elevation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "elevation"
    )

    Box(modifier = modifier) {
        // Shadow layers para crear profundidad
        repeat(3) { index ->
            val alpha = (0.1f * (3 - index) * animatedElevation).coerceIn(0f, 0.3f)
            val offset = (2.dp * (index + 1) * animatedElevation).coerceAtLeast(0.dp)
            
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = offset, y = offset),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = WisdomCharcoal.copy(alpha = alpha)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {}
        }

        // Contenido principal
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = (8.dp * animatedElevation).coerceAtLeast(4.dp)
            )
        ) {
            content()
        }
    }
}

/**
 * Efecto de breathing para elementos clave
 */
@Composable
fun BreathingEffect(
    modifier: Modifier = Modifier,
    minScale: Float = 0.98f,
    maxScale: Float = 1.02f,
    durationMs: Int = 3000,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale = infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier.graphicsLayer(scaleX = scale.value, scaleY = scale.value)
    ) {
        content()
    }
}

/**
 * Morphing gradient que cambia suavemente
 */
@Composable
fun MorphingGradient(
    modifier: Modifier = Modifier,
    colorSets: List<List<Color>> = listOf(
        listOf(WisdomPearl, WisdomGold),
        listOf(WisdomBeige, WisdomChampagne),
        listOf(WisdomChampagne, WisdomTaupe)
    ),
    cycleDurationMs: Int = 5000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "morphing")
    val progress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = colorSets.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(cycleDurationMs * colorSets.size, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val currentColors by remember {
        derivedStateOf {
            val index = progress.value.toInt() % colorSets.size
            val nextIndex = (index + 1) % colorSets.size
            val fraction = progress.value - progress.value.toInt()

            val current = colorSets[index]
            val next = colorSets[nextIndex]

            current.zip(next) { currentColor, nextColor ->
                lerp(currentColor, nextColor, fraction)
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.linearGradient(
                colors = currentColors,
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
        )
    }
}

/**
 * Efecto de floating para elementos interactivos
 */
@Composable
fun FloatingEffect(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    floatDistance: Float = 8f,
    content: @Composable () -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue = if (isActive) -floatDistance else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "float_offset"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isActive) 12f else 4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "float_elevation"
    )

    Box(
        modifier = modifier
            .offset(y = offsetY.dp)
            .graphicsLayer(shadowElevation = elevation)
    ) {
        content()
    }
}

/**
 * Partículas sutiles para efectos premium
 */
@Composable
fun SubtleParticles(
    modifier: Modifier = Modifier,
    particleCount: Int = 8,
    color: Color = WisdomGold.copy(alpha = 0.3f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    // Crear las animaciones directamente como una lista de State<Float>
    val particleAnimations = List(particleCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 8000 + index * 500,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "particle_anim_$index"
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particleAnimations.forEachIndexed { index, animation ->
            val phase = (index * PI * 2 / particleCount).toFloat()
            val animationValue = animation.value

            val x = size.width * 0.5f + cos(animationValue + phase) * size.width * 0.3f
            val y = size.height * 0.5f + sin(animationValue + phase) * size.height * 0.2f
            val radius = (2f + sin(animationValue * 2 + phase) * 1f).coerceAtLeast(0.5f)

            drawCircle(
                color = color,
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Helper function para interpolación de colores
 */
private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = lerp(start.red, stop.red, fraction),
        green = lerp(start.green, stop.green, fraction),
        blue = lerp(start.blue, stop.blue, fraction),
        alpha = lerp(start.alpha, stop.alpha, fraction)
    )
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}