package com.albertowisdom.wisdomspark.workers

import android.content.Context
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
        const val WORKER_NAME = "daily_notification_worker"
        const val WORKER_TAG = "wisdom_daily_notifications"
    }

    override suspend fun doWork(): Result {
        return try {
            // Verificar si las notificaciones están habilitadas
            val notificationsEnabled = userPreferences.areNotificationsEnabled.first()
            
            if (!notificationsEnabled) {
                println("📵 Notificaciones deshabilitadas por el usuario")
                return Result.success()
            }
            
            // Verificar permisos del sistema
            if (!notificationHelper.hasNotificationPermission()) {
                println("🚫 Sin permisos de notificación")
                return Result.success()
            }
            
            // Verificar si es un día válido para notificar
            if (!shouldSendNotificationToday()) {
                println("📅 Hoy no se envían notificaciones")
                return Result.success()
            }
            
            // Intentar obtener la cita del día
            val todayQuote = try {
                quoteRepository.getOrCreateTodayQuote()
            } catch (e: Exception) {
                println("⚠️ Error al obtener cita del día: ${e.message}")
                null
            }
            
            // Enviar notificación
            if (todayQuote != null) {
                println("📱 Enviando notificación con cita: ${todayQuote.text.take(30)}...")
                notificationHelper.showDailyQuoteNotification(todayQuote)
            } else {
                println("📱 Enviando notificación simple")
                notificationHelper.showSimpleDailyNotification()
            }
            
            Result.success()
            
        } catch (e: Exception) {
            println("❌ Error en DailyNotificationWorker: ${e.message}")
            e.printStackTrace()
            
            // En caso de error, intentar enviar notificación simple
            try {
                if (notificationHelper.hasNotificationPermission()) {
                    notificationHelper.showSimpleDailyNotification()
                }
            } catch (fallbackError: Exception) {
                println("❌ Error en fallback de notificación: ${fallbackError.message}")
            }
            
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