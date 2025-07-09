package com.albertowisdom.wisdomspark.utils

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.unit.IntOffset

/**
 * Sistema centralizado de especificaciones de animación para WisdomSpark
 * Implementa spring physics profesionales para micro-interacciones elegantes
 */
object WisdomAnimations {
    
    // ===============================
    // SPRING CONFIGURATIONS
    // ===============================
    
    /**
     * Para botones y elementos interactivos - respuesta rápida con bounce sutil
     */
    val buttonPress = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * Para cards y elementos grandes - movimiento más suave
     */
    val cardHover = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy, 
        stiffness = Spring.StiffnessLow
    )
    
    /**
     * Para transiciones de página - sin bounce para profesionalismo
     */
    val pageTransition = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    
    /**
     * Para elementos que aparecen - entrada elegante
     */
    val elementAppear = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    /**
     * Para feedback de favoritos - bounce divertido
     */
    val favoriteToggle = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    /**
     * Para navegación bottom - transición fluida
     */
    val bottomNavTransition = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    // ===============================
    // TIMING CONSTANTS
    // ===============================
    
    const val MICRO_INTERACTION_DURATION = 200
    const val STANDARD_DURATION = 300
    const val SLOW_DURATION = 500
    const val LOADING_DURATION = 1200
    
    // ===============================
    // EASING CURVES
    // ===============================
    
    val easeInOut = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val easeOut = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val easeIn = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    val emphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    
    // ===============================
    // ENTRANCE ANIMATIONS
    // ===============================
    
    /**
     * Entrada desde arriba con fade
     */
    fun slideInFromTop(
        duration: Int = STANDARD_DURATION,
        delay: Int = 0
    ): EnterTransition {
        return slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(duration, delay, easeOut)
        ) + fadeIn(
            animationSpec = tween(duration, delay)
        )
    }
    
    /**
     * Entrada desde abajo con fade
     */
    fun slideInFromBottom(
        duration: Int = STANDARD_DURATION,
        delay: Int = 0
    ): EnterTransition {
        return slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(duration, delay, easeOut)
        ) + fadeIn(
            animationSpec = tween(duration, delay)
        )
    }
    
    /**
     * Entrada desde la izquierda
     */
    fun slideInFromLeft(
        duration: Int = STANDARD_DURATION,
        delay: Int = 0
    ): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(duration, delay, easeOut)
        ) + fadeIn(
            animationSpec = tween(duration, delay)
        )
    }
    
    /**
     * Entrada desde la derecha
     */
    fun slideInFromRight(
        duration: Int = STANDARD_DURATION,
        delay: Int = 0
    ): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(duration, delay, easeOut)
        ) + fadeIn(
            animationSpec = tween(duration, delay)
        )
    }
    
    /**
     * Entrada con escala - efectos "pop"
     */
    fun scaleInWithFade(
        duration: Int = MICRO_INTERACTION_DURATION,
        delay: Int = 0,
        initialScale: Float = 0.8f
    ): EnterTransition {
        return scaleIn(
            initialScale = initialScale,
            animationSpec = tween(duration, delay, easeOut)
        ) + fadeIn(
            animationSpec = tween(duration, delay)
        )
    }
    
    // ===============================
    // EXIT ANIMATIONS
    // ===============================
    
    /**
     * Salida hacia arriba con fade
     */
    fun slideOutToTop(
        duration: Int = STANDARD_DURATION,
        delay: Int = 0
    ): ExitTransition {
        return slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(duration, delay, easeIn)
        ) + fadeOut(
            animationSpec = tween(duration, delay)
        )
    }
    
    /**
     * Salida hacia abajo con fade
     */
    fun slideOutToBottom(
        duration: Int = STANDARD_DURATION,
        delay: Int = 0
    ): ExitTransition {
        return slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(duration, delay, easeIn)
        ) + fadeOut(
            animationSpec = tween(duration, delay)
        )
    }
    
    /**
     * Salida hacia la izquierda
     */
    fun slideOutToLeft(
        duration: Int = STANDARD_DURATION,
        delay: Int = 0
    ): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(duration, delay, easeIn)
        ) + fadeOut(
            animationSpec = tween(duration, delay)
        )
    }
    
    /**
     * Salida hacia la derecha
     */
    fun slideOutToRight(
        duration: Int = STANDARD_DURATION,
        delay: Int = 0
    ): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(duration, delay, easeIn)
        ) + fadeOut(
            animationSpec = tween(duration, delay)
        )
    }
    
    /**
     * Salida con escala - efectos "pop out"
     */
    fun scaleOutWithFade(
        duration: Int = MICRO_INTERACTION_DURATION,
        delay: Int = 0,
        targetScale: Float = 0.8f
    ): ExitTransition {
        return scaleOut(
            targetScale = targetScale,
            animationSpec = tween(duration, delay, easeIn)
        ) + fadeOut(
            animationSpec = tween(duration, delay)
        )
    }
    
    // ===============================
    // SHIMMER & LOADING ANIMATIONS
    // ===============================
    
    /**
     * Animación de shimmer para loading states
     */
    val shimmerAnimation = infiniteRepeatable<Float>(
        animation = tween(LOADING_DURATION, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )
    
    /**
     * Animación de pulso para elementos de carga
     */
    val pulseAnimation = infiniteRepeatable<Float>(
        animation = tween(1000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
    )
    
    /**
     * Rotación infinita para loading spinners
     */
    val rotationAnimation = infiniteRepeatable<Float>(
        animation = tween(1000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )
    
    // ===============================
    // COMPOSITE ANIMATIONS
    // ===============================
    
    /**
     * Animación de entrada elegante para cards
     */
    fun cardEnterAnimation(
        delayMs: Int = 0
    ): EnterTransition {
        return slideInFromBottom(delay = delayMs) + scaleInWithFade(delay = delayMs)
    }
    
    /**
     * Animación de salida elegante para cards
     */
    fun cardExitAnimation(): ExitTransition {
        return slideOutToTop() + scaleOutWithFade()
    }
    
    /**
     * Transición de página estilo slide
     */
    fun pageSlideTransition(
        isForward: Boolean = true
    ): Pair<EnterTransition, ExitTransition> {
        return if (isForward) {
            slideInFromRight() to slideOutToLeft()
        } else {
            slideInFromLeft() to slideOutToRight()
        }
    }
    
    // ===============================
    // STAGGERED ANIMATIONS
    // ===============================
    
    /**
     * Genera delays escalonados para listas
     */
    fun getStaggeredDelay(
        index: Int,
        baseDelay: Int = 50,
        maxDelay: Int = 300
    ): Int {
        return minOf(index * baseDelay, maxDelay)
    }
    
    /**
     * Animación escalonada para elementos de lista
     */
    fun staggeredListItemAnimation(
        index: Int,
        direction: StaggerDirection = StaggerDirection.BOTTOM_TO_TOP
    ): EnterTransition {
        val delay = getStaggeredDelay(index)
        
        return when (direction) {
            StaggerDirection.BOTTOM_TO_TOP -> slideInFromBottom(delay = delay)
            StaggerDirection.TOP_TO_BOTTOM -> slideInFromTop(delay = delay)
            StaggerDirection.LEFT_TO_RIGHT -> slideInFromLeft(delay = delay)
            StaggerDirection.RIGHT_TO_LEFT -> slideInFromRight(delay = delay)
            StaggerDirection.SCALE_IN -> scaleInWithFade(delay = delay)
        }
    }
    
    enum class StaggerDirection {
        BOTTOM_TO_TOP,
        TOP_TO_BOTTOM,
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        SCALE_IN
    }
    
    // ===============================
    // PHYSICS PRESETS
    // ===============================
    
    /**
     * Spring preset para elementos UI responsivos
     */
    val responsiveSpring = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = 300f
    )
    
    /**
     * Spring preset para animaciones playful
     */
    val playfulSpring = spring<Float>(
        dampingRatio = 0.5f,
        stiffness = 200f
    )
    
    /**
     * Spring preset para animaciones suaves
     */
    val gentleSpring = spring<Float>(
        dampingRatio = 1.0f,
        stiffness = 100f
    )
    
    /**
     * Spring preset para animaciones rápidas
     */
    val snappySpring = spring<Float>(
        dampingRatio = 0.9f,
        stiffness = 400f
    )
}
