package com.albertowisdom.wisdomspark.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// **PALETA PREMIUM - SOFT LUXURY GRADIENTS**
// Inspirada en Calm, Headspace y Things 3

// **COLORES PRINCIPALES LIGHT MODE**
val WisdomPearl = Color(0xFFF8F5F2)           // Fondo principal - perla suave
val WisdomBeige = Color(0xFFF0E8E0)           // Superficies secundarias - beige cálido
val WisdomChampagne = Color(0xFFE8DDD4)       // Cards y elementos - champagne elegante
val WisdomGold = Color(0xFFD3C7AB)            // Acentos premium - dorado suave
val WisdomTaupe = Color(0xFFA8A5A0)           // Texto secundario - taupe sofisticado
val WisdomCharcoal = Color(0xFF2D2A26)        // Texto principal - carbón profundo

// **COLORES FUNCIONALES**
val WisdomSuccess = Color(0xFF10B981)         // Verde éxito - esmeralda
val WisdomError = Color(0xFFEF4444)           // Rojo error - coral intenso
val WisdomWarning = Color(0xFFF59E0B)         // Amarillo advertencia - ámbar
val WisdomInfo = Color(0xFF3B82F6)            // Azul información - safiro
val WisdomCoral = Color(0xFFFF6B6B)           // Coral para favoritos

// **DARK MODE CIENTÍFICAMENTE BALANCEADO**
val WisdomDarkSurface = Color(0xFF1A1917)     // Fondo oscuro principal
val WisdomDarkSecondary = Color(0xFF2D2926)   // Superficies secundarias oscuras
val WisdomDarkTertiary = Color(0xFF3F3C37)    // Cards en modo oscuro
val WisdomDarkAccent = Color(0xFFE8DDD4)      // Acentos en dark (mismo que light)
val WisdomDarkText = Color(0xFFF5F5F4)        // Texto principal dark
val WisdomDarkTextSecondary = Color(0xFFA8A5A0) // Texto secundario dark

// **COMPATIBILIDAD CON COLORES ANTERIORES (Para no romper archivos existentes)**
val WisdomWhite = Color.White                 // Blanco puro
val WisdomSage = WisdomGold                   // Mapeo a dorado
val WisdomGreen = WisdomSuccess               // Verde principal
val WisdomLightGreen = WisdomSuccess.copy(alpha = 0.3f) // Verde claro
val WisdomDarkGreen = Color(0xFF059669)       // Verde oscuro
val WisdomDarkGray = WisdomCharcoal           // Gris oscuro

// **FUNCIÓN PARA GRADIENTES (Compatibilidad)**
fun getWisdomGradientColors(): List<Color> {
    return listOf(WisdomPearl, WisdomBeige, WisdomChampagne)
}

// **GRADIENTES PROFESIONALES**
// Estos no son Color objects, pero las definiciones para usar en Brush.linearGradient()

// Para fondos principales
// listOf(WisdomPearl, WisdomBeige, WisdomChampagne.copy(alpha = 0.8f))

// Para cards glassmorphic
// listOf(Color.White.copy(alpha = 0.95f), WisdomPearl.copy(alpha = 0.9f), WisdomChampagne.copy(alpha = 0.85f))

// Para borders luminosos
// listOf(WisdomGold.copy(alpha = 0.3f), Color.White.copy(alpha = 0.5f), WisdomGold.copy(alpha = 0.2f))

// Para shimmer effects
// listOf(WisdomChampagne.copy(alpha = 0.6f), WisdomGold.copy(alpha = 0.3f), WisdomChampagne.copy(alpha = 0.6f))

// **OPACIDADES ESPECÍFICAS PARA GLASSMORPHISM**
const val GlassOpacityHigh = 0.95f           // Para cards principales