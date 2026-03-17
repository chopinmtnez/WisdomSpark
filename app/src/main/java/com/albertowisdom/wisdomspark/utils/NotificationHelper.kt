package com.albertowisdom.wisdomspark.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.albertowisdom.wisdomspark.MainActivity
import com.albertowisdom.wisdomspark.R
import com.albertowisdom.wisdomspark.data.models.Quote
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor centralizado de notificaciones para WisdomSpark
 */
@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "NotificationHelper"
        const val CHANNEL_ID = "wisdom_daily_channel"
        const val NOTIFICATION_ID = 1001
        
        private val MOTIVATIONAL_MESSAGES = listOf(
            "🌅 Tu dosis de sabiduría matutina está lista",
            "✨ Una nueva cita te espera para inspirar tu día", 
            "💡 ¿Listo para tu momento de reflexión diaria?",
            "🎯 Tu motivación diaria ha llegado",
            "🌟 Descubre la sabiduría que te espera hoy",
            "💝 Tu inspiración diaria está aquí",
            "🔥 ¡Enciende tu día con sabiduría!",
            "🚀 Impulsa tu día con una nueva perspectiva"
        )
        
        private val CONTEXTUAL_MESSAGES = mapOf(
            1 to "🌅 ¡Feliz lunes! Comienza la semana con sabiduría",
            2 to "💪 ¡Martes de motivación! Tu cita te espera",
            3 to "🎯 Mitad de semana, mitad de inspiración",
            4 to "🌟 ¡Jueves de reflexión! Descubre tu cita",
            5 to "🎉 ¡Viernes! Celebra con sabiduría",
            6 to "🌸 Sábado de contemplación y crecimiento",
            7 to "☀️ Domingo de reflexión y nueva energía"
        )
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Crear canal de notificación para Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemNotificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Mostrar notificación de cita diaria
     */
    fun showDailyQuoteNotification(quote: Quote? = null) {
        if (!hasNotificationPermission()) {
            return
        }
        
        val dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
        val title = getContextualMessage(dayOfWeek)
        
        val content = if (quote != null) {
            // Mostrar preview de la cita (primeras 50 caracteres)
            val preview = if (quote.text.length > 50) {
                "${quote.text.take(47)}..."
            } else {
                quote.text
            }
            "\"$preview\" - ${quote.author}"
        } else {
            "Toca para descubrir tu cita de hoy"
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_daily_quote", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
            
        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permiso denegado - manejar silenciosamente
            Log.e(TAG, "Error al mostrar notificacion: permisos denegados")
        }
    }
    
    /**
     * Mostrar notificación simple sin preview de cita
     */
    fun showSimpleDailyNotification() {
        showDailyQuoteNotification(null)
    }
    
    /**
     * Verificar si tenemos permisos de notificación
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context, 
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
    
    /**
     * Cancelar todas las notificaciones pendientes
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    /**
     * Cancelar notificación específica de cita diaria
     */
    fun cancelDailyQuoteNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
    
    /**
     * Obtener mensaje contextual según el día de la semana
     */
    private fun getContextualMessage(dayOfWeek: Int): String {
        return CONTEXTUAL_MESSAGES[dayOfWeek] ?: getRandomMotivationalMessage()
    }
    
    /**
     * Obtener mensaje motivacional aleatorio
     */
    private fun getRandomMotivationalMessage(): String {
        return MOTIVATIONAL_MESSAGES.random()
    }
    
    /**
     * Verificar si las notificaciones están habilitadas en configuración del sistema
     */
    fun areNotificationsEnabledInSystem(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    
    /**
     * Verificar si deberíamos mostrar el rationale para solicitar permiso
     */
    fun shouldShowPermissionRationale(activity: android.app.Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                activity, 
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            false
        }
    }
    
    /**
     * Obtener diagnóstico detallado de permisos
     */
    fun getPermissionDiagnostic(): String {
        val diagnostic = StringBuilder()
        diagnostic.appendLine("🔍 DIAGNÓSTICO DE PERMISOS DE NOTIFICACIÓN")
        diagnostic.appendLine("====================================================")
        
        // Información de Android
        diagnostic.appendLine("📱 Android API Level: ${Build.VERSION.SDK_INT}")
        diagnostic.appendLine("🎯 Target SDK: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) "Requires POST_NOTIFICATIONS" else "No permission required"}")
        
        // Estado de permisos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = hasNotificationPermission()
            diagnostic.appendLine("🔐 POST_NOTIFICATIONS: ${if (hasPermission) "✅ GRANTED" else "❌ DENIED"}")
        } else {
            diagnostic.appendLine("🔐 POST_NOTIFICATIONS: ✅ NOT REQUIRED (API < 33)")
        }
        
        // Estado del sistema
        val systemEnabled = areNotificationsEnabledInSystem()
        diagnostic.appendLine("⚙️ System notifications: ${if (systemEnabled) "✅ ENABLED" else "❌ DISABLED"}")
        
        // Canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = systemNotificationManager.getNotificationChannel(CHANNEL_ID)
            if (channel != null) {
                diagnostic.appendLine("📡 Notification channel: ✅ CREATED")
                diagnostic.appendLine("    - Name: ${channel.name}")
                diagnostic.appendLine("    - Importance: ${channel.importance}")
                diagnostic.appendLine("    - Enabled: ${channel.importance != NotificationManager.IMPORTANCE_NONE}")
            } else {
                diagnostic.appendLine("📡 Notification channel: ❌ NOT FOUND")
            }
        }
        
        return diagnostic.toString()
    }

}