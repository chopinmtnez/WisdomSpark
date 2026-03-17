package com.albertowisdom.wisdomspark.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio principal para gestionar notificaciones diarias
 * Coordina UserPreferences, NotificationScheduler y permisos
 */
@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences,
    private val notificationScheduler: NotificationScheduler,
    private val notificationHelper: NotificationHelper
) {

    companion object {
        private const val TAG = "NotificationService"
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    /**
     * Inicializar el servicio de notificaciones
     * Llamar desde Application.onCreate()
     */
    fun initialize() {
        serviceScope.launch {
            // 1. Programar notificaciones con configuración actual
            scheduleInitialNotifications()
            
            // 2. Observar cambios futuros en preferencias
            observeNotificationPreferences()
        }
    }
    
    /**
     * Programar notificaciones iniciales basadas en configuración actual
     */
    private suspend fun scheduleInitialNotifications() {
        try {
            val enabled = userPreferences.areNotificationsEnabled.first()
            val hour = userPreferences.notificationHour.first()
            val minute = userPreferences.notificationMinute.first()
            
            Log.d(TAG, "Configuracion inicial: enabled=$enabled, time=${hour}:${String.format("%02d", minute)}")
            
            if (enabled && notificationHelper.hasNotificationPermission()) {
                scheduleNotifications(hour, minute)
            } else {
                if (!enabled) {
                    Log.d(TAG, "Notificaciones deshabilitadas en preferencias")
                }
                if (!notificationHelper.hasNotificationPermission()) {
                    Log.d(TAG, "Sin permisos de notificacion")
                }
                cancelNotifications()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en programacion inicial: ${e.message}")
        }
    }
    
    /**
     * Programar notificaciones diarias
     */
    fun scheduleNotifications(hour: Int, minute: Int) {
        if (!notificationHelper.hasNotificationPermission()) {
            Log.d(TAG, "No hay permisos de notificacion - no se pueden programar")
            return
        }
        
        if (!notificationHelper.areNotificationsEnabledInSystem()) {
            Log.d(TAG, "Notificaciones deshabilitadas en configuracion del sistema")
            return
        }
        
        try {
            notificationScheduler.scheduleDailyNotifications(hour, minute)
            Log.d(TAG, "Notificaciones programadas exitosamente para ${hour}:${String.format("%02d", minute)}")
        } catch (e: Exception) {
            Log.e(TAG, "Error programando notificaciones: ${e.message}")
        }
    }
    
    /**
     * Cancelar todas las notificaciones
     */
    fun cancelNotifications() {
        notificationScheduler.cancelDailyNotifications()
        notificationHelper.cancelAllNotifications()
        Log.d(TAG, "Notificaciones canceladas")
    }
    
    
    /**
     * Verificar estado actual de las notificaciones
     */
    fun getNotificationStatus(): NotificationStatus {
        val hasPermission = notificationHelper.hasNotificationPermission()
        val areScheduled = notificationScheduler.areNotificationsScheduled()
        val systemEnabled = notificationHelper.areNotificationsEnabledInSystem()
        
        return NotificationStatus(
            hasPermission = hasPermission,
            systemEnabled = systemEnabled,
            requiresPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
            canSendNotifications = hasPermission && systemEnabled
        )
    }
    
    /**
     * Diagnóstico completo del sistema de notificaciones
     * Útil para debugging
     */
    suspend fun performDiagnostic(): String {
        val status = StringBuilder()
        status.appendLine("🔍 DIAGNÓSTICO DEL SISTEMA DE NOTIFICACIONES")
        status.appendLine("====================================================")
        
        try {
            // Incluir diagnóstico detallado de permisos
            status.appendLine(notificationHelper.getPermissionDiagnostic())
            status.appendLine("")
            
            // Verificar preferencias del usuario
            val userEnabled = userPreferences.areNotificationsEnabled.first()
            val hour = userPreferences.notificationHour.first()
            val minute = userPreferences.notificationMinute.first()
            status.appendLine("👤 CONFIGURACIÓN DEL USUARIO")
            status.appendLine("====================================================")
            status.appendLine("👤 Notificaciones habilitadas: ${if (userEnabled) "✅ ENABLED" else "❌ DISABLED"}")
            status.appendLine("⏰ Hora configurada: ${hour}:${String.format("%02d", minute)}")
            status.appendLine("")
            
            // Verificar WorkManager
            status.appendLine("🔧 WORKMANAGER")
            status.appendLine("====================================================")
            val workInfo = notificationScheduler.getDetailedWorkInfo()
            status.appendLine(workInfo)
            status.appendLine("")
            
            // Estado de trabajos actuales
            val workManagerStatus = notificationScheduler.checkWorkManagerStatus()
            status.appendLine(workManagerStatus)
            
            // Calcular próxima notificación
            if (userEnabled) {
                val nextDelay = calculateNextNotificationTime(hour, minute)
                val hoursUntilNext = nextDelay / 1000 / 60 / 60
                val minutesUntilNext = (nextDelay / 1000 / 60) % 60
                status.appendLine("⏳ Próxima notificación en: ${hoursUntilNext}h ${minutesUntilNext}m")
            }
            status.appendLine("")
            
            // Estado general
            val hasPermission = notificationHelper.hasNotificationPermission()
            val systemEnabled = notificationHelper.areNotificationsEnabledInSystem()
            val overallStatus = hasPermission && systemEnabled && userEnabled
            status.appendLine("📊 ESTADO GENERAL")
            status.appendLine("====================================================")
            status.appendLine("Resultado: ${if (overallStatus) "✅ TODO OK - Notificaciones funcionando" else "❌ PROBLEMAS DETECTADOS"}")
            
            if (!overallStatus) {
                status.appendLine("🔧 SOLUCIONES RECOMENDADAS:")
                if (!hasPermission) {
                    status.appendLine("   • Conceder permiso POST_NOTIFICATIONS en el botón 'Permiso'")
                }
                if (!systemEnabled) {
                    status.appendLine("   • Habilitar notificaciones en Configuración del sistema")
                }
                if (!userEnabled) {
                    status.appendLine("   • Activar notificaciones en la configuración de la app")
                }
            }
            
        } catch (e: Exception) {
            status.appendLine("❌ Error durante diagnóstico: ${e.message}")
        }
        
        return status.toString()
    }
    
    /**
     * Calcular tiempo hasta próxima notificación
     */
    private fun calculateNextNotificationTime(hour: Int, minute: Int): Long {
        val calendar = java.util.Calendar.getInstance()
        val currentTime = calendar.timeInMillis
        
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
        calendar.set(java.util.Calendar.MINUTE, minute)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        var targetTime = calendar.timeInMillis
        if (targetTime <= currentTime) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
            targetTime = calendar.timeInMillis
        }
        
        return targetTime - currentTime
    }
    
    /**
     * Reprogramar notificaciones con nueva configuración
     */
    suspend fun updateNotificationSettings(enabled: Boolean, hour: Int, minute: Int) {
        // Actualizar preferencias
        userPreferences.setNotificationsEnabled(enabled)
        userPreferences.setNotificationTime(hour, minute)
        
        // La programación se actualizará automáticamente via observeNotificationPreferences
    }
    
    /**
     * Observar cambios en preferencias de notificación
     */
    private fun observeNotificationPreferences() {
        serviceScope.launch {
            combine(
                userPreferences.areNotificationsEnabled,
                userPreferences.notificationHour,
                userPreferences.notificationMinute
            ) { enabled, hour, minute ->
                NotificationSettings(enabled, hour, minute)
            }.collect { settings ->
                handleNotificationSettingsChange(settings)
            }
        }
    }
    
    /**
     * Manejar cambios en configuración de notificaciones
     */
    private fun handleNotificationSettingsChange(settings: NotificationSettings) {
        if (settings.enabled && notificationHelper.hasNotificationPermission()) {
            scheduleNotifications(settings.hour, settings.minute)
        } else {
            cancelNotifications()
        }
    }
    
    /**
     * Configuración de notificaciones
     */
    private data class NotificationSettings(
        val enabled: Boolean,
        val hour: Int,
        val minute: Int
    )
}

/**
 * Estado actual de las notificaciones
 */
data class NotificationStatus(
    val hasPermission: Boolean,
    val systemEnabled: Boolean,
    val requiresPermission: Boolean,
    val canSendNotifications: Boolean
)