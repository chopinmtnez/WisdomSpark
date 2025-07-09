package com.albertowisdom.wisdomspark.utils

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Utilidades para optimización de performance en Compose
 */
object PerformanceUtils {
    
    /**
     * Memoiza cálculos costosos de gradientes
     */
    @Composable
    fun rememberGradientColors(
        baseColors: List<Color>,
        alphaFactor: Float = 1f
    ): List<Color> {
        return remember(baseColors, alphaFactor) {
            baseColors.map { color ->
                color.copy(alpha = color.alpha * alphaFactor)
            }
        }
    }

    /**
     * Convierte DP a PX una sola vez y lo memoiza
     */
    @Composable
    fun rememberDpToPx(dp: Dp): Float {
        val density = LocalDensity.current
        return remember(dp, density) {
            with(density) { dp.toPx() }
        }
    }

    /**
     * Memoiza cálculos de animación costosos
     */
    @Composable
    fun rememberAnimationValues(
        animatedValue: Float,
        transform: (Float) -> Float
    ): Float {
        return remember(animatedValue) {
            transform(animatedValue)
        }
    }
}

/**
 * Extensions para optimización de listas
 */
fun <T> List<T>.stableKey(index: Int, keySelector: (T) -> Any): Any {
    return if (index < size) keySelector(this[index]) else index
}

/**
 * Stable data class para evitar recomposiciones innecesarias
 */
@Immutable
data class QuoteDisplayState(
    val text: String,
    val author: String,
    val category: String,
    val isFavorite: Boolean,
    val categoryEmoji: String
) {
    companion object {
        val Empty = QuoteDisplayState(
            text = "",
            author = "",
            category = "",
            isFavorite = false,
            categoryEmoji = "💫"
        )
    }
}

/**
 * Stable holder para evitar recomposiciones de colores
 */
@Immutable
data class ColorPalette(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val accent: Color
) {
    companion object {
        @Stable
        fun wisdom() = ColorPalette(
            primary = Color(0xFFD3C7AB),
            secondary = Color(0xFFA8A5A0),
            background = Color(0xFFF8F5F2),
            surface = Color(0xFFF0E8E0),
            accent = Color(0xFFE8DDD4)
        )
    }
}

/**
 * Helper para evitar boxing en animaciones
 */
@Composable
fun rememberStableFloat(value: Float): State<Float> {
    return rememberUpdatedState(value)
}

/**
 * Cache para paths complejos de drawing
 */
object DrawingCache {
    private val pathCache = mutableMapOf<String, androidx.compose.ui.graphics.Path>()
    
    fun getOrCreatePath(key: String, creator: () -> androidx.compose.ui.graphics.Path): androidx.compose.ui.graphics.Path {
        return pathCache.getOrPut(key, creator)
    }
    
    fun clearCache() {
        pathCache.clear()
    }
}

/**
 * Optimización para content descriptions
 */
object AccessibilityUtils {
    
    @Composable
    fun rememberQuoteContentDescription(
        text: String,
        author: String,
        category: String,
        isFavorite: Boolean
    ): String {
        return remember(text, author, category, isFavorite) {
            buildString {
                append("Cita de $category: ")
                append(text)
                append(". Por $author.")
                if (isFavorite) {
                    append(" Marcada como favorita.")
                }
            }
        }
    }

    @Composable
    fun rememberCategoryContentDescription(
        categoryName: String,
        quoteCount: Int
    ): String {
        return remember(categoryName, quoteCount) {
            "Categoría $categoryName con $quoteCount citas disponibles"
        }
    }
}