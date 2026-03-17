package com.albertowisdom.wisdomspark.premium.debug

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Helper para debug y testing del sistema premium
 * Solo para desarrollo - NO incluir en release builds
 */
object PremiumDebugHelper {
    
    private const val PREFS_NAME = "premium_debug"
    private const val KEY_TESTING_MODE = "testing_mode_enabled"
    private const val TAG = "PremiumDebugHelper"
    
    /**
     * Verificar si el modo de testing está habilitado
     */
    fun isTestingModeEnabled(context: Context): Boolean {
        val prefs = getPrefs(context)
        val enabled = prefs.getBoolean(KEY_TESTING_MODE, false)
        Log.d(TAG, "🧪 Modo testing premium: $enabled")
        return enabled
    }
    
    /**
     * Activar/desactivar modo de testing
     */
    fun setTestingMode(context: Context, enabled: Boolean) {
        val prefs = getPrefs(context)
        prefs.edit().putBoolean(KEY_TESTING_MODE, enabled).apply()
        Log.d(TAG, if (enabled) "✅ Modo testing activado" else "❌ Modo testing desactivado")
    }
    
    /**
     * Alternar modo de testing
     */
    fun toggleTestingMode(context: Context): Boolean {
        val currentMode = isTestingModeEnabled(context)
        val newMode = !currentMode
        setTestingMode(context, newMode)
        return newMode
    }
    
    /**
     * Obtener información de debug
     */
    fun getDebugInfo(context: Context): String {
        return buildString {
            appendLine("=== PREMIUM DEBUG INFO ===")
            appendLine("Modo Testing: ${isTestingModeEnabled(context)}")
            appendLine("Build Type: ${if (android.os.Build.VERSION.SDK_INT >= 33) "Debug" else "Unknown"}")
            appendLine("Timestamp: ${System.currentTimeMillis()}")
        }
    }
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}