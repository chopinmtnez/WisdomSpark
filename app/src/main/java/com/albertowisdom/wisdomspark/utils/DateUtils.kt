package com.albertowisdom.wisdomspark.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private fun getLocale(context: Context): Locale {
        val prefs = context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", Locale.getDefault().language) ?: Locale.getDefault().language
        return Locale.forLanguageTag(languageCode)
    }
    
    private fun getDisplayDateFormat(context: Context): SimpleDateFormat {
        val locale = getLocale(context)
        return when (locale.language) {
            "es" -> SimpleDateFormat("EEEE, d 'de' MMMM", locale)
            "en" -> SimpleDateFormat("EEEE, MMMM d", locale)
            "fr" -> SimpleDateFormat("EEEE d MMMM", locale)
            "de" -> SimpleDateFormat("EEEE, d. MMMM", locale)
            "pt" -> SimpleDateFormat("EEEE, d 'de' MMMM", locale)
            "it" -> SimpleDateFormat("EEEE, d MMMM", locale)
            else -> SimpleDateFormat("EEEE, d MMMM", locale)
        }
    }
    
    private fun getFullDisplayDateFormat(context: Context): SimpleDateFormat {
        val locale = getLocale(context)
        return when (locale.language) {
            "es" -> SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", locale)
            "en" -> SimpleDateFormat("EEEE, MMMM d, yyyy", locale)
            "fr" -> SimpleDateFormat("EEEE d MMMM yyyy", locale)
            "de" -> SimpleDateFormat("EEEE, d. MMMM yyyy", locale)
            "pt" -> SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", locale)
            "it" -> SimpleDateFormat("EEEE, d MMMM yyyy", locale)
            else -> SimpleDateFormat("EEEE, d MMMM yyyy", locale)
        }
    }
    
    /**
     * Obtiene la fecha actual en formato para base de datos (yyyy-MM-dd)
     */
    fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }
    
    /**
     * Obtiene la fecha actual formateada para mostrar al usuario
     * Ejemplo: "Lunes, 7 de Julio"
     */
    fun getCurrentDateFormatted(context: Context): String {
        val displayFormat = getDisplayDateFormat(context)
        val locale = getLocale(context)
        return displayFormat.format(Date()).replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(locale) else it.toString() 
        }
    }
    
    /**
     * Obtiene la fecha actual con año completo
     * Ejemplo: "Lunes, 7 de Julio de 2025"
     */
    fun getCurrentDateFormattedFull(context: Context): String {
        val fullDisplayFormat = getFullDisplayDateFormat(context)
        val locale = getLocale(context)
        return fullDisplayFormat.format(Date()).replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(locale) else it.toString() 
        }
    }
    
    /**
     * Convierte una fecha de string a formato de display
     */
    fun formatDateForDisplay(context: Context, dateString: String): String? {
        return try {
            val date = dateFormat.parse(dateString)
            val displayFormat = getDisplayDateFormat(context)
            val locale = getLocale(context)
            date?.let { 
                displayFormat.format(it).replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(locale) else char.toString() 
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Verifica si una fecha es hoy
     */
    fun isToday(dateString: String): Boolean {
        return dateString == getCurrentDate()
    }
    
    /**
     * Verifica si una fecha es de ayer
     */
    fun isYesterday(dateString: String): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = dateFormat.format(calendar.time)
        return dateString == yesterday
    }
    
    /**
     * Obtiene un saludo basado en la hora del día
     */
    fun getGreeting(context: Context): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> context.getString(com.albertowisdom.wisdomspark.R.string.good_morning)
            in 12..17 -> context.getString(com.albertowisdom.wisdomspark.R.string.good_afternoon)
            in 18..22 -> context.getString(com.albertowisdom.wisdomspark.R.string.good_evening)
            else -> context.getString(com.albertowisdom.wisdomspark.R.string.good_night)
        }
    }
    
    /**
     * Obtiene los días transcurridos desde una fecha
     */
    fun getDaysAgo(dateString: String): Int? {
        return try {
            val date = dateFormat.parse(dateString)
            val today = Date()
            date?.let {
                val diffInMs = today.time - it.time
                val diffInDays = diffInMs / (1000 * 60 * 60 * 24)
                diffInDays.toInt()
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Formatea una fecha relativa (hoy, ayer, hace X días)
     */
    fun getRelativeDateString(context: Context, dateString: String): String {
        return when {
            isToday(dateString) -> context.getString(com.albertowisdom.wisdomspark.R.string.today)
            isYesterday(dateString) -> context.getString(com.albertowisdom.wisdomspark.R.string.yesterday)
            else -> {
                val daysAgo = getDaysAgo(dateString)
                when {
                    daysAgo == null -> formatDateForDisplay(context, dateString) ?: dateString
                    daysAgo <= 7 -> context.getString(com.albertowisdom.wisdomspark.R.string.days_ago, daysAgo)
                    else -> formatDateForDisplay(context, dateString) ?: dateString
                }
            }
        }
    }
    
    /**
     * Obtiene el emoji del día de la semana
     */
    fun getDayEmoji(): String {
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.MONDAY -> "💪"
            Calendar.TUESDAY -> "🔥"
            Calendar.WEDNESDAY -> "⚡"
            Calendar.THURSDAY -> "🌟"
            Calendar.FRIDAY -> "🎉"
            Calendar.SATURDAY -> "😎"
            Calendar.SUNDAY -> "🌅"
            else -> "✨"
        }
    }
    
    /**
     * Obtiene una frase motivacional basada en el día de la semana
     */
    fun getDayMotivation(context: Context): String {
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.MONDAY -> context.getString(com.albertowisdom.wisdomspark.R.string.monday_motivation)
            Calendar.TUESDAY -> context.getString(com.albertowisdom.wisdomspark.R.string.tuesday_motivation)
            Calendar.WEDNESDAY -> context.getString(com.albertowisdom.wisdomspark.R.string.wednesday_motivation)
            Calendar.THURSDAY -> context.getString(com.albertowisdom.wisdomspark.R.string.thursday_motivation)
            Calendar.FRIDAY -> context.getString(com.albertowisdom.wisdomspark.R.string.friday_motivation)
            Calendar.SATURDAY -> context.getString(com.albertowisdom.wisdomspark.R.string.saturday_motivation)
            Calendar.SUNDAY -> context.getString(com.albertowisdom.wisdomspark.R.string.sunday_motivation)
            else -> context.getString(com.albertowisdom.wisdomspark.R.string.default_motivation)
        }
    }
}
