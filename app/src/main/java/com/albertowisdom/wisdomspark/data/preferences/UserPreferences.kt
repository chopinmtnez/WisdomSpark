package com.albertowisdom.wisdomspark.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
}
