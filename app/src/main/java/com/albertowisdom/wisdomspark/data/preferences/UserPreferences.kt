package com.albertowisdom.wisdomspark.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val SWIPEABLE_MODE_KEY = booleanPreferencesKey("swipeable_mode")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val HAPTIC_FEEDBACK_KEY = booleanPreferencesKey("haptic_feedback")
        private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
        private val NOTIFICATION_HOUR_KEY = intPreferencesKey("notification_hour")
        private val NOTIFICATION_MINUTE_KEY = intPreferencesKey("notification_minute")
        private val NOTIFICATION_DAYS_KEY = stringSetPreferencesKey("notification_days")
        private val NOTIFICATION_PERMISSION_ASKED_KEY = booleanPreferencesKey("notification_permission_asked")
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val FIRST_LAUNCH_KEY = booleanPreferencesKey("first_launch")
        
        // Premium preferences
        private val PREMIUM_STATUS_KEY = booleanPreferencesKey("premium_status")
        private val PREMIUM_PURCHASE_TOKEN_KEY = stringPreferencesKey("premium_purchase_token")
        private val PREMIUM_PURCHASE_TIME_KEY = stringPreferencesKey("premium_purchase_time")
        private val PREMIUM_PLAN_KEY = stringPreferencesKey("premium_plan")
        private val PREMIUM_EXPIRY_TIME_KEY = stringPreferencesKey("premium_expiry_time")
    }
    
    // Flow para modo swipeable
    val isSwipeableModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SWIPEABLE_MODE_KEY] ?: false
        }
    
    // Flow para dark mode
    val isDarkModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }
    
    // Flow para haptic feedback
    val isHapticFeedbackEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HAPTIC_FEEDBACK_KEY] ?: true
        }
    
    // Flow para notificaciones
    val areNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_KEY] ?: true
        }
    
    // Flow para hora de notificación
    val notificationHour: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATION_HOUR_KEY] ?: 9 // 9:00 AM por defecto
        }
    
    // Flow para minuto de notificación  
    val notificationMinute: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATION_MINUTE_KEY] ?: 0 // :00 por defecto
        }
    
    // Flow para días de notificación (1=Domingo, 2=Lunes, etc.)
    val notificationDays: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATION_DAYS_KEY] ?: setOf("1", "2", "3", "4", "5", "6", "7") // Todos los días por defecto
        }
    
    // Flow para idioma de la aplicación que sincroniza con SharedPreferences
    val appLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            val dataStoreLanguage = preferences[LANGUAGE_KEY]
            if (dataStoreLanguage != null) {
                dataStoreLanguage
            } else {
                // Fallback: verificar SharedPreferences para compatibilidad con LocaleHelper
                val sharedPrefs = context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
                val sharedPrefsLanguage = sharedPrefs.getString("app_language", null)
                sharedPrefsLanguage ?: getSystemLanguage()
            }
        }
    
    // Flow para detectar primer lanzamiento
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[FIRST_LAUNCH_KEY] ?: true
        }
    
    // Flow para saber si ya se pidió el permiso de notificación
    val notificationPermissionAsked: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATION_PERMISSION_ASKED_KEY] ?: false
        }
    
    // Funciones para cambiar preferencias
    suspend fun setSwipeableMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SWIPEABLE_MODE_KEY] = enabled
        }
    }
    
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
    
    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK_KEY] = enabled
        }
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_KEY] = enabled
        }
    }
    
    suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_HOUR_KEY] = hour
            preferences[NOTIFICATION_MINUTE_KEY] = minute
        }
    }
    
    suspend fun setNotificationDays(days: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_DAYS_KEY] = days
        }
    }
    
    suspend fun setAppLanguage(language: String) {
        // Guardar en DataStore
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
        
        // Sincronizar con SharedPreferences para compatibilidad con LocaleHelper
        // FIXED: Usar commit() para garantizar sincronización síncrona y evitar race conditions
        try {
            val sharedPrefs = context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
            val success = sharedPrefs.edit()
                .putString("app_language", language)
                .commit() // ✅ SÍNCRONO - garantiza orden y evita condición de carrera
                
            if (success) {
                android.util.Log.d("UserPreferences", "Language synchronized successfully: $language")
            } else {
                android.util.Log.w("UserPreferences", "Failed to commit language to SharedPreferences: $language")
            }
        } catch (e: Exception) {
            // Log error but don't fail the operation
            android.util.Log.e("UserPreferences", "Exception syncing language to SharedPreferences: ${e.message}")
        }
    }
    
    suspend fun getAppLanguage(): String {
        return appLanguage.first()
    }
    
    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH_KEY] = false
        }
    }
    
    suspend fun setNotificationPermissionAsked(asked: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_PERMISSION_ASKED_KEY] = asked
        }
    }
    
    /**
     * Obtener idioma del sistema
     */
    private fun getSystemLanguage(): String {
        val systemLocale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        
        return when (systemLocale.language) {
            "es" -> "es" // Español
            "en" -> "en" // Inglés
            "fr" -> "fr" // Francés
            "de" -> "de" // Alemán
            "pt" -> "pt" // Portugués
            "it" -> "it" // Italiano
            else -> "es" // Español por defecto
        }
    }
    
    /**
     * Obtener lista de idiomas soportados
     */
    fun getSupportedLanguages(): List<SupportedLanguage> {
        return listOf(
            SupportedLanguage("es", "Español", "🇪🇸"),
            SupportedLanguage("en", "English", "🇺🇸"),
            SupportedLanguage("fr", "Français", "🇫🇷"),
            SupportedLanguage("de", "Deutsch", "🇩🇪"),
            SupportedLanguage("pt", "Português", "🇧🇷"),
            SupportedLanguage("it", "Italiano", "🇮🇹")
        )
    }
    
    // Premium preferences flows
    val isPremium: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PREMIUM_STATUS_KEY] ?: false
        }
    
    val premiumPurchaseToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PREMIUM_PURCHASE_TOKEN_KEY]
        }
    
    val premiumPlan: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PREMIUM_PLAN_KEY]
        }
    
    val premiumPurchaseTime: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PREMIUM_PURCHASE_TIME_KEY]
        }
    
    val premiumExpiryTime: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PREMIUM_EXPIRY_TIME_KEY]
        }
    
    // Premium preference setters
    suspend fun setPremiumStatus(isPremium: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PREMIUM_STATUS_KEY] = isPremium
        }
    }
    
    suspend fun setPremiumPurchaseToken(token: String?) {
        context.dataStore.edit { preferences ->
            if (token != null) {
                preferences[PREMIUM_PURCHASE_TOKEN_KEY] = token
            } else {
                preferences.remove(PREMIUM_PURCHASE_TOKEN_KEY)
            }
        }
    }
    
    suspend fun setPremiumPlan(plan: String?) {
        context.dataStore.edit { preferences ->
            if (plan != null) {
                preferences[PREMIUM_PLAN_KEY] = plan
            } else {
                preferences.remove(PREMIUM_PLAN_KEY)
            }
        }
    }
    
    suspend fun setPremiumPurchaseTime(time: String?) {
        context.dataStore.edit { preferences ->
            if (time != null) {
                preferences[PREMIUM_PURCHASE_TIME_KEY] = time
            } else {
                preferences.remove(PREMIUM_PURCHASE_TIME_KEY)
            }
        }
    }
    
    suspend fun setPremiumExpiryTime(time: String?) {
        context.dataStore.edit { preferences ->
            if (time != null) {
                preferences[PREMIUM_EXPIRY_TIME_KEY] = time
            } else {
                preferences.remove(PREMIUM_EXPIRY_TIME_KEY)
            }
        }
    }
    
    suspend fun clearPremiumData() {
        context.dataStore.edit { preferences ->
            preferences.remove(PREMIUM_STATUS_KEY)
            preferences.remove(PREMIUM_PURCHASE_TOKEN_KEY)
            preferences.remove(PREMIUM_PLAN_KEY)
            preferences.remove(PREMIUM_PURCHASE_TIME_KEY)
            preferences.remove(PREMIUM_EXPIRY_TIME_KEY)
        }
    }
}

/**
 * Clase para representar un idioma soportado
 */
data class SupportedLanguage(
    val code: String,
    val name: String,
    val flag: String
)
