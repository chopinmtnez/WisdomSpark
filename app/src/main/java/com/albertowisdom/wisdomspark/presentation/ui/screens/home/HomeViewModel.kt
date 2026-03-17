package com.albertowisdom.wisdomspark.presentation.ui.screens.home

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.managers.LanguageManager
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HomeViewModel con integración AdMob
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository,
    private val adMobManager: AdMobManager,
    private val userPreferences: UserPreferences,
    private val languageManager: LanguageManager
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val FREE_FAVORITES_LIMIT = 10
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Evento one-shot: el usuario alcanzó el límite de favoritos
    private val _favoritesLimitReached = MutableSharedFlow<Int>()
    val favoritesLimitReached: SharedFlow<Int> = _favoritesLimitReached.asSharedFlow()

    // Contador de interacciones para interstitials
    private var userInteractions = 0

    init {
        Log.d(TAG, "HomeViewModel initialized")
        observeLanguageChanges()
    }

    /**
     * Observa cambios en el idioma y espera a que el LanguageManager complete la sincronización
     * Ya no realiza sincronización duplicada - el LanguageManager coordina todo centralmente
     */
    private fun observeLanguageChanges() {
        viewModelScope.launch {
            combine(
                userPreferences.appLanguage,
                languageManager.languageChangeInProgress
            ) { language, syncInProgress ->
                Log.d(TAG, "Language: $language, syncInProgress: $syncInProgress")
                Pair(language, syncInProgress)
            }.collectLatest { (language, syncInProgress) ->
                if (!syncInProgress) {
                    Log.d(TAG, "Sync completed, loading today's quote for: $language")
                    loadTodayQuote(language)
                } else {
                    Log.d(TAG, "Waiting for language sync to complete...")
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    /**
     * Cargar cita del día para el idioma especificado.
     * Es suspend fun directa (no lanza coroutine interna) para que collectLatest
     * pueda cancelarla correctamente al cambiar de idioma.
     */
    private suspend fun loadTodayQuote(language: String) {
        Log.d(TAG, "Loading today's quote for language: $language")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        try {
            val quote = quoteRepository.getOrCreateTodayQuote(language)
            Log.d(TAG, "Quote loaded for $language: ${quote.text.take(50)}...")
            _uiState.value = _uiState.value.copy(
                todayQuote = quote,
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading quote", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "error_loading_quote" // Clave para que la UI use stringResource()
            )
        }
    }

    /**
     * Recargar cita del día manualmente
     */
    fun refreshTodayQuote() {
        viewModelScope.launch {
            val language = userPreferences.appLanguage.first()
            loadTodayQuote(language)
        }
    }

    /**
     * Toggle favorito con lógica de interstitial y límite para free users
     */
    fun toggleFavorite() {
        val currentQuote = _uiState.value.todayQuote ?: return

        viewModelScope.launch {
            try {
                // Si va a AÑADIR a favoritos (no quitar), verificar límite
                if (!currentQuote.isFavorite && !adMobManager.isPremium()) {
                    val currentCount = quoteRepository.getFavoriteCount()
                    if (currentCount >= FREE_FAVORITES_LIMIT) {
                        Log.d(TAG, "Favorites limit reached: $currentCount/$FREE_FAVORITES_LIMIT")
                        _favoritesLimitReached.emit(FREE_FAVORITES_LIMIT)
                        return@launch
                    }
                }

                val updatedQuote = currentQuote.copy(isFavorite = !currentQuote.isFavorite)
                quoteRepository.updateQuote(updatedQuote)

                _uiState.value = _uiState.value.copy(todayQuote = updatedQuote)

                // Incrementar interacciones para AdMob
                incrementUserInteraction()

            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite", e)
            }
        }
    }

    /**
     * Guardar favorito tras completar rewarded ad (sin verificar límite).
     * Se llama solo después de que el usuario haya visto el anuncio completo.
     */
    fun forceSaveFavorite() {
        val currentQuote = _uiState.value.todayQuote ?: return

        viewModelScope.launch {
            try {
                if (!currentQuote.isFavorite) {
                    val updatedQuote = currentQuote.copy(isFavorite = true)
                    quoteRepository.updateQuote(updatedQuote)
                    _uiState.value = _uiState.value.copy(todayQuote = updatedQuote)
                    Log.d(TAG, "Favorite saved via rewarded ad")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving favorite after reward", e)
            }
        }
    }

    /**
     * Cargar una cita aleatoria diferente a la actual (tras rewarded ad)
     */
    fun loadRandomQuote() {
        viewModelScope.launch {
            try {
                val language = userPreferences.appLanguage.first()
                val currentQuoteId = _uiState.value.todayQuote?.id ?: -1
                val excludeIds = if (currentQuoteId > 0) listOf(currentQuoteId) else emptyList()
                val randomQuote = quoteRepository.getRandomQuoteExcluding(excludeIds, language)
                if (randomQuote != null) {
                    _uiState.value = _uiState.value.copy(todayQuote = randomQuote)
                    Log.d(TAG, "Random quote loaded via rewarded ad")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading random quote", e)
            }
        }
    }

    /**
     * Verificar si hay un rewarded ad disponible
     */
    fun hasRewardedAd(): Boolean = adMobManager.hasRewardedAd()

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
     * Verificar si el usuario es Premium
     */
    fun isPremium(): Boolean = adMobManager.isPremium()

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
                val language = userPreferences.appLanguage.first()
                val quote = quoteRepository.getOrCreateTodayQuote(language)
                
                _uiState.value = _uiState.value.copy(
                    todayQuote = quote,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "error_generating_quotes" // Clave para que la UI use stringResource()
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
