package com.albertowisdom.wisdomspark.functional

import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import com.albertowisdom.wisdomspark.domain.usecase.GetTodayQuoteUseCase
import com.albertowisdom.wisdomspark.domain.usecase.ToggleFavoriteUseCase
import com.albertowisdom.wisdomspark.domain.usecase.GetCategoriesUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Tests funcionales para verificar que los Use Cases funcionan correctamente
 */
class UseCaseFunctionalTest {

    private val mockQuoteRepository: QuoteRepository = mock()
    private val mockUserPreferences: UserPreferences = mock()
    
    private val sampleQuote = Quote(
        id = 1,
        text = "Test quote",
        author = "Test Author",
        category = "Motivación",
        language = "es",
        isFavorite = false
    )

    @Test
    fun `GetTodayQuoteUseCase debe retornar cita en idioma correcto`() = runTest {
        // Arrange
        whenever(mockUserPreferences.appLanguage).thenReturn(flowOf("es"))
        whenever(mockQuoteRepository.getOrCreateTodayQuote("es")).thenReturn(sampleQuote)
        
        val useCase = GetTodayQuoteUseCase(mockQuoteRepository, mockUserPreferences)
        
        // Act
        val result = useCase()
        
        // Assert
        assertNotNull(result)
        assertEquals("es", result.language)
        assertEquals("Test quote", result.text)
        assertEquals("Test Author", result.author)
    }

    @Test
    fun `ToggleFavoriteUseCase debe cambiar estado de favorito correctamente`() = runTest {
        // Arrange - cita NO favorita
        val nonFavoriteQuote = sampleQuote.copy(isFavorite = false)
        val useCase = ToggleFavoriteUseCase(mockQuoteRepository)
        
        // Act
        val result = useCase(nonFavoriteQuote)
        
        // Assert
        assertTrue(result.isFavorite) // Debe ser true después del toggle
        assertEquals(nonFavoriteQuote.id, result.id)
        assertEquals(nonFavoriteQuote.text, result.text)
        assertEquals(nonFavoriteQuote.author, result.author)
    }

    @Test
    fun `ToggleFavoriteUseCase debe desfavoritar cita favorita`() = runTest {
        // Arrange - cita SÍ favorita
        val favoriteQuote = sampleQuote.copy(isFavorite = true)
        val useCase = ToggleFavoriteUseCase(mockQuoteRepository)
        
        // Act
        val result = useCase(favoriteQuote)
        
        // Assert
        assertFalse(result.isFavorite) // Debe ser false después del toggle
        assertEquals(favoriteQuote.id, result.id)
    }

    @Test
    fun `GetCategoriesUseCase debe retornar categorías con emojis`() = runTest {
        // Arrange
        val mockCategories = listOf("Motivación", "Liderazgo", "Vida")
        whenever(mockUserPreferences.appLanguage).thenReturn(flowOf("es"))
        whenever(mockQuoteRepository.getAllCategoriesByLanguage("es")).thenReturn(mockCategories)
        
        val useCase = GetCategoriesUseCase(mockQuoteRepository, mockUserPreferences)
        
        // Act
        val result = useCase()
        
        // Assert
        assertNotNull(result)
        assertEquals(3, result.size)
        
        // Verificar que cada categoría tiene nombre y emoji
        result.forEach { categoryItem ->
            assertTrue(categoryItem.name.isNotEmpty())
            assertTrue(categoryItem.emoji.isNotEmpty())
            assertEquals(0, categoryItem.quotesCount) // Default value
        }
        
        // Verificar categorías específicas
        val motivacionCategory = result.find { it.name == "Motivación" }
        assertNotNull(motivacionCategory)
        assertEquals("💪", motivacionCategory.emoji)
        
        val liderazgoCategory = result.find { it.name == "Liderazgo" }
        assertNotNull(liderazgoCategory)
        assertEquals("👑", liderazgoCategory.emoji)
    }

    @Test
    fun `GetCategoriesUseCase debe ordenar categorías alfabéticamente`() = runTest {
        // Arrange - categorías en orden NO alfabético
        val unorderedCategories = listOf("Vida", "Amor", "Motivación", "Creatividad")
        whenever(mockUserPreferences.appLanguage).thenReturn(flowOf("es"))
        whenever(mockQuoteRepository.getAllCategoriesByLanguage("es")).thenReturn(unorderedCategories)
        
        val useCase = GetCategoriesUseCase(mockQuoteRepository, mockUserPreferences)
        
        // Act
        val result = useCase()
        
        // Assert
        val categoryNames = result.map { it.name }
        val expectedOrder = listOf("Amor", "Creatividad", "Motivación", "Vida")
        assertEquals(expectedOrder, categoryNames)
    }

    @Test
    fun `Use Cases deben manejar excepciones de repository`() = runTest {
        // Arrange - Repository que lanza excepción
        whenever(mockUserPreferences.appLanguage).thenReturn(flowOf("es"))
        whenever(mockQuoteRepository.getOrCreateTodayQuote("es"))
            .thenThrow(RuntimeException("Database error"))
        
        val useCase = GetTodayQuoteUseCase(mockQuoteRepository, mockUserPreferences)
        
        // Act & Assert
        try {
            useCase()
            throw AssertionError("Se esperaba una excepción")
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }
    }
}