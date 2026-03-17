package com.albertowisdom.wisdomspark.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

/**
 * ContextWrapper que aplica localización preservando las características del contexto base,
 * especialmente importante para Hilt que necesita un Activity context.
 */
class LocaleContextWrapper(base: Context) : ContextWrapper(base) {
    
    companion object {
        fun wrap(context: Context, language: String): Context {
            val locale = Locale.forLanguageTag(language)
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(locale)
            
            return LocaleContextWrapper(context.createConfigurationContext(configuration))
        }
    }
    
    override fun getResources(): Resources {
        return baseContext.resources
    }
    
    override fun getApplicationContext(): Context {
        return baseContext.applicationContext
    }
}