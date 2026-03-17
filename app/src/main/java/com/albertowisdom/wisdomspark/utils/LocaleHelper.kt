package com.albertowisdom.wisdomspark.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "locale_prefs"
    private const val LANGUAGE_KEY = "app_language"

    fun updateBaseContextLocale(context: Context): Context {
        return try {
            val savedLanguage = getSavedLanguage(context)
            
            // Verificar si el idioma ya está aplicado para evitar trabajo innecesario
            val currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }
            
            if (currentLocale.language == savedLanguage) {
                Log.d("LocaleHelper", "Idioma ya aplicado: $savedLanguage")
                return context
            }
            
            Log.d("LocaleHelper", "Aplicando idioma: $savedLanguage")
            
            val locale = Locale.forLanguageTag(savedLanguage)
            Locale.setDefault(locale)
            
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(locale)
            
            val newContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.createConfigurationContext(configuration)
            } else {
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
                context
            }
            
            Log.d("LocaleHelper", "Idioma aplicado exitosamente: $savedLanguage")
            newContext
        } catch (e: Exception) {
            Log.e("LocaleHelper", "Error aplicando idioma: ${e.message}")
            context
        }
    }

    private fun getSavedLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString(LANGUAGE_KEY, null)
        return if (savedLanguage != null) {
            Log.d("LocaleHelper", "Idioma guardado encontrado: $savedLanguage")
            savedLanguage
        } else {
            // En primer lanzamiento, usar español por defecto como especifica UserPreferences
            val defaultLanguage = "es"
            Log.d("LocaleHelper", "Primer lanzamiento - usando idioma por defecto: $defaultLanguage")
            defaultLanguage
        }
    }

    fun saveLanguage(context: Context, languageCode: String) {
        try {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val success = prefs.edit()
                .putString(LANGUAGE_KEY, languageCode)
                .commit() // Usar commit() para guardado síncrono inmediato
            if (success) {
                Log.d("LocaleHelper", "Idioma guardado inmediatamente en SharedPreferences: $languageCode")
            } else {
                Log.w("LocaleHelper", "Fallo al guardar idioma en SharedPreferences: $languageCode")
            }
        } catch (e: Exception) {
            Log.e("LocaleHelper", "Error guardando idioma en SharedPreferences: ${e.message}")
        }
    }
    
    /**
     * Modern language change using AppCompatDelegate for Android 13+ with optimizations
     */
    fun changeLanguageModern(languageCode: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+: Use AppCompatDelegate (handles recreation automatically)
                val localeList = LocaleListCompat.forLanguageTags(languageCode)
                AppCompatDelegate.setApplicationLocales(localeList)
                Log.d("LocaleHelper", "✅ Idioma aplicado con AppCompatDelegate (automático): $languageCode")
            } else {
                // Fallback: Just set default, Activity recreation will handle the rest
                val locale = Locale.forLanguageTag(languageCode)
                Locale.setDefault(locale)
                Log.d("LocaleHelper", "✅ Locale.setDefault aplicado: $languageCode")
            }
        } catch (e: Exception) {
            Log.e("LocaleHelper", "❌ Error aplicando idioma moderno: ${e.message}")
        }
    }
    
    /**
     * Fast language change for immediate UI update - optimized version
     */
    fun changeLanguageImmediate(languageCode: String) {
        try {
            val locale = Locale.forLanguageTag(languageCode)
            Locale.setDefault(locale)
            Log.d("LocaleHelper", "⚡ Cambio inmediato de idioma aplicado: $languageCode")
        } catch (e: Exception) {
            Log.e("LocaleHelper", "❌ Error en cambio inmediato: ${e.message}")
        }
    }
    
    fun getCurrentSavedLanguage(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, null)
    }

    fun getSystemLanguage(context: Context): String {
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
}