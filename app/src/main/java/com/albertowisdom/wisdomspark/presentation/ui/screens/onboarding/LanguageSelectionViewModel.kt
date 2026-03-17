package com.albertowisdom.wisdomspark.presentation.ui.screens.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albertowisdom.wisdomspark.data.preferences.SupportedLanguage
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.managers.LanguageManager
import com.albertowisdom.wisdomspark.data.remote.repository.SyncResult
import com.albertowisdom.wisdomspark.utils.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val languageManager: LanguageManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LanguageSelectionUiState())
    val uiState: StateFlow<LanguageSelectionUiState> = _uiState.asStateFlow()
    
    init {
        // Leer directamente desde SharedPreferences para sincronización inmediata
        try {
            val savedLanguage = LocaleHelper.getCurrentSavedLanguage(context) ?: "es"
            _uiState.value = _uiState.value.copy(selectedLanguage = savedLanguage)
            android.util.Log.d("LanguageSelectionVM", "✅ Idioma inicial cargado desde SharedPreferences: $savedLanguage")
        } catch (e: Exception) {
            // Fallback: usar español por defecto
            _uiState.value = _uiState.value.copy(selectedLanguage = "es")
            android.util.Log.w("LanguageSelectionVM", "⚠️ Error cargando idioma inicial, usando 'es': ${e.message}")
        }
    }
    
    fun selectLanguage(languageCode: String, context: Context? = null) {
        _uiState.value = _uiState.value.copy(selectedLanguage = languageCode)
        
        // Solo guardar el idioma seleccionado, NO reiniciar en onboarding
        context?.let { ctx ->
            viewModelScope.launch {
                try {
                    // 1. Guardar el idioma seleccionado inmediatamente
                    LocaleHelper.saveLanguage(ctx, languageCode)
                    
                    // 2. Aplicar usando AppCompatDelegate para Android 13+
                    LocaleHelper.changeLanguageModern(languageCode)
                    
                    // 3. NO reiniciar Activity durante onboarding - dejar que se complete
                    android.util.Log.d("LanguageSelectionVM", "Idioma seleccionado (sin reinicio): $languageCode")
                } catch (e: Exception) {
                    android.util.Log.e("LanguageSelectionVM", "Error aplicando idioma: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Reinicia la Activity actual para aplicar cambios de idioma
     * Usa método robusto con Intent para asegurar que se aplique el nuevo locale
     */
    private fun restartActivity(context: Context) {
        try {
            if (context is android.app.Activity) {
                // Método más robusto: crear Intent para reiniciar la app completamente
                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                intent?.let {
                    it.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                    context.finish() // Terminar la activity actual
                }
                android.util.Log.d("LanguageSelectionVM", "Activity reiniciada con Intent")
            }
        } catch (e: Exception) {
            android.util.Log.e("LanguageSelectionVM", "Error reiniciando Activity: ${e.message}")
            // Fallback: intentar recreate() como último recurso
            try {
                if (context is android.app.Activity) {
                    context.recreate()
                }
            } catch (e2: Exception) {
                android.util.Log.e("LanguageSelectionVM", "Error en fallback recreate(): ${e2.message}")
            }
        }
    }
    
    fun completeOnboarding(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, syncingData = true)
            
            try {
                val selectedLanguage = _uiState.value.selectedLanguage
                android.util.Log.d("LanguageSelectionVM", "🚀 Completando onboarding para idioma: $selectedLanguage")
                
                // 1. Sincronizar base de datos para el idioma seleccionado
                android.util.Log.d("LanguageSelectionVM", "🔄 Sincronizando base de datos...")
                val syncResult = languageManager.changeLanguage(selectedLanguage)
                
                if (syncResult is SyncResult.Success) {
                    android.util.Log.d("LanguageSelectionVM", "✅ Sincronización exitosa")
                    
                    // 2. Asegurar que está guardado en SharedPreferences
                    LocaleHelper.saveLanguage(context, selectedLanguage)
                    
                    // 3. Marcar que el onboarding se completó
                    userPreferences.setFirstLaunchCompleted()
                    
                    // 4. Delay adicional para asegurar estabilidad
                    kotlinx.coroutines.delay(1000)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        syncingData = false,
                        isCompleted = true
                    )
                    android.util.Log.d("LanguageSelectionVM", "🎉 Onboarding completado exitosamente")
                    
                } else {
                    android.util.Log.e("LanguageSelectionVM", "❌ Error en sincronización: $syncResult")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        syncingData = false,
                        error = "Error sincronizando datos. Intenta de nuevo."
                    )
                }
                
            } catch (e: Exception) {
                android.util.Log.e("LanguageSelectionVM", "❌ Error completando onboarding: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    syncingData = false,
                    error = e.message
                )
            }
        }
    }
    
    fun getSupportedLanguages(): List<SupportedLanguage> {
        return userPreferences.getSupportedLanguages()
    }
}

data class LanguageSelectionUiState(
    val selectedLanguage: String = "",
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null,
    val syncingData: Boolean = false
)