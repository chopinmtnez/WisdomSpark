package com.albertowisdom.wisdomspark.presentation.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.albertowisdom.wisdomspark.R
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Componente que maneja la solicitud automática de permisos de notificación
 * Se ejecuta automáticamente después de un delay inicial para no ser intrusivo
 */
@Composable
fun NotificationPermissionHandler(
    userPreferences: UserPreferences,
    notificationHelper: com.albertowisdom.wisdomspark.utils.NotificationHelper
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Estados
    var showPermissionDialog by remember { mutableStateOf(false) }
    var hasAskedPermission by remember { mutableStateOf(false) }
    
    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        scope.launch {
            // Guardar que ya se pidió permiso
            userPreferences.setNotificationPermissionAsked(true)
            
            if (isGranted) {
                // Si se concedió el permiso, activar notificaciones por defecto
                userPreferences.setNotificationsEnabled(true)
            }
        }
    }
    
    // Observar estados de preferencias
    val permissionAsked by userPreferences.notificationPermissionAsked.collectAsState(initial = false)
    val isFirstLaunch by userPreferences.isFirstLaunch.collectAsState(initial = true)
    val notificationsEnabled by userPreferences.areNotificationsEnabled.collectAsState(initial = true)
    
    // Verificar si debe mostrar el diálogo de permisos
    LaunchedEffect(Unit) {
        // Esperar a que se carguen las preferencias
        delay(1000)
        
        // Solo ejecutar una vez cuando se inicializa el componente
        if (isFirstLaunch == false && 
            !permissionAsked && 
            notificationsEnabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationHelper.hasNotificationPermission()) {
            
            // Esperar un poco para que el usuario se familiarice con la app
            delay(2000)
            
            showPermissionDialog = true
        }
    }
    
    // Diálogo de solicitud de permiso
    if (showPermissionDialog) {
        NotificationPermissionDialog(
            onGrantPermission = {
                showPermissionDialog = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onDenyPermission = {
                showPermissionDialog = false
                scope.launch {
                    // Marcar como pedido y desactivar notificaciones
                    userPreferences.setNotificationPermissionAsked(true)
                    userPreferences.setNotificationsEnabled(false)
                }
            }
        )
    }
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
                text = stringResource(R.string.notification_auto_request_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(R.string.notification_auto_request_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onGrantPermission
            ) {
                Text(stringResource(R.string.yes_want_daily_inspiration))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDenyPermission
            ) {
                Text(stringResource(R.string.maybe_later))
            }
        }
    )
}

