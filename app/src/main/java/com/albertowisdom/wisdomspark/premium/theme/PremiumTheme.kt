package com.albertowisdom.wisdomspark.premium.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tema Premium con colores dorados y efectos especiales
 */
object PremiumTheme {
    // Colores Premium
    val PremiumGold = Color(0xFFFFD700)
    val PremiumGoldVariant = Color(0xFFFFA500)
    val PremiumDarkGold = Color(0xFFB8860B)
    val PremiumRose = Color(0xFFFFE4E1)
    val PremiumDeepPurple = Color(0xFF2E0854)
    val PremiumLavender = Color(0xFFE6E6FA)
    
    private val LightPremiumColorScheme = lightColorScheme(
        primary = PremiumGold,
        onPrimary = Color.Black,
        primaryContainer = PremiumRose,
        onPrimaryContainer = PremiumDeepPurple,
        secondary = PremiumGoldVariant,
        onSecondary = Color.Black,
        secondaryContainer = PremiumLavender,
        onSecondaryContainer = PremiumDeepPurple,
        tertiary = PremiumDarkGold,
        onTertiary = Color.White,
        error = Color(0xFFBA1A1A),
        errorContainer = Color(0xFFFFDAD6),
        onError = Color.White,
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFFFBFE),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFBFE),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFF4EFF4),
        onSurfaceVariant = Color(0xFF49454E),
        outline = Color(0xFF79747E),
        inverseOnSurface = Color(0xFFF4EFF4),
        inverseSurface = Color(0xFF313033),
        inversePrimary = PremiumGold,
    )

    private val DarkPremiumColorScheme = darkColorScheme(
        primary = PremiumGold,
        onPrimary = Color.Black,
        primaryContainer = PremiumDeepPurple,
        onPrimaryContainer = PremiumGold,
        secondary = PremiumGoldVariant,
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFF3E2723),
        onSecondaryContainer = PremiumGold,
        tertiary = PremiumLavender,
        onTertiary = PremiumDeepPurple,
        error = Color(0xFFFFB4AB),
        errorContainer = Color(0xFF93000A),
        onError = Color(0xFF690005),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1C1B1F),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454E),
        onSurfaceVariant = Color(0xFFCAC4CF),
        outline = Color(0xFF938F99),
        inverseOnSurface = Color(0xFF1C1B1F),
        inverseSurface = Color(0xFFE6E1E5),
        inversePrimary = PremiumDeepPurple,
    )

    @Composable
    fun PremiumColorScheme(darkTheme: Boolean = isSystemInDarkTheme()): ColorScheme {
        return if (darkTheme) {
            DarkPremiumColorScheme
        } else {
            LightPremiumColorScheme
        }
    }
}

/**
 * Wrapper para aplicar tema Premium solo si el usuario es Premium
 */
@Composable
fun PremiumThemeWrapper(
    isPremium: Boolean,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isPremium) {
        PremiumTheme.PremiumColorScheme(darkTheme)
    } else {
        if (darkTheme) darkColorScheme()
        else lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = if (isPremium) PremiumTypography else Typography(),
        content = content
    )
}

/**
 * Tipografía Premium con fuentes mejoradas
 */
val PremiumTypography = Typography(
    displayLarge = Typography().displayLarge.copy(
        color = PremiumTheme.PremiumGold
    ),
    displayMedium = Typography().displayMedium.copy(
        color = PremiumTheme.PremiumGold
    ),
    displaySmall = Typography().displaySmall.copy(
        color = PremiumTheme.PremiumGold
    ),
    headlineLarge = Typography().headlineLarge.copy(
        color = PremiumTheme.PremiumDarkGold
    ),
    headlineMedium = Typography().headlineMedium.copy(
        color = PremiumTheme.PremiumDarkGold
    ),
    headlineSmall = Typography().headlineSmall.copy(
        color = PremiumTheme.PremiumDarkGold
    )
)