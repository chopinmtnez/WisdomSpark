package com.albertowisdom.wisdomspark.presentation.ui.screens.categories

import android.util.Log
import androidx.lifecycle.ViewModel
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.managers.LanguageManager
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository,
    private val adMobManager: AdMobManager,
    private val userPreferences: UserPreferences,
    private val languageManager: LanguageManager
) : ViewModel() {

    companion object {
        private const val TAG = "CategoryDetailViewModel"
        private const val FREE_FAVORITES_LIMIT = 10
    }

    // Evento one-shot: el usuario alcanzó el límite de favoritos
    private val _favoritesLimitReached = MutableSharedFlow<Int>()
    val favoritesLimitReached: SharedFlow<Int> = _favoritesLimitReached.asSharedFlow()

    /**
     * Obtener citas de una categoría de manera coordinada con el LanguageManager
     * Ahora espera a que termine la sincronización antes de consultar la base de datos
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun getQuotesByCategory(categoryName: String): Flow<List<Quote>> =
        combine(
            userPreferences.appLanguage,
            languageManager.languageChangeInProgress
        ) { language, syncInProgress ->
            Log.d(TAG, "Category '$categoryName', Language: '$language', SyncInProgress: $syncInProgress")
            Triple(categoryName, language, syncInProgress)
        }.flatMapLatest { (category, language, syncInProgress) ->
            if (syncInProgress) {
                // Durante la sincronización, devolver lista vacía
                Log.d(TAG, "Sync in progress, returning empty list for '$category'")
                flowOf(emptyList())
            } else {
                // Una vez terminada la sincronización, consultar la base de datos
                flow {
                    Log.d(TAG, "Querying database for category '$category' in language '$language'")

                    // Obtener las citas desde el repositorio
                    val quotesFlow = quoteRepository.getQuotesByCategory(category, language)
                    quotesFlow.collect { quotes ->
                        Log.d(TAG, "Found ${quotes.size} quotes for '$category' in '$language'")
                        emit(quotes)
                    }
                }
            }
        }

    /**
     * Obtener el Flow de citas favoritas
     */
    fun getFavoriteQuotesFlow(): Flow<List<Quote>> = quoteRepository.getFavoriteQuotes()

    /**
     * Alternar el estado de favorito de una cita, respetando el límite para free users.
     * Retorna true si se aplicó el cambio, false si se bloqueó por límite.
     */
    suspend fun toggleFavoriteQuote(quote: Quote): Boolean {
        // Si va a AÑADIR a favoritos y no es premium, verificar límite
        if (!quote.isFavorite && !adMobManager.isPremium()) {
            val currentCount = quoteRepository.getFavoriteCount()
            if (currentCount >= FREE_FAVORITES_LIMIT) {
                Log.d(TAG, "Favorites limit reached: $currentCount/$FREE_FAVORITES_LIMIT")
                _favoritesLimitReached.emit(FREE_FAVORITES_LIMIT)
                return false
            }
        }
        quoteRepository.toggleFavorite(quote)
        return true
    }

    /**
     * Guardar favorito tras completar rewarded ad (sin verificar límite).
     */
    suspend fun forceSaveFavoriteQuote(quote: Quote) {
        if (!quote.isFavorite) {
            quoteRepository.toggleFavorite(quote)
            Log.d(TAG, "Favorite saved via rewarded ad for quote: ${quote.id}")
        }
    }

    /**
     * Verificar si hay un rewarded ad disponible
     */
    fun hasRewardedAd(): Boolean = adMobManager.hasRewardedAd()
}