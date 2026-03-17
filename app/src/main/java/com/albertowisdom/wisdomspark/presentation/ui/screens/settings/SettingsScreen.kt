package com.albertowisdom.wisdomspark.presentation.ui.screens.settings

import android.Manifest
import android.util.Log
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
import android.app.Activity
import com.albertowisdom.wisdomspark.utils.LocaleHelper
import com.albertowisdom.wisdomspark.utils.NotificationHelper
import com.albertowisdom.wisdomspark.presentation.ui.components.SecurityStatusCard
import com.albertowisdom.wisdomspark.presentation.ui.components.SecurityDebugCard
import com.albertowisdom.wisdomspark.presentation.ui.components.PremiumDebugCard
import com.albertowisdom.wisdomspark.premium.ui.PremiumBadge
import com.albertowisdom.wisdomspark.premium.ui.PremiumViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.albertowisdom.wisdomspark.BuildConfig
import com.albertowisdom.wisdomspark.R
import com.albertowisdom.wisdomspark.data.managers.LanguageManager
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.preferences.SupportedLanguage
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Pantalla de configuración completamente funcional
 */
@Composable
fun SettingsScreen(
    userPreferences: UserPreferences,
    languageManager: LanguageManager,
    notificationHelper: NotificationHelper,
    onNavigateToPremium: () -> Unit = {},
    modifier: Modifier = Modifier,
    premiumViewModel: PremiumViewModel = hiltViewModel()
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
    
    // Estado del idioma usando StateFlow para cambio inmediato
    val currentLanguage by userPreferences.appLanguage.collectAsState(initial = LocaleHelper.getSystemLanguage(context))
    
    // Estado para el diálogo de información
    var showInfoDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    // Estados para manejo de permisos de notificación
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var pendingNotificationToggle by remember { mutableStateOf(false) }
    
    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si se concedió el permiso, activar notificaciones
            scope.launch {
                userPreferences.setNotificationsEnabled(pendingNotificationToggle)
                userPreferences.setNotificationPermissionAsked(true)
            }
        } else {
            // Si se denegó, mantener notificaciones desactivadas
            scope.launch {
                userPreferences.setNotificationsEnabled(false)
                userPreferences.setNotificationPermissionAsked(true)
            }
        }
        pendingNotificationToggle = false
    }
    
    // Lista de idiomas soportados
    val supportedLanguages = remember { userPreferences.getSupportedLanguages() }
    
    // Función para manejar el toggle de notificaciones con permisos
    val handleNotificationToggle = { newValue: Boolean ->
        if (newValue) {
            // Usuario quiere activar notificaciones
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationHelper.hasNotificationPermission()) {
                // Necesita permisos - mostrar diálogo explicativo
                pendingNotificationToggle = newValue
                showNotificationPermissionDialog = true
            } else {
                // Tiene permisos o no los necesita - activar directamente
                scope.launch {
                    userPreferences.setNotificationsEnabled(newValue)
                }
            }
        } else {
            // Usuario quiere desactivar notificaciones - permitir siempre
            scope.launch {
                userPreferences.setNotificationsEnabled(newValue)
            }
        }
    }
    
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
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.settings_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Sección: Experiencia de Usuario
            SettingsSection(title = stringResource(R.string.user_experience)) {
                SettingsToggleItem(
                    icon = Icons.Default.SwapHoriz,
                    title = stringResource(R.string.swipeable_mode),
                    description = stringResource(R.string.swipeable_mode_desc),
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
                    title = stringResource(R.string.dark_mode),
                    description = stringResource(R.string.dark_mode_desc),
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
                    title = stringResource(R.string.vibration),
                    description = stringResource(R.string.vibration_desc),
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
                
                // Selector de idioma
                SettingsActionItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.language),
                    description = supportedLanguages.find { it.code == currentLanguage }?.let { 
                        "${it.flag} ${it.name}" 
                    } ?: "🇪🇸 Español",
                    onClick = {
                        if (isHapticEnabled) {
                            hapticManager.selection()
                        }
                        showLanguageDialog = true
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sección: Notificaciones
            SettingsSection(title = stringResource(R.string.notifications_section)) {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.daily_notifications),
                    description = stringResource(R.string.daily_notifications_desc),
                    isChecked = areNotificationsEnabled,
                    onCheckedChange = { newValue ->
                        if (isHapticEnabled) {
                            hapticManager.lightImpact()
                        }
                        handleNotificationToggle(newValue)
                    }
                )
                
                // Selector de hora solo si las notificaciones están habilitadas
                if (areNotificationsEnabled) {
                    SettingsActionItem(
                        icon = Icons.Default.Schedule,
                        title = stringResource(R.string.notification_time),
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
            SettingsSection(title = stringResource(R.string.premium_section)) {
                PremiumSettingsItem(
                    premiumViewModel = premiumViewModel,
                    isHapticEnabled = isHapticEnabled,
                    hapticManager = hapticManager,
                    onNavigateToPremium = onNavigateToPremium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sección: Información
            SettingsSection(title = stringResource(R.string.info_section)) {
                SettingsActionItem(
                    icon = Icons.Default.Share,
                    title = stringResource(R.string.share_app),
                    description = stringResource(R.string.share_app_desc),
                    onClick = {
                        if (isHapticEnabled) {
                            hapticManager.selection()
                        }
                        shareApp(context)
                    }
                )
                
                SettingsActionItem(
                    icon = Icons.Default.Email,
                    title = stringResource(R.string.contact),
                    description = stringResource(R.string.contact_desc),
                    onClick = {
                        if (isHapticEnabled) {
                            hapticManager.selection()
                        }
                        openEmailApp(context)
                    }
                )
                
                SettingsActionItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.about),
                    description = stringResource(R.string.about_desc),
                    onClick = {
                        if (isHapticEnabled) {
                            hapticManager.selection()
                        }
                        showInfoDialog = true
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sección: Seguridad
            SettingsSection(title = stringResource(R.string.settings_security_title)) {
                SecurityStatusCard(
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Debug cards - only visible in debug builds
                if (BuildConfig.DEBUG) {
                    SecurityDebugCard(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    PremiumDebugCard(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
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
    
    // Diálogo de selección de idioma
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            supportedLanguages = supportedLanguages,
            currentLanguage = currentLanguage,
            onLanguageSelected = { selectedLanguage ->
                showLanguageDialog = false
                
                // OPTIMIZED: UI-FIRST pattern for immediate response (WhatsApp/Telegram style)
                scope.launch {
                    Log.d("SettingsScreen", "Immediate language change to '$selectedLanguage'")
                    
                    try {
                        // 1. IMMEDIATE UI CHANGE (0ms response time)
                        Log.d("SettingsScreen", "Step 1: Immediate UI change")
                        LocaleHelper.changeLanguageImmediate(selectedLanguage)
                        LocaleHelper.saveLanguage(context, selectedLanguage)
                        
                        // 2. UPDATE PREFERENCES IMMEDIATELY (sync)
                        Log.d("SettingsScreen", "Step 2: Update preferences")
                        userPreferences.setAppLanguage(selectedLanguage)
                        
                        // 3. MODERN LOCALE CHANGE (Android 13+)
                        try {
                            LocaleHelper.changeLanguageModern(selectedLanguage)
                            Log.d("SettingsScreen", "Modern locale applied")
                        } catch (e: Exception) {
                            Log.d("SettingsScreen", "Modern locale failed (not critical): ${e.message}")
                        }
                        
                        // 4. RECREATE ACTIVITY IMMEDIATELY for instant UX
                        Log.d("SettingsScreen", "Step 3: Recreating activity for immediate UI update")
                        (context as? Activity)?.recreate()
                        
                        // 5. BACKGROUND DATABASE SYNC (non-blocking)
                        Log.d("SettingsScreen", "Step 4: Starting background database sync...")
                        scope.launch(Dispatchers.IO) {
                            try {
                                // Background sync - doesn't block UI
                                languageManager.changeLanguage(selectedLanguage)
                                Log.d("SettingsScreen", "Background database sync completed for '$selectedLanguage'")
                            } catch (e: Exception) {
                                Log.d("SettingsScreen", "Background sync failed (app still works): ${e.message}")
                                // Could show a subtle notification/toast if needed
                            }
                        }
                        
                        Log.d("SettingsScreen", "Immediate language change completed! Background sync in progress...")
                        
                    } catch (e: Exception) {
                        Log.e("SettingsScreen", "Error in immediate language change: ${e.message}")
                        
                        // Fallback: still try to change UI immediately
                        try {
                            LocaleHelper.saveLanguage(context, selectedLanguage)
                            LocaleHelper.changeLanguageModern(selectedLanguage)
                            (context as? Activity)?.recreate()
                            Log.d("SettingsScreen", "Fallback immediate change applied")
                        } catch (fallbackError: Exception) {
                            Log.e("SettingsScreen", "Complete fallback failure: ${fallbackError.message}")
                        }
                    }
                }
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
    
    // Diálogo de permisos de notificación
    if (showNotificationPermissionDialog) {
        NotificationPermissionDialog(
            onGrantPermission = {
                showNotificationPermissionDialog = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onDenyPermission = {
                showNotificationPermissionDialog = false
                scope.launch {
                    userPreferences.setNotificationsEnabled(false)
                    userPreferences.setNotificationPermissionAsked(true)
                }
                pendingNotificationToggle = false
            }
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
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
                InfoRow(label = stringResource(R.string.about_version_label), value = BuildConfig.VERSION_NAME)
                InfoRow(label = stringResource(R.string.about_developer_label), value = stringResource(R.string.info_developer_name))
                InfoRow(label = stringResource(R.string.about_description_label), value = stringResource(R.string.about_description_text))

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.about_long_description),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.about_made_with_love),
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
                Text(stringResource(R.string.close))
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
        val appName = context.getString(R.string.app_name)
        val shareSubject = context.getString(R.string.share_app_subject, appName)
        val shareText = context.getString(R.string.share_app_text, appName)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, shareSubject)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_via)))
    } catch (e: Exception) {
        Log.d("SettingsScreen", "Error sharing app: ${e.message}")
    }
}

/**
 * Función para abrir la app de email
 */
private fun openEmailApp(context: Context) {
    val feedbackSubject = context.getString(R.string.feedback_email_subject)
    val feedbackBody = context.getString(
        R.string.feedback_email_body,
        BuildConfig.VERSION_NAME,
        android.os.Build.VERSION.RELEASE,
        "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
    )
    val sendFeedback = context.getString(R.string.send_feedback)

    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("albertomartinezmartin.palencia@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, feedbackSubject)
            putExtra(Intent.EXTRA_TEXT, feedbackBody)
        }
        context.startActivity(Intent.createChooser(emailIntent, sendFeedback))
    } catch (e: Exception) {
        // Fallback: ACTION_SEND if no email app handles mailto
        try {
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("albertomartinezmartin.palencia@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, feedbackSubject)
                putExtra(Intent.EXTRA_TEXT, feedbackBody)
            }
            context.startActivity(Intent.createChooser(fallbackIntent, sendFeedback))
        } catch (e2: Exception) {
            Log.d("SettingsScreen", "Error opening email: ${e2.message}")
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
                text = stringResource(R.string.settings_notification_time_title),
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
                    text = stringResource(R.string.settings_notification_time_subtitle),
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
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun LanguageSelectionDialog(
    supportedLanguages: List<SupportedLanguage>,
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_language),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.select_language_desc),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                supportedLanguages.forEach { language ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { 
                                Log.d("SettingsScreen", "Language clicked: ${language.name} (${language.code})")
                                onLanguageSelected(language.code) 
                            },
                        color = if (language.code == currentLanguage) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = language.flag,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            
                            Text(
                                text = language.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (language.code == currentLanguage) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    color = if (language.code == currentLanguage) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (language.code == currentLanguage) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(R.string.selected),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(R.string.close))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}


@Composable
private fun NotificationPermissionDialog(
    onGrantPermission: () -> Unit,
    onDenyPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDenyPermission,
        title = { 
            Text(
                text = stringResource(R.string.daily_notifications),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(R.string.daily_notifications_desc) + "\n\n" + 
                      stringResource(R.string.notification_permission_request),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onGrantPermission
            ) {
                Text(stringResource(R.string.yes_i_want_inspiration))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDenyPermission
            ) {
                Text(stringResource(R.string.no_thanks))
            }
        }
    )
}

/**
 * Componente de configuración Premium
 */
@Composable
private fun PremiumSettingsItem(
    premiumViewModel: PremiumViewModel,
    isHapticEnabled: Boolean,
    hapticManager: HapticManager,
    onNavigateToPremium: () -> Unit
) {
    val premiumUiState by premiumViewModel.uiState.collectAsState()
    
    if (premiumUiState.isPremium) {
        // Usuario Premium - mostrar estado activo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "👑",
                    fontSize = 24.sp
                )
                
                Column {
                    Text(
                        text = stringResource(R.string.premium_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = stringResource(R.string.premium_all_features),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            PremiumBadge()
        }
        
        // Botón para gestionar suscripción
        SettingsActionItem(
            icon = Icons.Default.Settings,
            title = stringResource(R.string.premium_manage_title),
            description = stringResource(R.string.premium_manage_description),
            onClick = {
                if (isHapticEnabled) {
                    hapticManager.lightImpact()
                }
                onNavigateToPremium()
            }
        )
    } else {
        // Usuario no Premium - botón de upgrade
        SettingsActionItem(
            icon = Icons.Default.Star,
            title = stringResource(R.string.premium_title),
            description = stringResource(R.string.premium_desc),
            actionText = "👑 Upgrade",
            onClick = {
                if (isHapticEnabled) {
                    hapticManager.heavyImpact()
                }
                onNavigateToPremium()
            }
        )
    }
}
