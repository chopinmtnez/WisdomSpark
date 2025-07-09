package com.albertowisdom.wisdomspark.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES"))
    private val fullDisplayDateFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES"))
    
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
    fun getCurrentDateFormatted(): String {
        return displayDateFormat.format(Date()).replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("es-ES")) else it.toString() 
        }
    }
    
    /**
     * Obtiene la fecha actual con año completo
     * Ejemplo: "Lunes, 7 de Julio de 2025"
     */
    fun getCurrentDateFormattedFull(): String {
        return fullDisplayDateFormat.format(Date()).replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("es-ES")) else it.toString() 
        }
    }
    
    /**
     * Convierte una fecha de string a formato de display
     */
    fun formatDateForDisplay(dateString: String): String? {
        return try {
            val date = dateFormat.parse(dateString)
            date?.let { 
                displayDateFormat.format(it).replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.forLanguageTag("es-ES")) else char.toString() 
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
    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Buenos días"
            in 12..17 -> "Buenas tardes"
            in 18..22 -> "Buenas noches"
            else -> "Buenas madrugadas"
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
    fun getRelativeDateString(dateString: String): String {
        return when {
            isToday(dateString) -> "Hoy"
            isYesterday(dateString) -> "Ayer"
            else -> {
                val daysAgo = getDaysAgo(dateString)
                when {
                    daysAgo == null -> formatDateForDisplay(dateString) ?: dateString
                    daysAgo <= 7 -> "Hace $daysAgo días"
                    else -> formatDateForDisplay(dateString) ?: dateString
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
    fun getDayMotivation(): String {
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.MONDAY -> "¡Nuevo comienzo, nuevas oportunidades!"
            Calendar.TUESDAY -> "La constancia es la clave del éxito"
            Calendar.WEDNESDAY -> "Mitad de semana, ¡sigue adelante!"
            Calendar.THURSDAY -> "¡Casi llegamos al final!"
            Calendar.FRIDAY -> "¡El esfuerzo de la semana da sus frutos!"
            Calendar.SATURDAY -> "Tiempo para relajar y reflexionar"
            Calendar.SUNDAY -> "Prepárate para una nueva semana increíble"
            else -> "Cada día es una nueva oportunidad"
        }
    }
}
