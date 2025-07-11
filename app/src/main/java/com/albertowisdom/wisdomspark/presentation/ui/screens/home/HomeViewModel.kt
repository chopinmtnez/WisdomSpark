package com.albertowisdom.wisdomspark.presentation.ui.screens.home

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HomeViewModel con integración AdMob
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository,
    private val adMobManager: AdMobManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Contador de interacciones para interstitials
    private var userInteractions = 0

    init {
        println("🏠 HomeViewModel inicializado")
        loadTodayQuote()
    }

    /**
     * Cargar cita del día
     */
    fun loadTodayQuote() {
        println("🔄 Cargando cita del día...")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            println("📊 Estado cambiado a loading: ${_uiState.value}")

            try {
                val quote = quoteRepository.getOrCreateTodayQuote()
                println("✅ Cita obtenida: ${quote.text.take(50)}...")
                _uiState.value = _uiState.value.copy(
                    todayQuote = quote,
                    isLoading = false,
                    error = null
                )
                println("📊 Estado final: ${_uiState.value}")
            } catch (e: Exception) {
                println("❌ Error cargando cita: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No pudimos cargar tu cita diaria. Verifica tu conexión e intenta de nuevo."
                )
                println("📊 Estado error: ${_uiState.value}")
            }
        }
    }

    /**
     * Toggle favorito con lógica de interstitial
     */
    fun toggleFavorite() {
        val currentQuote = _uiState.value.todayQuote ?: return

        viewModelScope.launch {
            try {
                val updatedQuote = currentQuote.copy(isFavorite = !currentQuote.isFavorite)
                quoteRepository.updateQuote(updatedQuote)

                _uiState.value = _uiState.value.copy(todayQuote = updatedQuote)

                // Incrementar interacciones para AdMob
                incrementUserInteraction()

            } catch (e: Exception) {
                // Manejar error silenciosamente o mostrar snackbar
            }
        }
    }

    /**
     * Compartir cita con tracking de interacciones
     */
    fun shareQuote(activity: Activity) {
        incrementUserInteraction()

        // Mostrar interstitial ocasionalmente al compartir
        if (userInteractions % 4 == 0) { // Cada 4 compartidos
            adMobManager.showInterstitialAd(activity)
        }
    }

    /**
     * Incrementar contador de interacciones y mostrar interstitial cuando corresponda
     */
    private fun incrementUserInteraction() {
        userInteractions++

        // Lógica para mostrar interstitial se maneja en AdMobManager
        // basado en AdMobManager.INTERSTITIAL_FREQUENCY
    }

    /**
     * Mostrar interstitial manualmente (para testing o eventos especiales)
     */
    fun showInterstitialAd(activity: Activity, force: Boolean = false) {
        adMobManager.showInterstitialAd(activity, force)
    }

    /**
     * Reset contador de interacciones (útil para testing)
     */
    fun resetInteractions() {
        userInteractions = 0
        adMobManager.resetInteractionCount()
    }

    /**
     * Verificar si el usuario es premium
     */
    fun isPremiumUser(): Boolean = !adMobManager.shouldShowAds()

    /**
     * Activar modo premium (para testing o después de compra)
     */
    fun activatePremium() {
        adMobManager.activatePremium()
    }

    /**
     * Generar nuevas citas para el modo swipeable
     */
    fun generateNewQuotes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Limpiar la cita del día actual para forzar nueva generación
                quoteRepository.resetTodayQuotes()
                
                // Cargar nueva cita del día
                val quote = quoteRepository.getOrCreateTodayQuote()
                
                _uiState.value = _uiState.value.copy(
                    todayQuote = quote,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No pudimos cargar nuevas citas. Verifica tu conexión e intenta de nuevo."
                )
            }
        }
    }
}

/**
 * UI State para HomeScreen
 */
data class HomeUiState(
    val todayQuote: Quote? = null,
    val isLoading: Boolean = true, // CAMBIO: Iniciar en loading para mostrar algo
    val error: String? = null
)
