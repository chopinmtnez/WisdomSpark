package com.albertowisdom.wisdomspark.presentation.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albertowisdom.wisdomspark.security.IntegrityResult
import com.albertowisdom.wisdomspark.security.PlayIntegrityManager
import com.albertowisdom.wisdomspark.security.PlayServicesInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestionar el estado de seguridad e integridad de la aplicación
 */
@HiltViewModel
class SecurityStatusViewModel @Inject constructor(
    private val playIntegrityManager: PlayIntegrityManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SecurityUiState>(SecurityUiState.Loading)
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "SecurityStatusViewModel"
    }

    init {
        checkSecurity()
    }

    /**
     * Realizar verificación de seguridad
     */
    fun checkSecurity() {
        viewModelScope.launch {
            try {
                _uiState.value = SecurityUiState.Loading
                Log.d(TAG, "🔐 Iniciando verificación de seguridad...")

                // Obtener información de Play Services
                val playServicesInfo = playIntegrityManager.getPlayServicesInfo()
                Log.d(TAG, "Play Services disponible: ${playServicesInfo.isAvailable}")

                // Realizar verificación de integridad
                val integrityResult = try {
                    playIntegrityManager.verifyIntegrity()
                } catch (e: Exception) {
                    Log.w(TAG, "Error en verificación de integridad: ${e.message}")
                    null
                }

                _uiState.value = SecurityUiState.Success(
                    integrityResult = integrityResult,
                    playServicesInfo = playServicesInfo
                )

                Log.d(TAG, "✅ Verificación de seguridad completada")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en verificación de seguridad: ${e.message}", e)
                _uiState.value = SecurityUiState.Error(
                    error = e.message ?: "Error desconocido en verificación de seguridad"
                )
            }
        }
    }

    /**
     * Verificar solo la integridad básica
     */
    fun checkBasicIntegrity() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔍 Verificando integridad básica...")
                val isBasicIntegrityOk = playIntegrityManager.performBasicIntegrityCheck()
                Log.d(TAG, "Integridad básica: ${if (isBasicIntegrityOk) "✅ OK" else "❌ FALLO"}")
            } catch (e: Exception) {
                Log.e(TAG, "Error en verificación básica: ${e.message}", e)
            }
        }
    }

    /**
     * Estados de la UI para seguridad
     */
    sealed class SecurityUiState {
        object Loading : SecurityUiState()
        
        data class Success(
            val integrityResult: IntegrityResult?,
            val playServicesInfo: PlayServicesInfo
        ) : SecurityUiState()
        
        data class Error(
            val error: String
        ) : SecurityUiState()
    }
}