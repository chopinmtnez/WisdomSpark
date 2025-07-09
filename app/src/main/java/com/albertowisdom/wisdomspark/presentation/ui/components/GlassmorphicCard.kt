package com.albertowisdom.wisdomspark.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import com.albertowisdom.wisdomspark.presentation.ui.theme.*

/**
 * 🔮 Componente Glassmorphic Premium para WisdomSpark
 * Utiliza Haze library para efectos de glassmorphism de nivel profesional
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState = remember { HazeState() },
    blurRadius: Dp = 25.dp,
    alpha: Float = 0.9f,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color.White.copy(alpha = 0.3f),
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.hazeChild(
            state = hazeState,
            shape = RoundedCornerShape(cornerRadius),
            style = HazeDefaults.style(
                backgroundColor = backgroundColor.copy(alpha = alpha),
                blurRadius = blurRadius
            )
        ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            width = borderWidth,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        content()
    }
}

/**
 * 🌟 Variante Premium con gradiente glassmorphic
 */
@Composable
fun GlassmorphicCardPremium(
    modifier: Modifier = Modifier,
    hazeState: HazeState = remember { HazeState() },
    blurRadius: Dp = 30.dp,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.hazeChild(
            state = hazeState,
            shape = RoundedCornerShape(cornerRadius),
            style = HazeDefaults.style(
                backgroundColor = WisdomPearl.copy(alpha = 0.85f),
                blurRadius = blurRadius
            )
        ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    WisdomGold.copy(alpha = 0.4f),
                    Color.White.copy(alpha = 0.3f),
                    WisdomGold.copy(alpha = 0.4f)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.1f),
                        WisdomChampagne.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                )
            )
        ) {
            content()
        }
    }
}

/**
 * 🎭 Variante para Header con glassmorphism sutil
 */
@Composable
fun GlassmorphicHeader(
    modifier: Modifier = Modifier,
    hazeState: HazeState = remember { HazeState() },
    blurRadius: Dp = 20.dp,
    cornerRadius: Dp = 28.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(cornerRadius),
                style = HazeDefaults.style(
                    backgroundColor = WisdomPearl.copy(alpha = 0.7f),
                    blurRadius = blurRadius
                )
            )
            .clip(RoundedCornerShape(cornerRadius)),
        color = Color.Transparent,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        WisdomGold.copy(alpha = 0.1f),
                        Color.White.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    radius = 200f
                )
            )
        ) {
            content()
        }
    }
}

/**
 * 📱 Variante para Bottom Navigation glassmorphic
 */
@Composable
fun GlassmorphicBottomNav(
    modifier: Modifier = Modifier,
    hazeState: HazeState = remember { HazeState() },
    blurRadius: Dp = 35.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                style = HazeDefaults.style(
                    backgroundColor = WisdomPearl.copy(alpha = 0.9f),
                    blurRadius = blurRadius
                )
            )
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        color = Color.Transparent,
        shadowElevation = 20.dp
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        WisdomBeige.copy(alpha = 0.3f),
                        WisdomChampagne.copy(alpha = 0.2f)
                    )
                )
            )
        ) {
            content()
        }
    }
}

/**
 * 🔧 Variante para elementos flotantes (FAB, Dialogs)
 */
@Composable
fun GlassmorphicFloating(
    modifier: Modifier = Modifier,
    hazeState: HazeState = remember { HazeState() },
    blurRadius: Dp = 40.dp,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(cornerRadius),
                style = HazeDefaults.style(
                    backgroundColor = Color.White.copy(alpha = 0.8f),
                    blurRadius = blurRadius
                )
            )
            .clip(RoundedCornerShape(cornerRadius)),
        color = Color.Transparent,
        shadowElevation = 24.dp
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                )
            )
        ) {
            content()
        }
    }
}

/**
 * 🎨 Helper para crear gradientes glassmorphic personalizados
 */
object GlassmorphicHelper {
    
    /**
     * Crea un gradiente glassmorphic personalizado
     */
    fun createGlassGradient(
        primaryColor: Color = WisdomPearl,
        secondaryColor: Color = WisdomChampagne,
        intensity: Float = 0.1f
    ): Brush {
        return Brush.linearGradient(
            colors = listOf(
                primaryColor.copy(alpha = intensity),
                secondaryColor.copy(alpha = intensity * 0.7f),
                Color.Transparent
            )
        )
    }
    
    /**
     * Gradiente para bordes glassmorphic
     */
    fun createGlassBorder(
        accentColor: Color = WisdomGold,
        intensity: Float = 0.4f
    ): Brush {
        return Brush.linearGradient(
            colors = listOf(
                accentColor.copy(alpha = intensity),
                Color.White.copy(alpha = 0.3f),
                accentColor.copy(alpha = intensity)
            )
        )
    }
}