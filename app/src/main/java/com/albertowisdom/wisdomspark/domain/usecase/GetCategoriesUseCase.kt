package com.albertowisdom.wisdomspark.domain.usecase

import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import com.albertowisdom.wisdomspark.utils.getCategoryEmoji
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Representa una categoría con metadatos adicionales
 */
data class CategoryItem(
    val name: String,
    val emoji: String,
    val quotesCount: Int = 0
)

/**
 * Use case para obtener categorías disponibles
 * Encapsula la lógica de negocio de categorías con emoji y conteo
 */
class GetCategoriesUseCase @Inject constructor(
    private val quoteRepository: QuoteRepository,
    private val userPreferences: UserPreferences
) {
    
    /**
     * Ejecuta el caso de uso para obtener categorías enriquecidas
     * @return List<CategoryItem> Lista de categorías con metadatos
     */
    suspend operator fun invoke(): List<CategoryItem> {
        val currentLanguage = userPreferences.appLanguage.first()
        val categories = quoteRepository.getAllCategoriesByLanguage(currentLanguage)
        
        return categories.map { categoryName ->
            CategoryItem(
                name = categoryName,
                emoji = getCategoryEmoji(categoryName),
                quotesCount = 0 // Por performance, podrías cachear esto si es necesario
            )
        }.sortedBy { it.name }
    }
}