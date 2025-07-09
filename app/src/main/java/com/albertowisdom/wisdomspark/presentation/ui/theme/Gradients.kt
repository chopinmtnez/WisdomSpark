package com.albertowisdom.wisdomspark.presentation.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 🎨 Sistema de Gradientes Premium para WisdomSpark
 * Basado en las mejores prácticas de diseño móvil 2024-2025
 */
object WisdomGradients {
    
    // 🌅 GRADIENTE PRINCIPAL - Fondo de aplicación
    val MainBackground = Brush.linearGradient(
        colors = listOf(
            WisdomPearl,
            WisdomBeige,
            WisdomChampagne
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )
    
    // 💎 GRADIENTE PARA CARDS - Glassmorphism base
    val CardBackground = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.9f),
            WisdomChampagne.copy(alpha = 0.8f),
            WisdomBeige.copy(alpha = 0.7f)
        ),
        start = Offset(0f, 0f),
        end = Offset(500f, 500f)
    )
    
    // 🏔️ GRADIENTE PARA HEADER - Efecto de profundidad
    val HeaderBackground = Brush.linearGradient(
        colors = listOf(
            WisdomPearl.copy(alpha = 0.4f),
            WisdomGold.copy(alpha = 0.3f),
            WisdomChampagne.copy(alpha = 0.2f)
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 300f)
    )
    
    // ✨ GRADIENTE PARA BOTONES - Elegante y sutil
    val ButtonBackground = Brush.linearGradient(
        colors = listOf(
            WisdomGold,
            WisdomGold.copy(alpha = 0.8f)
        ),
        start = Offset(0f, 0f),
        end = Offset(200f, 100f)
    )
    
    // 🌟 GRADIENTE PARA FAVORITE BUTTON - Especial
    val FavoriteBackground = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF6B6B),
            Color(0xFFFF8E53)
        ),
        start = Offset(0f, 0f),
        end = Offset(100f, 50f)
    )
    
    // 🔄 GRADIENTE RADIAL - Para efectos especiales
    val RadialAccent = Brush.radialGradient(
        colors = listOf(
            WisdomGold.copy(alpha = 0.3f),
            WisdomChampagne.copy(alpha = 0.1f),
            Color.Transparent
        ),
        radius = 300f
    )
    
    // 🌙 GRADIENTES PARA DARK MODE
    val DarkMainBackground = Brush.linearGradient(
        colors = listOf(
            WisdomDarkSurface,
            WisdomDarkSecondary,
            WisdomDarkSecondary.copy(alpha = 0.8f)
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )
    
    val DarkCardBackground = Brush.linearGradient(
        colors = listOf(
            WisdomDarkSecondary.copy(alpha = 0.9f),
            WisdomDarkSurface.copy(alpha = 0.8f)
        ),
        start = Offset(0f, 0f),
        end = Offset(500f, 500f)
    )
    
    // 💫 GRADIENTE ANGULAR PERSONALIZADO - 45 grados
    val AngularPremium = Brush.linearGradient(
        colors = listOf(
            WisdomPearl,
            WisdomGold.copy(alpha = 0.6f),
            WisdomChampagne
        ),
        start = Offset(0f, 0f),
        end = Offset(300f, 300f) // 45 grados perfecto
    )
    
    // 🎭 GRADIENTE PARA NAVIGATION BAR
    val NavigationBackground = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            WisdomPearl.copy(alpha = 0.8f),
            WisdomBeige.copy(alpha = 0.9f)
        )
    )
    
    // ⭐ GRADIENTE PARA SHIMMER EFFECT (loading)
    val ShimmerEffect = Brush.linearGradient(
        colors = listOf(
            WisdomChampagne.copy(alpha = 0.6f),
            WisdomGold.copy(alpha = 0.2f),
            WisdomChampagne.copy(alpha = 0.6f)
        ),
        start = Offset(-300f, -300f),
        end = Offset(300f, 300f)
    )
}

/**
 * 🛠️ Funciones helper para crear gradientes dinámicos
 */
object GradientHelper {
    
    /**
     * Crea un gradiente personalizado con opacidad
     */
    fun createCustomGradient(
        baseColor: Color,
        opacity: Float = 0.8f,
        angle: Float = 45f
    ): Brush {
        val endOffset = when {
            angle <= 45f -> Offset(300f, 300f)
            angle <= 90f -> Offset(0f, 500f)
            angle <= 135f -> Offset(-300f, 300f)
            else -> Offset(500f, 0f)
        }
        
        return Brush.linearGradient(
            colors = listOf(
                baseColor.copy(alpha = opacity),
                baseColor.copy(alpha = opacity * 0.6f),
                baseColor.copy(alpha = opacity * 0.3f)
            ),
            start = Offset.Zero,
            end = endOffset
        )
    }
    
    /**
     * Gradiente glassmorphic para overlay
     */
    fun glassmorphicOverlay(
        baseColor: Color = Color.White,
        intensity: Float = 0.15f
    ): Brush {
        return Brush.linearGradient(
            colors = listOf(
                baseColor.copy(alpha = intensity),
                baseColor.copy(alpha = intensity * 0.8f),
                baseColor.copy(alpha = intensity * 0.5f)
            )
        )
    }
    
    /**
     * Gradiente de sombra sutil
     */
    fun subtleShadowGradient(
        shadowColor: Color = WisdomCharcoal
    ): Brush {
        return Brush.radialGradient(
            colors = listOf(
                shadowColor.copy(alpha = 0.1f),
                shadowColor.copy(alpha = 0.05f),
                Color.Transparent
            ),
            radius = 200f
        )
    }
}