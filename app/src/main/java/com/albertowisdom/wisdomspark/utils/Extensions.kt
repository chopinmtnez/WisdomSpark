package com.albertowisdom.wisdomspark.utils

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object Extensions {
    
    /**
     * Muestra un Toast corto
     */
    fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Muestra un Toast largo
     */
    fun Context.showLongToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Capitaliza la primera letra de cada palabra
     */
    fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase() else it.toString() 
            }
        }
    }
    
    /**
     * Trunca texto si es muy largo
     */
    fun String.truncate(maxLength: Int): String {
        return if (length <= maxLength) this else substring(0, maxLength) + "..."
    }
}
