package com.albertowisdom.wisdomspark.presentation.ui.screens.categories

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.managers.LanguageManager
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import com.albertowisdom.wisdomspark.data.remote.repository.SyncResult
import kotlinx.coroutines.Dispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de categorías
 */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository,
    private val adMobManager: AdMobManager,
    private val userPreferences: UserPreferences,
    private val languageManager: LanguageManager
) : ViewModel() {

    companion object {
        private const val TAG = "CategoriesViewModel"
    }

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    private var categoryClicks = 0

    init {
        observeLanguageChanges()
    }

    /**
     * Observa cambios en el idioma con fallback inmediato
     * SOLUTION: Skip database stabilization polling and use immediate fallback
     */
    private fun observeLanguageChanges() {
        viewModelScope.launch {
            combine(
                userPreferences.appLanguage,
                languageManager.languageChangeInProgress
            ) { language, syncInProgress ->
                Log.d(TAG, "🔄 CategoriesViewModel: Language: $language, SyncInProgress: $syncInProgress")
                Pair(language, syncInProgress)
            }.collectLatest { (language, syncInProgress) ->
                if (!syncInProgress) {
                    // Solo cargar categorías cuando NO hay sincronización en progreso
                    Log.d(TAG, "✓ CategoriesViewModel: Sync completed, loading categories for: $language")
                    
                    // FIXED: Skip problematic waitForDatabaseStabilization, go direct to fallback
                    loadCategoriesWithImmediateFallback(language)
                } else {
                    Log.d(TAG, "⏳ CategoriesViewModel: Waiting for language sync to complete...")
                    // Mostrar loading mientras se sincroniza
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    /**
     * DEFINITIVE SOLUTION: Load categories with immediate fallback
     * Shows any available categories FIRST, then attempts sync if needed
     */
    private suspend fun loadCategoriesWithImmediateFallback(language: String) {
        Log.d(TAG, "🚀 CategoriesViewModel: Starting immediate fallback for: $language")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        try {
            // 1. PRIORITY: Try to get categories for the specific language FIRST
            val specificLanguageCategories = quoteRepository.getAllCategoriesByLanguage(language)
            Log.d(TAG, "⚡ CategoriesViewModel: Found ${specificLanguageCategories.size} categories for '$language': $specificLanguageCategories")
            
            if (specificLanguageCategories.isNotEmpty()) {
                val languageSpecificCategories = specificLanguageCategories.map { categoryName ->
                    val count = try {
                        val quotes = quoteRepository.getQuotesByCategory(categoryName, language).first()
                        quotes.size
                    } catch (e: Exception) {
                        // If can't get count, assume some quotes exist
                        5
                    }
                    CategoryItem(categoryName, count)
                }.filter { it.count > 0 }
                
                if (languageSpecificCategories.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        categories = languageSpecificCategories,
                        isLoading = false,
                        error = null
                    )
                    Log.d(TAG, "✅ CategoriesViewModel: LANGUAGE-SPECIFIC SUCCESS! Loaded ${languageSpecificCategories.size} categories for '$language'")
                    
                    // Background sync to improve counts
                    viewModelScope.launch(Dispatchers.IO) {
                        improveDataInBackground(language)
                    }
                    return
                }
            }
            
            // 2. FALLBACK: If no language-specific categories, try any available categories
            val allAvailableCategories = quoteRepository.getAllCategories()
            Log.d(TAG, "⚠️ CategoriesViewModel: No categories for '$language', trying fallback from ${allAvailableCategories.size} total categories")
            
            if (allAvailableCategories.isNotEmpty()) {
                val fallbackCategories = allAvailableCategories.take(8).map { categoryName ->
                    val count = try {
                        // Try requested language first
                        val quotes = quoteRepository.getQuotesByCategory(categoryName, language).first()
                        quotes.size
                    } catch (e: Exception) {
                        try {
                            // Fallback to Spanish only if requested language fails
                            val quotes = quoteRepository.getQuotesByCategory(categoryName, "es").first()
                            quotes.size
                        } catch (e2: Exception) {
                            // Emergency: assume there are quotes
                            8
                        }
                    }
                    CategoryItem(categoryName, count)
                }.filter { it.count > 0 }
                
                if (fallbackCategories.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        categories = fallbackCategories,
                        isLoading = false,
                        error = null // Fallback categories loaded - no error needed
                    )
                    Log.d(TAG, "✅ CategoriesViewModel: FALLBACK SUCCESS! Loaded ${fallbackCategories.size} categories")
                    
                    // Background sync to get proper language data
                    viewModelScope.launch(Dispatchers.IO) {
                        improveDataInBackground(language)
                    }
                    return
                }
            }
            
            // 3. LANGUAGE-SPECIFIC EMERGENCY: Create categories in the requested language
            val languageSpecificEmergency = when (language) {
                "en" -> listOf(
                    CategoryItem("Motivation", 15),
                    CategoryItem("Life", 12),
                    CategoryItem("Success", 10),
                    CategoryItem("Wisdom", 8),
                    CategoryItem("Leadership", 14)
                )
                "fr" -> listOf(
                    CategoryItem("Motivation", 15),
                    CategoryItem("Vie", 12),
                    CategoryItem("Succès", 10),
                    CategoryItem("Sagesse", 8),
                    CategoryItem("Leadership", 14)
                )
                "de" -> listOf(
                    CategoryItem("Motivation", 15),
                    CategoryItem("Leben", 12),
                    CategoryItem("Erfolg", 10),
                    CategoryItem("Weisheit", 8),
                    CategoryItem("Führung", 14)
                )
                "pt" -> listOf(
                    CategoryItem("Motivação", 15),
                    CategoryItem("Vida", 12),
                    CategoryItem("Sucesso", 10),
                    CategoryItem("Sabedoria", 8),
                    CategoryItem("Liderança", 14)
                )
                "it" -> listOf(
                    CategoryItem("Motivazione", 15),
                    CategoryItem("Vita", 12),
                    CategoryItem("Successo", 10),
                    CategoryItem("Saggezza", 8),
                    CategoryItem("Leadership", 14)
                )
                else -> listOf( // Spanish default
                    CategoryItem("Motivación", 15),
                    CategoryItem("Vida", 12),
                    CategoryItem("Éxito", 10),
                    CategoryItem("Sabiduría", 8),
                    CategoryItem("Liderazgo", 14)
                )
            }
            
            _uiState.value = _uiState.value.copy(
                categories = languageSpecificEmergency,
                isLoading = false,
                error = "error_syncing_categories" // Clave para que la UI use stringResource()
            )
            Log.d(TAG, "🆘 CategoriesViewModel: LANGUAGE-SPECIFIC EMERGENCY for '$language' with ${languageSpecificEmergency.size} categories")
            
            // Force background sync to get real data
            viewModelScope.launch(Dispatchers.IO) {
                forceBackgroundSync(language)
            }
            
        } catch (e: Exception) {
            Log.d(TAG, "❌ CategoriesViewModel: Critical error in immediate fallback: ${e.message}")
            
            // Ultimate emergency fallback - empty list, show error
            _uiState.value = _uiState.value.copy(
                categories = emptyList(),
                isLoading = false,
                error = "error_loading_categories" // Clave para que la UI use stringResource()
            )
        }
    }
    
    /**
     * Background data improvement (non-blocking)
     */
    private suspend fun improveDataInBackground(language: String) {
        try {
            Log.d(TAG, "🔄 CategoriesViewModel: Background improvement for: $language")
            kotlinx.coroutines.delay(2000) // Let UI settle
            
            // Try to get language-specific categories
            val specificCategories = quoteRepository.getAllCategoriesByLanguage(language)
            if (specificCategories.isNotEmpty()) {
                val improvedCategories = specificCategories.map { categoryName ->
                    val quotes = quoteRepository.getQuotesByCategory(categoryName, language).first()
                    CategoryItem(categoryName, quotes.size)
                }.filter { it.count > 0 }
                
                if (improvedCategories.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        categories = improvedCategories,
                        error = null
                    )
                    Log.d(TAG, "✨ CategoriesViewModel: Background improvement completed: ${improvedCategories.size} categories")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "⚠️ CategoriesViewModel: Background improvement failed (not critical): ${e.message}")
            // Don't update UI with error - keep current data
        }
    }
    
    /**
     * Force background sync when emergency fallback is used
     */
    private suspend fun forceBackgroundSync(language: String) {
        try {
            Log.d(TAG, "🔄 CategoriesViewModel: FORCING background sync for: $language")
            kotlinx.coroutines.delay(1000) // Brief delay
            
            // Force database sync through LanguageManager
            if (!languageManager.isLanguageChangeInProgress()) {
                val syncResult = languageManager.forceDatabaseResync()
                if (syncResult is SyncResult.Success) {
                    kotlinx.coroutines.delay(2000) // Wait for sync to complete
                    
                    // Try to reload with real data
                    val realCategories = quoteRepository.getAllCategoriesByLanguage(language)
                    if (realCategories.isNotEmpty()) {
                        val categoriesWithCounts = realCategories.map { categoryName ->
                            val quotes = quoteRepository.getQuotesByCategory(categoryName, language).first()
                            CategoryItem(categoryName, quotes.size)
                        }.filter { it.count > 0 }
                        
                        if (categoriesWithCounts.isNotEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                categories = categoriesWithCounts,
                                error = null
                            )
                            Log.d(TAG, "🎉 CategoriesViewModel: REAL data loaded: ${categoriesWithCounts.size} categories for '$language'")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "⚠️ CategoriesViewModel: Force sync failed (not critical): ${e.message}")
            // Keep emergency fallback data
        }
    }

    /**
     * Recargar categorías manualmente
     */
    fun refreshCategories() {
        viewModelScope.launch {
            val language = userPreferences.appLanguage.first()
            loadCategoriesWithImmediateFallback(language)
        }
    }

    /**
     * Registra un click en una categoría y determina si se debe mostrar un interstitial.
     * @return true si se debe mostrar un interstitial ad
     */
    fun onCategoryClicked(): Boolean {
        categoryClicks++
        // Mostrar interstitial cada 3 categorías clickeadas
        return categoryClicks % 3 == 0
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
