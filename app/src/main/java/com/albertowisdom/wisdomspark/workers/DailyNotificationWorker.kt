package com.albertowisdom.wisdomspark.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import com.albertowisdom.wisdomspark.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.*

/**
 * Worker para enviar notificaciones diarias de citas
 */
@HiltWorker
class DailyNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userPreferences: UserPreferences,
    private val quoteRepository: QuoteRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "DailyNotificationWorker"
        const val WORKER_NAME = "daily_notification_worker"
        const val WORKER_TAG = "wisdom_daily_notifications"
    }

    override suspend fun doWork(): Result {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "DailyNotificationWorker iniciado - ${java.util.Date()}")
        
        return try {
            // Verificar si las notificaciones están habilitadas
            val notificationsEnabled = userPreferences.areNotificationsEnabled.first()
            Log.d(TAG, "Notificaciones habilitadas en preferencias: $notificationsEnabled")
            
            if (!notificationsEnabled) {
                Log.d(TAG, "Notificaciones deshabilitadas por el usuario - Worker terminado")
                return Result.success()
            }
            
            // Verificar permisos del sistema
            val hasPermission = notificationHelper.hasNotificationPermission()
            val systemEnabled = notificationHelper.areNotificationsEnabledInSystem()
            Log.d(TAG, "Permiso POST_NOTIFICATIONS: $hasPermission")
            Log.d(TAG, "Notificaciones del sistema habilitadas: $systemEnabled")
            
            if (!hasPermission) {
                Log.d(TAG, "Sin permisos de notificacion - Worker terminado")
                return Result.success()
            }
            
            if (!systemEnabled) {
                Log.d(TAG, "Notificaciones deshabilitadas en configuracion del sistema - Worker terminado")
                return Result.success()
            }
            
            // Verificar si es un día válido para notificar
            if (!shouldSendNotificationToday()) {
                Log.d(TAG, "Hoy no se envian notificaciones - Worker terminado")
                return Result.success()
            }
            
            // Intentar obtener la cita del día
            Log.d(TAG, "Obteniendo cita del dia...")
            val todayQuote = try {
                val language = userPreferences.appLanguage.first()
                Log.d(TAG, "Idioma seleccionado: $language")
                quoteRepository.getOrCreateTodayQuote(language)
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener cita del dia: ${e.message}")
                e.printStackTrace()
                null
            }
            
            // Enviar notificación
            if (todayQuote != null) {
                Log.d(TAG, "Enviando notificacion con cita: '${todayQuote.text.take(50)}...' - ${todayQuote.author}")
                notificationHelper.showDailyQuoteNotification(todayQuote)
                Log.d(TAG, "Notificacion enviada exitosamente")
            } else {
                Log.d(TAG, "Enviando notificacion simple (sin cita)")
                notificationHelper.showSimpleDailyNotification()
                Log.d(TAG, "Notificacion simple enviada")
            }
            
            val executionTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "Worker completado en ${executionTime}ms")
            
            Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error critico en DailyNotificationWorker: ${e.message}")
            e.printStackTrace()
            
            // En caso de error, intentar enviar notificación simple
            try {
                if (notificationHelper.hasNotificationPermission()) {
                    Log.d(TAG, "Intentando enviar notificacion de fallback...")
                    notificationHelper.showSimpleDailyNotification()
                    Log.d(TAG, "Notificacion de fallback enviada")
                }
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Error en fallback de notificacion: ${fallbackError.message}")
                fallbackError.printStackTrace()
            }
            
            Log.d(TAG, "Worker programado para retry")
            Result.retry()
        }
    }
    
    /**
     * Determinar si se debe enviar notificación hoy
     * TODO: Esto se puede expandir para incluir configuración de días de la semana
     */
    private suspend fun shouldSendNotificationToday(): Boolean {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Por ahora, enviar todos los días
        // En el futuro, esto consultará las preferencias del usuario
        return true
        
        // Ejemplo de lógica para días específicos:
        // val selectedDays = userPreferences.getNotificationDays()
        // return selectedDays.contains(dayOfWeek)
    }
}