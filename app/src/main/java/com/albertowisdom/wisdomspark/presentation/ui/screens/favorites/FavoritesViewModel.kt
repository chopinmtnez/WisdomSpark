package com.albertowisdom.wisdomspark.presentation.ui.screens.favorites

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
 * ViewModel para la pantalla de favoritos
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository,
    private val adMobManager: AdMobManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private var shareCount = 0

    init {
        loadFavorites()
    }

    /**
     * Cargar citas favoritas
     */
    private fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                quoteRepository.getFavoriteQuotes().collect { quotes ->
                    _uiState.value = _uiState.value.copy(
                        favoriteQuotes = quotes,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Refrescar favoritos manualmente
     */
    fun refreshFavorites() {
        loadFavorites()
    }

    /**
     * Toggle favorito de una cita
     */
    fun toggleFavorite(quote: Quote) {
        viewModelScope.launch {
            try {
                val updatedQuote = quote.copy(isFavorite = !quote.isFavorite)
                quoteRepository.updateQuote(updatedQuote)
                
                // Si se desmarca como favorito, remover de la lista actual
                if (!updatedQuote.isFavorite) {
                    val currentFavorites = _uiState.value.favoriteQuotes.toMutableList()
                    currentFavorites.removeAll { it.id == quote.id }
                    _uiState.value = _uiState.value.copy(favoriteQuotes = currentFavorites)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Manejar cuando se comparte una cita
     */
    fun onQuoteShared() {
        shareCount++
        // Mostrar interstitial cada 3 shares
        if (shareCount % 3 == 0) {
            // El AdMobManager maneja la lógica de mostrar ads
        }
    }

    /**
     * Obtener estadísticas de favoritos
     */
    fun getFavoritesStats(): FavoritesStats {
        val favorites = _uiState.value.favoriteQuotes
        val categoryCounts = favorites.groupingBy { it.category }.eachCount()
        val mostPopularCategory = categoryCounts.maxByOrNull { it.value }?.key
        
        return FavoritesStats(
            totalFavorites = favorites.size,
            categoriesCount = categoryCounts.size,
            mostPopularCategory = mostPopularCategory ?: "Sin categoría",
            averageQuoteLength = if (favorites.isNotEmpty()) {
                favorites.sumOf { it.text.length } / favorites.size
            } else 0
        )
    }

    /**
     * Buscar en favoritos
     */
    fun searchFavorites(query: String) {
        if (query.isBlank()) {
            loadFavorites()
            return
        }
        
        val allFavorites = _uiState.value.favoriteQuotes
        val filteredFavorites = allFavorites.filter { quote ->
            quote.text.contains(query, ignoreCase = true) ||
            quote.author.contains(query, ignoreCase = true) ||
            quote.category.contains(query, ignoreCase = true)
        }
        
        _uiState.value = _uiState.value.copy(favoriteQuotes = filteredFavorites)
    }

    /**
     * Ordenar favoritos
     */
    fun sortFavorites(sortType: FavoritesSortType) {
        val currentFavorites = _uiState.value.favoriteQuotes.toMutableList()
        
        when (sortType) {
            FavoritesSortType.DATE_ADDED -> {
                // Ordenar por ID (asumiendo que ID mayor = más reciente)
                currentFavorites.sortByDescending { it.id }
            }
            FavoritesSortType.AUTHOR -> {
                currentFavorites.sortBy { it.author }
            }
            FavoritesSortType.CATEGORY -> {
                currentFavorites.sortBy { it.category }
            }
            FavoritesSortType.LENGTH -> {
                currentFavorites.sortBy { it.text.length }
            }
        }
        
        _uiState.value = _uiState.value.copy(favoriteQuotes = currentFavorites)
    }

    /**
     * Exportar favoritos como texto
     */
    fun exportFavoritesToText(): String {
        val favorites = _uiState.value.favoriteQuotes
        if (favorites.isEmpty()) return "No tienes citas favoritas para exportar."
        
        val builder = StringBuilder()
        builder.appendLine("🌟 Mis Citas Favoritas - WisdomSpark")
        builder.appendLine("=" * 50)
        builder.appendLine()
        
        favorites.forEachIndexed { index, quote ->
            builder.appendLine("${index + 1}. \"${quote.text}\"")
            builder.appendLine("   — ${quote.author}")
            builder.appendLine("   📂 ${quote.category}")
            builder.appendLine()
        }
        
        builder.appendLine("=" * 50)
        builder.appendLine("📱 Exportado desde WisdomSpark")
        builder.appendLine("💫 ${favorites.size} citas inspiracionales")
        
        return builder.toString()
    }
}

/**
 * UI State para la pantalla de favoritos
 */
data class FavoritesUiState(
    val favoriteQuotes: List<Quote> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val sortType: FavoritesSortType = FavoritesSortType.DATE_ADDED
)

/**
 * Estadísticas de favoritos
 */
data class FavoritesStats(
    val totalFavorites: Int,
    val categoriesCount: Int,
    val mostPopularCategory: String,
    val averageQuoteLength: Int
)

/**
 * Tipos de ordenamiento para favoritos
 */
enum class FavoritesSortType {
    DATE_ADDED,
    AUTHOR,
    CATEGORY,
    LENGTH
}

/**
 * Extensión para repetir strings (como Python)
 */
private operator fun String.times(n: Int): String = this.repeat(n)
