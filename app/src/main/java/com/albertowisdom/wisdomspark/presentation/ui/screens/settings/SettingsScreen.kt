package com.albertowisdom.wisdomspark.presentation.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Pantalla de configuración completamente funcional
 */
@Composable
fun SettingsScreen(
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Estados de las preferencias
    val isDarkMode by userPreferences.isDarkModeEnabled.collectAsState(initial = false)
    val isHapticEnabled by userPreferences.isHapticFeedbackEnabled.collectAsState(initial = true)
    val isSwipeableMode by userPreferences.isSwipeableModeEnabled.collectAsState(initial = false)
    val areNotificationsEnabled by userPreferences.areNotificationsEnabled.collectAsState(initial = true)
    
    // Gradiente de fondo
    val backgroundGradient = Brush.linearGradient(
        colors = getWisdomGradientColors(),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "⚙️ Configuración",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = WisdomCharcoal
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Personaliza tu experiencia WisdomSpark",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WisdomTaupe
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Sección: Experiencia de Usuario
            SettingsSection(title = "🎨 Experiencia de Usuario") {
                SettingsToggleItem(
                    icon = Icons.Default.SwapHoriz,
                    title = "Modo Swipeable",
                    description = "Interfaz tipo Tinder para explorar citas",
                    isChecked = isSwipeableMode,
                    onCheckedChange = { newValue ->
                        if (isHapticEnabled) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        scope.launch {
                            userPreferences.setSwipeableMode(newValue)
                        }
                    }
                )
                
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Modo Oscuro",
                    description = "Tema oscuro para mayor comodidad visual",
                    isChecked = isDarkMode,
                    onCheckedChange = { newValue ->
                        if (isHapticEnabled) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        scope.launch {
                            userPreferences.setDarkMode(newValue)
                        }
                    }
                )
                
                SettingsToggleItem(
                    icon = Icons.Default.PhoneAndroid,
                    title = "Vibración",
                    description = "Feedback táctil en las interacciones",
                    isChecked = isHapticEnabled,
                    onCheckedChange = { newValue ->
                        scope.launch {
                            userPreferences.setHapticFeedback(newValue)
                        }
                        // Test vibration si se habilita
                        if (newValue) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sección: Notificaciones
            SettingsSection(title = "🔔 Notificaciones") {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones Diarias",
                    description = "Recibe tu dosis diaria de sabiduría",
                    isChecked = areNotificationsEnabled,
                    onCheckedChange = { newValue ->
                        if (isHapticEnabled) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        scope.launch {
                            userPreferences.setNotificationsEnabled(newValue)
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sección: Premium
            SettingsSection(title = "💎 Premium") {
                SettingsActionItem(
                    icon = Icons.Default.Star,
                    title = "WisdomSpark Premium",
                    description = "Sin anuncios + funciones exclusivas",
                    actionText = "€2.99",
                    onClick = {
                        if (isHapticEnabled) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        // TODO: Lanzar Google Play Billing
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sección: Información
            SettingsSection(title = "ℹ️ Información") {
                SettingsActionItem(
                    icon = Icons.Default.Share,
                    title = "Compartir App",
                    description = "Comparte WisdomSpark con tus amigos",
                    onClick = {
                        if (isHapticEnabled) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        // TODO: Implementar compartir app
                    }
                )
                
                SettingsActionItem(
                    icon = Icons.Default.Email,
                    title = "Contacto",
                    description = "¿Tienes alguna sugerencia?",
                    onClick = {
                        if (isHapticEnabled) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        // TODO: Abrir email
                    }
                )
                
                SettingsActionItem(
                    icon = Icons.Default.Info,
                    title = "Acerca de",
                    description = "WisdomSpark v1.0 - Sabiduría diaria",
                    onClick = {
                        if (isHapticEnabled) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        // TODO: Mostrar diálogo de información
                    }
                )
            }
            
            // Espaciado final
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = WisdomCharcoal
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = { onCheckedChange(!isChecked) },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = WisdomGold,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = WisdomCharcoal
                        )
                    )
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WisdomTaupe
                        )
                    )
                }
            }
            
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = WisdomGold,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = WisdomTaupe.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionText: String? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = WisdomGold,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = WisdomCharcoal
                        )
                    )
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WisdomTaupe
                        )
                    )
                }
            }
            
            if (actionText != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WisdomGold.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = WisdomGold
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = WisdomTaupe,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
