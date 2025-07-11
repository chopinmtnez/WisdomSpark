package com.albertowisdom.wisdomspark.utils

import android.content.Context
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
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
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    init {
        // Observar cambios en preferencias y actualizar programación automáticamente
        observeNotificationPreferences()
    }
    
    /**
     * Inicializar el servicio de notificaciones
     * Llamar desde Application.onCreate()
     */
    fun initialize() {
        serviceScope.launch {
            // Combinar todas las preferencias de notificación
            combine(
                userPreferences.areNotificationsEnabled,
                userPreferences.notificationHour,
                userPreferences.notificationMinute
            ) { enabled, hour, minute ->
                Triple(enabled, hour, minute)
            }.collect { (enabled, hour, minute) ->
                if (enabled && notificationHelper.hasNotificationPermission()) {
                    scheduleNotifications(hour, minute)
                } else {
                    cancelNotifications()
                }
            }
        }
    }
    
    /**
     * Programar notificaciones diarias
     */
    fun scheduleNotifications(hour: Int, minute: Int) {
        if (!notificationHelper.hasNotificationPermission()) {
            println("⚠️ No hay permisos de notificación - no se pueden programar")
            return
        }
        
        notificationScheduler.scheduleDailyNotifications(hour, minute)
        println("✅ Notificaciones programadas para ${hour}:${String.format("%02d", minute)}")
    }
    
    /**
     * Cancelar todas las notificaciones
     */
    fun cancelNotifications() {
        notificationScheduler.cancelDailyNotifications()
        notificationHelper.cancelAllNotifications()
        println("🚫 Notificaciones canceladas")
    }
    
    /**
     * Enviar notificación de prueba inmediatamente
     */
    fun sendTestNotification() {
        if (!notificationHelper.hasNotificationPermission()) {
            println("⚠️ No hay permisos de notificación para enviar prueba")
            return
        }
        
        notificationScheduler.sendTestNotification()
        println("🧪 Notificación de prueba enviada")
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
            areScheduled = areScheduled,
            systemEnabled = systemEnabled
        )
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
    val areScheduled: Boolean,
    val systemEnabled: Boolean
) {
    val isFullyEnabled: Boolean
        get() = hasPermission && areScheduled && systemEnabled
}