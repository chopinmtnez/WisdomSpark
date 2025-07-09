package com.albertowisdom.wisdomspark.presentation.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
                // Obtener todas las categorías con sus counts
                val categories = listOf(
                    CategoryItem("Motivación", 15),
                    CategoryItem("Vida", 12),
                    CategoryItem("Sueños", 8),
                    CategoryItem("Perseverancia", 10),
                    CategoryItem("Educación", 6),
                    CategoryItem("Creatividad", 7),
                    CategoryItem("Éxito", 9),
                    CategoryItem("Autenticidad", 5),
                    CategoryItem("Felicidad", 11),
                    CategoryItem("Sabiduría", 13),
                    CategoryItem("Confianza", 8),
                    CategoryItem("Progreso", 6),
                    CategoryItem("Excelencia", 7),
                    CategoryItem("Acción", 9)
                )
                
                _uiState.value = _uiState.value.copy(
                    categories = categories,
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
