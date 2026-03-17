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
 * Singleton para manejar HazeState de manera eficiente
 */
object HazeStateManager {
    val globalHazeState = HazeState()
}

/**
 * Componente Glassmorphic Premium para WisdomSpark
 * Utiliza Haze library para efectos de glassmorphism de nivel profesional.
 * Todos los colores se resuelven desde MaterialTheme.colorScheme para
 * funcionar correctamente en light y dark mode.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState = HazeStateManager.globalHazeState,
    blurRadius: Dp = 25.dp,
    alpha: Float = 0.9f,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
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
 * Variante Premium con gradiente glassmorphic.
 * Theme-aware: usa colorScheme para adaptarse a light/dark mode.
 */
@Composable
fun GlassmorphicCardPremium(
    modifier: Modifier = Modifier,
    hazeState: HazeState = HazeStateManager.globalHazeState,
    blurRadius: Dp = 30.dp,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier.hazeChild(
            state = hazeState,
            shape = RoundedCornerShape(cornerRadius),
            style = HazeDefaults.style(
                backgroundColor = colorScheme.background.copy(alpha = 0.85f),
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
                    colorScheme.primary.copy(alpha = 0.4f),
                    colorScheme.surface.copy(alpha = 0.3f),
                    colorScheme.primary.copy(alpha = 0.4f)
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
                        colorScheme.surface.copy(alpha = 0.1f),
                        colorScheme.primaryContainer.copy(alpha = 0.05f),
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
 * Variante para Header con glassmorphism sutil.
 * Theme-aware: usa colorScheme para adaptarse a light/dark mode.
 */
@Composable
fun GlassmorphicHeader(
    modifier: Modifier = Modifier,
    hazeState: HazeState = HazeStateManager.globalHazeState,
    blurRadius: Dp = 20.dp,
    cornerRadius: Dp = 28.dp,
    content: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(cornerRadius),
                style = HazeDefaults.style(
                    backgroundColor = colorScheme.background.copy(alpha = 0.7f),
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
                        colorScheme.primary.copy(alpha = 0.1f),
                        colorScheme.surface.copy(alpha = 0.05f),
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
 * Variante para Bottom Navigation glassmorphic.
 * Theme-aware: usa colorScheme para adaptarse a light/dark mode.
 */
@Composable
fun GlassmorphicBottomNav(
    modifier: Modifier = Modifier,
    hazeState: HazeState = HazeStateManager.globalHazeState,
    blurRadius: Dp = 35.dp,
    content: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                style = HazeDefaults.style(
                    backgroundColor = colorScheme.background.copy(alpha = 0.9f),
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
                        colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                )
            )
        ) {
            content()
        }
    }
}

/**
 * Variante para elementos flotantes (FAB, Dialogs).
 * Theme-aware: usa colorScheme para adaptarse a light/dark mode.
 */
@Composable
fun GlassmorphicFloating(
    modifier: Modifier = Modifier,
    hazeState: HazeState = HazeStateManager.globalHazeState,
    blurRadius: Dp = 40.dp,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(cornerRadius),
                style = HazeDefaults.style(
                    backgroundColor = colorScheme.surface.copy(alpha = 0.8f),
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
                        colorScheme.surface.copy(alpha = 0.2f),
                        colorScheme.surface.copy(alpha = 0.1f),
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