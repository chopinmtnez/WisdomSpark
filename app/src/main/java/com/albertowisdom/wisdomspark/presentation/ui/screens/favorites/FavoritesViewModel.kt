package com.albertowisdom.wisdomspark.presentation.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de favoritos.
 * Usa combine + stateIn para evitar leaks de múltiples collectors.
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository,
    private val adMobManager: AdMobManager
) : ViewModel() {

    // Controles internos de búsqueda y orden
    private val _searchQuery = MutableStateFlow("")
    private val _sortType = MutableStateFlow(FavoritesSortType.DATE_ADDED)
    private val _error = MutableStateFlow<String?>(null)

    /**
     * Estado de UI derivado reactivamente del Flow de Room + filtros.
     * Un SOLO collector, sin riesgo de leaks.
     */
    val uiState: StateFlow<FavoritesUiState> = combine(
        quoteRepository.getFavoriteQuotes(),
        _searchQuery,
        _sortType,
        _error
    ) { allFavorites, query, sort, error ->
        val filtered = if (query.isBlank()) {
            allFavorites
        } else {
            allFavorites.filter { quote ->
                quote.text.contains(query, ignoreCase = true) ||
                quote.author.contains(query, ignoreCase = true) ||
                quote.category.contains(query, ignoreCase = true)
            }
        }

        val sorted = when (sort) {
            FavoritesSortType.DATE_ADDED -> filtered.sortedByDescending { it.id }
            FavoritesSortType.AUTHOR -> filtered.sortedBy { it.author }
            FavoritesSortType.CATEGORY -> filtered.sortedBy { it.category }
            FavoritesSortType.LENGTH -> filtered.sortedBy { it.text.length }
        }

        FavoritesUiState(
            favoriteQuotes = sorted,
            isLoading = false,
            error = error,
            searchQuery = query,
            sortType = sort
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState(isLoading = true)
    )

    /**
     * Toggle favorito de una cita
     */
    fun toggleFavorite(quote: Quote) {
        viewModelScope.launch {
            try {
                quoteRepository.toggleFavorite(quote) // Usa el método consistente del repo
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Buscar en favoritos (reactivo - actualiza el Flow)
     */
    fun searchFavorites(query: String) {
        _searchQuery.value = query
    }

    /**
     * Ordenar favoritos (reactivo - actualiza el Flow)
     */
    fun sortFavorites(sortType: FavoritesSortType) {
        _sortType.value = sortType
    }

    /**
     * Refrescar favoritos: con combine+stateIn Room actualiza automáticamente,
     * así que solo limpiamos errores pendientes.
     */
    fun refreshFavorites() {
        _error.value = null
    }

    /**
     * Limpiar error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Obtener estadísticas de favoritos
     */
    fun getFavoritesStats(): FavoritesStats {
        val favorites = uiState.value.favoriteQuotes
        val categoryCounts = favorites.groupingBy { it.category }.eachCount()
        val mostPopularCategory = categoryCounts.maxByOrNull { it.value }?.key

        return FavoritesStats(
            totalFavorites = favorites.size,
            categoriesCount = categoryCounts.size,
            mostPopularCategory = mostPopularCategory ?: "-",
            averageQuoteLength = if (favorites.isNotEmpty()) {
                favorites.sumOf { it.text.length } / favorites.size
            } else 0
        )
    }

    /**
     * Exportar favoritos como texto
     */
    fun exportFavoritesToText(): String {
        val favorites = uiState.value.favoriteQuotes
        if (favorites.isEmpty()) return ""

        val builder = StringBuilder()
        builder.appendLine("My Favorite Quotes - WisdomSpark")
        builder.appendLine("=".repeat(50))
        builder.appendLine()

        favorites.forEachIndexed { index, quote ->
            builder.appendLine("${index + 1}. \"${quote.text}\"")
            builder.appendLine("   — ${quote.author}")
            builder.appendLine("   ${quote.category}")
            builder.appendLine()
        }

        builder.appendLine("=".repeat(50))
        builder.appendLine("Exported from WisdomSpark")
        builder.appendLine("${favorites.size} inspirational quotes")

        return builder.toString()
    }

    /**
     * shouldShowAds delegado al AdMobManager
     */
    fun shouldShowAds(): Boolean = adMobManager.shouldShowAds()
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
