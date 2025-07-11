package com.albertowisdom.wisdomspark.presentation.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de categorías
 */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository,
    private val adMobManager: AdMobManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    private var categoryClicks = 0

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Obtener todas las categorías disponibles
                val allCategories = quoteRepository.getAllCategories()
                
                // Crear lista de categorías con sus counts reales
                val categoriesWithCounts = allCategories.map { categoryName ->
                    // Obtener el count actual para esta categoría
                    val quotes = quoteRepository.getQuotesByCategory(categoryName).first()
                    CategoryItem(categoryName, quotes.size)
                }
                
                _uiState.value = _uiState.value.copy(
                    categories = categoriesWithCounts,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun onCategoryClicked() {
        categoryClicks++
        // Mostrar interstitial cada 3 categorías clickeadas
        if (categoryClicks % 3 == 0) {
            // El AdMobManager maneja cuándo mostrar el ad
        }
    }
}

/**
 * UI State para la pantalla de categorías
 */
data class CategoriesUiState(
    val categories: List<CategoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
