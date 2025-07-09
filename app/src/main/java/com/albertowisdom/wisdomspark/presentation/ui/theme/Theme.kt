package com.albertowisdom.wisdomspark.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// **LIGHT COLOR SCHEME - PREMIUM**
private val LightColorScheme = lightColorScheme(
    primary = WisdomGold,                     // Dorado premium
    onPrimary = WisdomCharcoal,               // Texto sobre dorado
    primaryContainer = WisdomChampagne,       // Contenedores principales
    onPrimaryContainer = WisdomCharcoal,      // Texto en contenedores

    secondary = WisdomBeige,                  // Secundario beige
    onSecondary = WisdomCharcoal,             // Texto sobre beige
    secondaryContainer = WisdomPearl,         // Contenedores secundarios
    onSecondaryContainer = WisdomTaupe,       // Texto en contenedores secundarios

    tertiary = WisdomTaupe,                   // Terciario taupe
    onTertiary = WisdomCharcoal,              // Texto sobre taupe
    tertiaryContainer = WisdomBeige,          // Contenedores terciarios
    onTertiaryContainer = WisdomCharcoal,     // Texto en contenedores terciarios

    error = WisdomError,                      // Rojo error
    errorContainer = WisdomError.copy(alpha = 0.1f), // Contenedor error
    onError = Color.White,                    // Texto sobre error
    onErrorContainer = WisdomError,           // Texto en contenedor error

    background = WisdomPearl,                 // Fondo principal
    onBackground = WisdomCharcoal,            // Texto sobre fondo

    surface = Color.White,                    // Superficies
    onSurface = WisdomCharcoal,               // Texto sobre superficies
    surfaceVariant = WisdomBeige,             // Variante de superficie
    onSurfaceVariant = WisdomTaupe,           // Texto sobre variante

    outline = WisdomTaupe.copy(alpha = 0.5f), // Contornos
    outlineVariant = WisdomGold.copy(alpha = 0.3f) // Variante contornos
)

// **DARK COLOR SCHEME - PREMIUM**
private val DarkColorScheme = darkColorScheme(
    primary = WisdomGold,                     // Dorado premium (mismo en dark)
    onPrimary = WisdomCharcoal,               // Texto sobre dorado
    primaryContainer = WisdomDarkSecondary,   // Contenedores principales dark
    onPrimaryContainer = WisdomDarkAccent,    // Texto en contenedores

    secondary = WisdomDarkSecondary,          // Secundario dark
    onSecondary = WisdomDarkText,             // Texto sobre secundario
    secondaryContainer = WisdomDarkTertiary,  // Contenedores secundarios
    onSecondaryContainer = WisdomDarkTextSecondary, // Texto en contenedores

    tertiary = WisdomDarkTertiary,            // Terciario dark
    onTertiary = WisdomDarkText,              // Texto sobre terciario
    tertiaryContainer = WisdomDarkSecondary,  // Contenedores terciarios
    onTertiaryContainer = WisdomDarkText,     // Texto en contenedores

    error = WisdomError,                      // Rojo error
    errorContainer = WisdomError.copy(alpha = 0.2f), // Contenedor error
    onError = Color.White,                    // Texto sobre error
    onErrorContainer = WisdomError,           // Texto en contenedor error

    background = WisdomDarkSurface,           // Fondo dark
    onBackground = WisdomDarkText,            // Texto sobre fondo dark

    surface = WisdomDarkSecondary,            // Superficies dark
    onSurface = WisdomDarkText,               // Texto sobre superficies dark
    surfaceVariant = WisdomDarkTertiary,      // Variante superficie dark
    onSurfaceVariant = WisdomDarkTextSecondary, // Texto sobre variante

    outline = WisdomDarkTextSecondary.copy(alpha = 0.5f), // Contornos dark
    outlineVariant = WisdomGold.copy(alpha = 0.2f) // Variante contornos dark
)

@Composable
fun WisdomSparkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}