package com.albertowisdom.wisdomspark.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Sistema centralizado de feedback háptico para WisdomSpark
 * Proporciona diferentes tipos de feedback según el contexto de interacción
 */
class HapticManager(private val context: Context) {
    
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * Feedback ligero para toggles, switches, y cambios menores
     */
    fun lightImpact() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(25)
        }
    }

    /**
     * Feedback medio para botones normales y navegación
     */
    fun mediumImpact() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    /**
     * Feedback fuerte para acciones importantes (favoritos, compartir)
     */
    fun heavyImpact() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    /**
     * Feedback de éxito para confirmaciones
     */
    fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 50, 50, 50)
            val amplitudes = intArrayOf(0, 100, 0, 150)
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, amplitudes, -1)
            )
        } else {
            val pattern = longArrayOf(0, 50, 50, 50)
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    /**
     * Feedback de error para fallos
     */
    fun error() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 100, 100, 100, 100, 100)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, amplitudes, -1)
            )
        } else {
            val pattern = longArrayOf(0, 100, 100, 100, 100, 100)
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    /**
     * Feedback sutil para interacciones de selección
     */
    fun selection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(20, 50)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(20)
        }
    }
}

/**
 * Extension functions para usar con HapticFeedback de Compose
 */
fun HapticFeedback.performButtonClick() {
    performHapticFeedback(HapticFeedbackType.TextHandleMove)
}

fun HapticFeedback.performToggle() {
    performHapticFeedback(HapticFeedbackType.LongPress)
}

fun HapticFeedback.performSelection() {
    performHapticFeedback(HapticFeedbackType.TextHandleMove)
}

fun HapticFeedback.performSuccess() {
    performHapticFeedback(HapticFeedbackType.LongPress)
}
