package com.albertowisdom.wisdomspark.utils

object Constants {
    const val DATABASE_NAME = "wisdom_spark.db"
    const val PREFS_NAME = "wisdom_spark_prefs"
    
    // SharedPreferences keys
    const val KEY_FIRST_LAUNCH = "first_launch"
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val KEY_NOTIFICATION_TIME = "notification_time"
    const val KEY_THEME_MODE = "theme_mode"
    
    // Categories
    val DEFAULT_CATEGORIES = listOf(
        "Motivación",
        "Vida", 
        "Sueños",
        "Perseverancia",
        "Educación",
        "Creatividad",
        "Éxito",
        "Autenticidad",
        "Felicidad",
        "Sabiduría",
        "Confianza",
        "Progreso",
        "Excelencia",
        "Acción"
    )
}
