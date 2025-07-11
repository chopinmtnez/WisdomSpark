package com.albertowisdom.wisdomspark.presentation.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.albertowisdom.wisdomspark.utils.HapticManager
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val hapticManager = remember { HapticManager(context) }
    
    // Estados de las preferencias
    val isDarkMode by userPreferences.isDarkModeEnabled.collectAsState(initial = false)
    val isHapticEnabled by userPreferences.isHapticFeedbackEnabled.collectAsState(initial = true)
    val isSwipeableMode by userPreferences.isSwipeableModeEnabled.collectAsState(initial = false)
    val areNotificationsEnabled by userPreferences.areNotificationsEnabled.collectAsState(initial = true)
    val notificationHour by userPreferences.notificationHour.collectAsState(initial = 9)
    val notificationMinute by userPreferences.notificationMinute.collectAsState(initial = 0)
    
    // Estado para el diálogo de información
    var showInfoDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    
    // Gradiente de fondo que respeta el tema
    val backgroundGradient = Brush.linearGradient(
        colors = getThemedGradientColors(),
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
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Personaliza tu experiencia WisdomSpark",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            hapticManager.mediumImpact()
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
                            hapticManager.lightImpact()
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
                            // Usar el HapticManager para un feedback más rico
                            hapticManager.success()
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
                            hapticManager.lightImpact()
                        }
                        scope.launch {
                            userPreferences.setNotificationsEnabled(newValue)
                        }
                    }
                )
                
                // Selector de hora solo si las notificaciones están habilitadas
                if (areNotificationsEnabled) {
                    SettingsActionItem(
                        icon = Icons.Default.Schedule,
                        title = "Hora de Notificación",
                        description = "${String.format("%02d", notificationHour)}:${String.format("%02d", notificationMinute)}",
                        onClick = {
                            if (isHapticEnabled) {
                                hapticManager.selection()
                            }
                            showTimePickerDialog = true
                        }
                    )
                }
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
                            hapticManager.heavyImpact()
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
                            hapticManager.selection()
                        }
                        shareApp(context)
                    }
                )
                
                SettingsActionItem(
                    icon = Icons.Default.Email,
                    title = "Contacto",
                    description = "¿Tienes alguna sugerencia?",
                    onClick = {
                        if (isHapticEnabled) {
                            hapticManager.selection()
                        }
                        openEmailApp(context)
                    }
                )
                
                SettingsActionItem(
                    icon = Icons.Default.Info,
                    title = "Acerca de",
                    description = "WisdomSpark v1.0 - Sabiduría diaria",
                    onClick = {
                        if (isHapticEnabled) {
                            hapticManager.selection()
                        }
                        showInfoDialog = true
                    }
                )
            }
            
            // Espaciado final
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Diálogo de información
    if (showInfoDialog) {
        InfoDialog(
            onDismiss = { showInfoDialog = false }
        )
    }
    
    // Diálogo de selección de hora
    if (showTimePickerDialog) {
        TimePickerDialog(
            currentHour = notificationHour,
            currentMinute = notificationMinute,
            onTimeSelected = { hour, minute ->
                scope.launch {
                    userPreferences.setNotificationTime(hour, minute)
                }
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
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
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            
            if (actionText != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "✨",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "WisdomSpark",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(label = "Versión", value = "1.0.0")
                InfoRow(label = "Desarrollador", value = "Alberto Wisdom")
                InfoRow(label = "Descripción", value = "Tu dosis diaria de sabiduría e inspiración")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "WisdomSpark te ofrece citas inspiracionales cuidadosamente seleccionadas para motivarte cada día. Explora diferentes categorías, guarda tus favoritas y comparte la sabiduría con otros.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "💝 Hecho con amor para inspirar y motivar",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Cerrar")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * Función para compartir la app
 */
private fun shareApp(context: Context) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "¡Descubre WisdomSpark!")
            putExtra(
                Intent.EXTRA_TEXT,
                """
                🌟 ¡Te recomiendo WisdomSpark! ✨
                
                Una increíble app que te ofrece citas inspiracionales y sabiduría diaria para motivarte cada día.
                
                📱 Características:
                • Citas inspiracionales diarias
                • Diferentes categorías de sabiduría
                • Modo swipeable estilo Tinder
                • Guarda tus citas favoritas
                • Comparte la inspiración
                
                💝 Descárgala y comienza tu viaje de inspiración diaria.
                
                #WisdomSpark #Inspiración #MotivacionDiaria
                """.trimIndent()
            )
        }
        
        val chooser = Intent.createChooser(shareIntent, "Compartir WisdomSpark")
        context.startActivity(chooser)
    } catch (e: Exception) {
        // Manejar error silenciosamente
        println("Error al compartir la app: ${e.message}")
    }
}

/**
 * Función para abrir la app de email
 */
private fun openEmailApp(context: Context) {
    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("albertomartinezmartin.palencia@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback sobre WisdomSpark")
            putExtra(
                Intent.EXTRA_TEXT,
                """
                Hola Alberto,
                
                Te escribo desde WisdomSpark para compartir mi feedback:
                
                [Escribe aquí tus comentarios, sugerencias o reportes de bugs]
                
                Información del dispositivo:
                - App: WisdomSpark v1.0.0
                - Android: ${android.os.Build.VERSION.RELEASE}
                - Dispositivo: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
                
                ¡Gracias por crear esta increíble app!
                
                Saludos cordiales
                """.trimIndent()
            )
        }
        
        context.startActivity(Intent.createChooser(emailIntent, "Enviar feedback"))
    } catch (e: Exception) {
        // Si no hay app de email, intentar con ACTION_SEND
        try {
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("albertomartinezmartin.palencia@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Feedback sobre WisdomSpark")
                putExtra(
                    Intent.EXTRA_TEXT,
                    """
                    Hola Alberto,
                    
                    Te escribo desde WisdomSpark para compartir mi feedback:
                    
                    [Escribe aquí tus comentarios, sugerencias o reportes de bugs]
                    
                    Información del dispositivo:
                    - App: WisdomSpark v1.0.0
                    - Android: ${android.os.Build.VERSION.RELEASE}
                    - Dispositivo: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
                    
                    ¡Gracias por crear esta increíble app!
                    
                    Saludos cordiales
                    """.trimIndent()
                )
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Enviar feedback"))
        } catch (e2: Exception) {
            println("Error al abrir email: ${e2.message}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    currentHour: Int,
    currentMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = currentHour,
        initialMinute = currentMinute,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Hora de Notificación",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Selecciona cuándo quieres recibir tu cita diaria",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surface,
                        selectorColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        periodSelectorBorderColor = MaterialTheme.colorScheme.outline,
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        periodSelectorUnselectedContainerColor = Color.Transparent,
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancelar")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
