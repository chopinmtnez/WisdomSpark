package com.albertowisdom.wisdomspark.presentation.ui.screens.categories

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

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    private var categoryClicks = 0

    init {
        observeLanguageChanges()
    }

    /**
     * Observa cambios en el idioma y espera a que el LanguageManager complete la sincronización
     * ENHANCED: Includes defensive validation and retry logic
     */
    private fun observeLanguageChanges() {
        viewModelScope.launch {
            combine(
                userPreferences.appLanguage,
                languageManager.languageChangeInProgress
            ) { language, syncInProgress ->
                println("🔄 CategoriesViewModel: Language: $language, SyncInProgress: $syncInProgress")
                Pair(language, syncInProgress)
            }.collectLatest { (language, syncInProgress) ->
                if (!syncInProgress) {
                    // Solo cargar categorías cuando NO hay sincronización en progreso
                    println("✓ CategoriesViewModel: Sync completed, loading categories for: $language")
                    
                    // Enhanced delay with validation - wait for database stabilization
                    waitForDatabaseStabilization(language)
                    loadCategoriesWithValidation(language)
                } else {
                    println("⏳ CategoriesViewModel: Waiting for language sync to complete...")
                    // Mostrar loading mientras se sincroniza
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    /**
     * Wait for database stabilization with aggressive timeout
     * OPTIMIZED: Minimal waiting to prevent infinite loops
     */
    private suspend fun waitForDatabaseStabilization(language: String) {
        // If background sync is running, wait briefly with timeout
        if (languageManager.isLanguageChangeInProgress()) {
            println("⏳ CategoriesViewModel: Background sync in progress, brief wait...")
            
            // Wait maximum 5 seconds for background sync
            var timeoutCounter = 0
            while (languageManager.isLanguageChangeInProgress() && timeoutCounter < 10) {
                kotlinx.coroutines.delay(500)
                timeoutCounter++
            }
            
            if (timeoutCounter >= 10) {
                println("⚠️ CategoriesViewModel: Background sync timeout, proceeding anyway")
            } else {
                println("✓ CategoriesViewModel: Background sync completed")
                kotlinx.coroutines.delay(500) // Minimal stabilization delay
            }
        }
        
        // Quick poll for database stability (reduced from 8 to 4 attempts)
        repeat(4) { attempt ->
            val categories = quoteRepository.getAllCategoriesByLanguage(language)
            if (categories.isNotEmpty()) {
                println("✅ CategoriesViewModel: Database stable for '$language' after ${attempt * 200}ms")
                return
            }
            println("⌛ CategoriesViewModel: Polling database stability... attempt ${attempt + 1}/4")
            kotlinx.coroutines.delay(200) // Even faster polling: 200ms
        }
        
        println("⚠️ CategoriesViewModel: Database stabilization timeout for '$language' - proceeding with recovery")
    }

    /**
     * Enhanced categories loading with immediate fallback to local data
     * FIXED: Execute fallback BEFORE checking empty database to avoid infinite loops
     */
    private suspend fun loadCategoriesWithValidation(language: String) {
        println("🔄 CategoriesViewModel: Loading categories with validation for: $language")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        try {
            // 1. IMMEDIATE FALLBACK: Try to show any available categories FIRST
            // This prevents infinite loops when database is being synced
            val availableCategories = quoteRepository.getAllCategories()
            if (availableCategories.isNotEmpty()) {
                println("⚡ CategoriesViewModel: Showing immediate fallback categories")
                
                val fallbackCategoriesWithCounts = mutableListOf<CategoryItem>()
                
                for (categoryName in availableCategories) {
                    try {
                        // Try to get quotes for the requested language first
                        val quotesInLanguage = quoteRepository.getQuotesByCategory(categoryName, language).first()
                        if (quotesInLanguage.isNotEmpty()) {
                            fallbackCategoriesWithCounts.add(CategoryItem(categoryName, quotesInLanguage.size))
                            continue
                        }
                    } catch (e: Exception) {
                        // Ignore and try fallback language
                    }
                    
                    try {
                        // Fallback to Spanish if requested language has no quotes
                        val quotesInSpanish = quoteRepository.getQuotesByCategory(categoryName, "es").first()
                        if (quotesInSpanish.isNotEmpty()) {
                            fallbackCategoriesWithCounts.add(CategoryItem(categoryName, quotesInSpanish.size))
                        }
                    } catch (e: Exception) {
                        // Ignore categories with no quotes
                        println("⚠️ Category '$categoryName' has no quotes in any language")
                    }
                }
                
                if (fallbackCategoriesWithCounts.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        categories = fallbackCategoriesWithCounts,
                        isLoading = false,
                        error = null
                    )
                    println("✅ CategoriesViewModel: Immediate fallback loaded: ${fallbackCategoriesWithCounts.size} categories")
                    
                    // Continue to sync in background for proper language data
                    viewModelScope.launch(Dispatchers.IO) {
                        syncCategoriesInBackground(language)
                    }
                    return
                }
            }
            
            // 2. If no fallback available, check database state
            val totalQuotes = quoteRepository.getQuotesCount()
            if (totalQuotes == 0) {
                println("⚠️ CategoriesViewModel: No quotes in database, attempting recovery...")
                handleEmptyDatabase()
                return
            }
            
            // 3. Get categories for the specified language
            val allCategories = quoteRepository.getAllCategoriesByLanguage(language)
            println("🔄 CategoriesViewModel: Found ${allCategories.size} categories for '$language': $allCategories")
            
            // 4. Handle empty categories case
            if (allCategories.isEmpty()) {
                println("⚠️ CategoriesViewModel: No categories for '$language', trying fallback...")
                handleEmptyCategories(language)
                return
            }
            
            // 5. Create categories with counts
            val categoriesWithCounts = allCategories.map { categoryName ->
                val quotes = quoteRepository.getQuotesByCategory(categoryName, language).first()
                println("📊 CategoriesViewModel: '$categoryName' has ${quotes.size} quotes in '$language'")
                CategoryItem(categoryName, quotes.size)
            }.filter { it.count > 0 } // Only show categories with quotes
            
            // 6. Final validation
            if (categoriesWithCounts.isEmpty()) {
                println("⚠️ CategoriesViewModel: All categories have 0 quotes, showing fallback")
                handleEmptyCategories(language)
                return
            }
            
            // 7. Success - update UI
            _uiState.value = _uiState.value.copy(
                categories = categoriesWithCounts,
                isLoading = false,
                error = null
            )
            println("✅ CategoriesViewModel: Successfully loaded ${categoriesWithCounts.size} categories")
            
        } catch (e: Exception) {
            println("❌ CategoriesViewModel: Error loading categories: ${e.message}")
            
            // Last resort fallback: show hardcoded categories to prevent infinite loading
            println("🚨 CategoriesViewModel: Showing emergency fallback categories")
            val emergencyCategories = listOf(
                CategoryItem("Motivacional", 0),
                CategoryItem("Filosófica", 0),
                CategoryItem("Éxito", 0),
                CategoryItem("Amor", 0),
                CategoryItem("Sabiduría", 0)
            )
            
            _uiState.value = _uiState.value.copy(
                categories = emergencyCategories,
                isLoading = false,
                error = "Mostrando categorías de respaldo. Por favor, reinicia la app."
            )
        }
    }
    
    /**
     * Background sync for categories after showing immediate fallback
     */
    private suspend fun syncCategoriesInBackground(language: String) {
        try {
            println("🔄 CategoriesViewModel: Background sync started for: $language")
            
            // Wait a bit to let UI settle
            kotlinx.coroutines.delay(1000)
            
            // Check if background sync is needed
            if (languageManager.isLanguageChangeInProgress()) {
                println("⏳ CategoriesViewModel: Background language sync in progress, waiting...")
                languageManager.waitForLanguageChangeCompletion()
                kotlinx.coroutines.delay(2000)
            }
            
            // Try to get proper language-specific categories
            val properCategories = quoteRepository.getAllCategoriesByLanguage(language)
            if (properCategories.isNotEmpty()) {
                val categoriesWithCounts = properCategories.map { categoryName ->
                    val quotes = quoteRepository.getQuotesByCategory(categoryName, language).first()
                    CategoryItem(categoryName, quotes.size)
                }.filter { it.count > 0 }
                
                if (categoriesWithCounts.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        categories = categoriesWithCounts,
                        error = null
                    )
                    println("✅ CategoriesViewModel: Background sync completed: ${categoriesWithCounts.size} categories for '$language'")
                } else {
                    println("⚠️ CategoriesViewModel: Background sync found no valid categories")
                }
            } else {
                println("⚠️ CategoriesViewModel: Background sync found no categories for: $language")
            }
            
        } catch (e: Exception) {
            println("❌ CategoriesViewModel: Background sync failed: ${e.message}")
            // Don't update UI with error - keep showing fallback data
        }
    }
    
    /**
     * Handle case when database is completely empty
     * FIXED: Prevent infinite loops with timeout and emergency fallback
     */
    private suspend fun handleEmptyDatabase() {
        println("🔧 CategoriesViewModel: Handling empty database...")
        
        // Check if there's already a background sync in progress with timeout
        if (languageManager.isLanguageChangeInProgress()) {
            println("⏳ CategoriesViewModel: Background sync in progress, waiting with timeout...")
            
            // Wait with timeout to prevent infinite loops
            var timeoutCounter = 0
            while (languageManager.isLanguageChangeInProgress() && timeoutCounter < 20) {
                kotlinx.coroutines.delay(500)
                timeoutCounter++
                println("⏰ Waiting for sync completion... ${timeoutCounter}/20")
            }
            
            if (timeoutCounter >= 20) {
                println("⚠️ CategoriesViewModel: Sync timeout reached, showing emergency fallback")
                showEmergencyFallback()
                return
            }
            
            // Give minimal time for stabilization
            kotlinx.coroutines.delay(1000)
            
            // Check if we now have categories available
            val availableCategories = quoteRepository.getAllCategories()
            if (availableCategories.isNotEmpty()) {
                println("✅ Categories available after sync, retrying...")
                val currentLanguage = userPreferences.appLanguage.first()
                loadCategoriesWithValidation(currentLanguage)
                return
            }
        }
        
        // Only start recovery if no background sync is running
        try {
            println("🔧 CategoriesViewModel: Starting database recovery...")
            val syncResult = languageManager.forceDatabaseResync()
            if (syncResult is SyncResult.Success) {
                println("✅ Database recovery successful")
                kotlinx.coroutines.delay(1000)
                
                // Check if recovery actually worked
                val categoriesAfterRecovery = quoteRepository.getAllCategories()
                if (categoriesAfterRecovery.isNotEmpty()) {
                    val currentLanguage = userPreferences.appLanguage.first()
                    loadCategoriesWithValidation(currentLanguage)
                } else {
                    println("⚠️ Recovery completed but no categories found, showing emergency fallback")
                    showEmergencyFallback()
                }
            } else {
                println("❌ Database recovery failed, showing emergency fallback")
                showEmergencyFallback()
            }
        } catch (e: Exception) {
            println("❌ Error during database recovery: ${e.message}")
            showEmergencyFallback()
        }
    }
    
    /**
     * Show emergency fallback categories to prevent infinite loading
     */
    private fun showEmergencyFallback() {
        println("🚨 CategoriesViewModel: Showing emergency fallback categories")
        val emergencyCategories = listOf(
            CategoryItem("Motivacional", 0),
            CategoryItem("Filosófica", 0),
            CategoryItem("Éxito", 0),
            CategoryItem("Amor", 0),
            CategoryItem("Sabiduría", 0)
        )
        
        _uiState.value = _uiState.value.copy(
            categories = emergencyCategories,
            isLoading = false,
            error = "Datos temporalmente no disponibles. Por favor, reinicia la app."
        )
    }
    
    /**
     * Handle case when no categories found for language
     */
    private suspend fun handleEmptyCategories(language: String) {
        println("🔧 CategoriesViewModel: Handling empty categories for '$language'")
        try {
            // Fallback: try to get any available categories
            val fallbackCategories = quoteRepository.getAllCategories()
            if (fallbackCategories.isNotEmpty()) {
                println("📝 Using fallback categories: $fallbackCategories")
                val categoriesWithCounts = fallbackCategories.map { categoryName ->
                    val quotes = quoteRepository.getQuotesByCategory(categoryName, "es").first() // Default to Spanish
                    CategoryItem(categoryName, quotes.size)
                }.filter { it.count > 0 }
                
                _uiState.value = _uiState.value.copy(
                    categories = categoriesWithCounts,
                    isLoading = false,
                    error = "Mostrando categorías disponibles (idioma: $language no disponible)"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    categories = emptyList(),
                    isLoading = false,
                    error = "No se encontraron categorías para el idioma: $language"
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Error manejando categorías vacías: ${e.message}"
            )
        }
    }
    
    /**
     * Legacy method - kept for compatibility
     */
    private fun loadCategories(language: String) {
        viewModelScope.launch {
            loadCategoriesWithValidation(language)
        }
    }

    /**
     * Recargar categorías manualmente
     */
    fun refreshCategories() {
        viewModelScope.launch {
            val language = userPreferences.appLanguage.first()
            loadCategories(language)
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
