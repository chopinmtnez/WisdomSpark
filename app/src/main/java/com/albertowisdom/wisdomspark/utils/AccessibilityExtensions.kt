@file:OptIn(ExperimentalFoundationApi::class)

package com.albertowisdom.wisdomspark.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/**
 * Extensiones de accesibilidad para WisdomSpark
 * Proporciona funciones helper para mejorar la accesibilidad de la app
 */

/**
 * Agrega semántica básica para elementos clickeables
 */
fun Modifier.wisdomClickableSemantics(
    label: String,
    onClick: (() -> Unit)? = null
): Modifier = this.semantics {
    contentDescription = label
    role = Role.Button
    if (onClick != null) {
        this.onClick {
            onClick()
            true
        }
    }
}

/**
 * Agrega semántica para elementos de texto
 */
fun Modifier.wisdomTextSemantics(
    text: String,
    isHeading: Boolean = false
): Modifier = this.semantics {
    contentDescription = text
    if (isHeading) {
        heading()
    }
}

/**
 * Agrega semántica para elementos de entrada/input
 */
fun Modifier.wisdomInputSemantics(
    label: String,
    value: String = "",
    isError: Boolean = false
): Modifier = this.semantics {
    contentDescription = "$label: $value"
    if (isError) {
        error("Error en $label")
    }
}

/**
 * Agrega semántica para elementos de estado (como favoritos)
 */
fun Modifier.wisdomToggleSemantics(
    label: String,
    isChecked: Boolean,
    onToggle: () -> Unit
): Modifier = this.semantics {
    contentDescription = if (isChecked) "$label activado" else "$label desactivado"
    role = Role.Switch
    stateDescription = if (isChecked) "activado" else "desactivado"
    onClick {
        onToggle()
        true
    }
}

/**
 * Agrega semántica para contenedores de lista
 */
fun Modifier.wisdomListSemantics(
    itemCount: Int,
    label: String = "Lista"
): Modifier = this.semantics {
    contentDescription = "$label con $itemCount elementos"
}

/**
 * Agrega semántica para navegación entre pestañas
 */
fun Modifier.wisdomNavigationSemantics(
    label: String,
    isSelected: Boolean = false
): Modifier = this.semantics {
    contentDescription = if (isSelected) "$label seleccionado" else label
    role = Role.Tab
    stateDescription = if (isSelected) "seleccionado" else "no seleccionado"
}

/**
 * Agrega semántica para botones de acción principales
 */
fun Modifier.wisdomPrimaryActionSemantics(
    action: String
): Modifier = this.semantics {
    contentDescription = action
    role = Role.Button
}

/**
 * Agrega semántica para elementos de carga/loading
 */
fun Modifier.wisdomLoadingSemantics(
    message: String = "Cargando"
): Modifier = this.semantics {
    contentDescription = message
    role = Role.Image
}

/**
 * Agrega semántica para elementos de error
 */
fun Modifier.wisdomErrorSemantics(
    errorMessage: String
): Modifier = this.semantics {
    contentDescription = "Error: $errorMessage"
    role = Role.Image
}

/**
 * Agrega semántica para grupos de elementos relacionados
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.wisdomGroupSemantics(
    groupName: String
): Modifier = this.semantics {
    contentDescription = "Grupo: $groupName"
}.selectableGroup()

/**
 * Agrega semántica para elementos que pueden expandirse/colapsarse
 */
fun Modifier.wisdomExpandableSemantics(
    label: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
): Modifier = this.semantics {
    contentDescription = if (isExpanded) "$label expandido" else "$label colapsado"
    role = Role.Button
    stateDescription = if (isExpanded) "expandido" else "colapsado"
    onClick {
        onToggle()
        true
    }
}

/**
 * Agrega acciones personalizadas de accesibilidad
 */
fun Modifier.wisdomCustomActionSemantics(
    actions: List<Pair<String, () -> Boolean>>
): Modifier = this.semantics {
    customActions = actions.map { (label, action) ->
        CustomAccessibilityAction(label = label, action = action)
    }
}

/**
 * Agrega semántica para regiones dinámicas (live regions)
 */
fun Modifier.wisdomLiveRegionSemantics(
    polite: Boolean = true
): Modifier {
    val mode = if (polite) LiveRegionMode.Polite else LiveRegionMode.Assertive
    return this.semantics {
        liveRegion = mode
    }
}

/**
 * Agrega descripción contextual para citas
 */
fun Modifier.wisdomQuoteSemantics(
    text: String,
    author: String,
    category: String,
    isFavorite: Boolean
): Modifier = this.semantics {
    contentDescription = buildString {
        append("Cita de $category. ")
        append("\"$text\" ")
        append("Por $author. ")
        if (isFavorite) {
            append("Marcada como favorita.")
        } else {
            append("No está en favoritos.")
        }
    }
}

/**
 * Colores con contraste mejorado para accesibilidad
 */
object WisdomAccessibilityColors {
    val HighContrastText = Color(0xFF000000)
    val HighContrastBackground = Color(0xFFFFFFFF)
    val HighContrastAccent = Color(0xFF0066CC)
    val HighContrastError = Color(0xFFCC0000)
    val HighContrastSuccess = Color(0xFF008800)
}

/**
 * Estilos de texto con contraste mejorado
 */
object WisdomAccessibilityTextStyles {
    val HighContrastHeadline = TextStyle(
        color = WisdomAccessibilityColors.HighContrastText,
        fontSize = 24.sp
    )
    
    val HighContrastBody = TextStyle(
        color = WisdomAccessibilityColors.HighContrastText,
        fontSize = 16.sp
    )
    
    val HighContrastCaption = TextStyle(
        color = WisdomAccessibilityColors.HighContrastText,
        fontSize = 12.sp
    )
}

/**
 * Función helper para crear texto accesible
 */
@Composable
fun accessibleText(
    text: String,
    isImportant: Boolean = false
): AnnotatedString {
    return if (isImportant) {
        AnnotatedString(text)
    } else {
        AnnotatedString(text)
    }
}

/**
 * Modificador para elementos que deben ser ignorados por lectores de pantalla
 */
fun Modifier.wisdomIgnoreAccessibility(): Modifier = this.semantics {
    // Alternativa a invisibleToUser() que puede ser experimental
    contentDescription = ""
    role = Role.Image
}

/**
 * Modificador para elementos decorativos sin significado semántico
 */
fun Modifier.wisdomDecorative(): Modifier = this.semantics {
    role = Role.Image
    contentDescription = ""
}

/**
 * Función para verificar si el modo de alto contraste está activo
 */
@Composable
fun isHighContrastMode(): Boolean {
    // En una implementación real, esto verificaría las configuraciones del sistema
    return false
}

/**
 * Función para obtener el factor de escala de texto del sistema
 */
@Composable
fun getSystemTextScale(): Float {
    // En una implementación real, esto obtendría la escala del sistema
    return 1.0f
}

/**
 * Extensión para hacer elementos más visibles si es necesario
 */
fun Modifier.wisdomVisibilityEnhanced(
    enhanceVisibility: Boolean = false
): Modifier = if (enhanceVisibility) {
    this.alpha(1f)
} else {
    this
}