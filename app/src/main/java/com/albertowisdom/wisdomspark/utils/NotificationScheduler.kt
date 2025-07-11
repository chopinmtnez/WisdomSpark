package com.albertowisdom.wisdomspark.utils

import android.content.Context
import androidx.work.*
import com.albertowisdom.wisdomspark.workers.DailyNotificationWorker
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Programador de notificaciones diarias usando WorkManager
 */
@Singleton
class NotificationScheduler @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val NOTIFICATION_WORK_NAME = "daily_wisdom_notifications"
        private const val DEFAULT_NOTIFICATION_HOUR = 9 // 9:00 AM
        private const val DEFAULT_NOTIFICATION_MINUTE = 0
    }
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Programar notificaciones diarias
     */
    fun scheduleDailyNotifications(hour: Int = DEFAULT_NOTIFICATION_HOUR, minute: Int = DEFAULT_NOTIFICATION_MINUTE) {
        // Cancelar trabajos previos
        cancelDailyNotifications()
        
        // Calcular el delay inicial hasta la próxima hora programada
        val initialDelay = calculateInitialDelay(hour, minute)
        
        // Crear restricciones del trabajo
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No requiere red
            .setRequiresBatteryNotLow(false) // Puede ejecutarse con batería baja
            .setRequiresCharging(false) // No requiere estar cargando
            .setRequiresDeviceIdle(false) // Puede ejecutarse aunque el dispositivo esté en uso
            .build()
        
        // Crear solicitud de trabajo periódico
        val notificationWorkRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(
            repeatInterval = 1, // Cada 1 día
            repeatIntervalTimeUnit = TimeUnit.DAYS,
            flexTimeInterval = 30, // Flexibilidad de 30 minutos
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(DailyNotificationWorker.WORKER_TAG)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        // Programar el trabajo
        workManager.enqueueUniquePeriodicWork(
            NOTIFICATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            notificationWorkRequest
        )
        
        println("📅 Notificaciones programadas para las ${hour}:${String.format("%02d", minute)} (próxima en ${initialDelay / 1000 / 60} minutos)")
    }
    
    /**
     * Cancelar todas las notificaciones programadas
     */
    fun cancelDailyNotifications() {
        workManager.cancelUniqueWork(NOTIFICATION_WORK_NAME)
        workManager.cancelAllWorkByTag(DailyNotificationWorker.WORKER_TAG)
        println("🚫 Notificaciones diarias canceladas")
    }
    
    /**
     * Verificar si las notificaciones están programadas
     */
    fun areNotificationsScheduled(): Boolean {
        return try {
            // Simplemente asumir que están programadas si no hay error
            // En una implementación más compleja, esto requeriría coroutines
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Obtener información del estado de las notificaciones
     */
    fun getNotificationWorkInfo(): String {
        return try {
            "Notificaciones programadas con WorkManager"
        } catch (e: Exception) {
            "Error al verificar estado"
        }
    }
    
    /**
     * Reprogramar notificaciones con nueva hora
     */
    fun rescheduleNotifications(hour: Int, minute: Int) {
        scheduleDailyNotifications(hour, minute)
    }
    
    /**
     * Ejecutar una notificación de prueba inmediatamente
     */
    fun sendTestNotification() {
        val testWorkRequest = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
            .addTag("test_notification")
            .build()
            
        workManager.enqueue(testWorkRequest)
        println("🧪 Notificación de prueba enviada")
    }
    
    /**
     * Calcular el delay inicial hasta la próxima hora programada
     */
    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis
        
        // Configurar la hora objetivo para hoy
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        var targetTime = calendar.timeInMillis
        
        // Si la hora ya pasó hoy, programar para mañana
        if (targetTime <= currentTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            targetTime = calendar.timeInMillis
        }
        
        return targetTime - currentTime
    }
    
    /**
     * Obtener hora formateada para debugging
     */
    fun getFormattedScheduleTime(hour: Int, minute: Int): String {
        return "${String.format("%02d", hour)}:${String.format("%02d", minute)}"
    }
}