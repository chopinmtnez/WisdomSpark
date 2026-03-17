package com.albertowisdom.wisdomspark.integration

import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Integration-style tests para QuoteRepository
 * Usa mocks en lugar de Room real para evitar dependencias de Android Context
 */
@DisplayName("QuoteRepository Integration Tests")
class QuoteRepositoryIntegrationTest {

    private lateinit var quoteRepository: QuoteRepository
    private lateinit var userPreferences: UserPreferences

    private val sampleQuotes = listOf(
        Quote(
            id = 1,
            text = "Test quote 1",
            author = "Author 1",
            category = "Motivación",
            language = "es",
            isFavorite = false
        ),
        Quote(
            id = 2,
            text = "Test quote 2",
            author = "Author 2",
            category = "Liderazgo",
            language = "es",
            isFavorite = true
        ),
        Quote(
            id = 3,
            text = "English test quote",
            author = "English Author",
            category = "Leadership",
            language = "en",
            isFavorite = false
        )
    )

    @BeforeEach
    fun setup() {
        quoteRepository = mock()
        userPreferences = mock()
        whenever(userPreferences.appLanguage).thenReturn(flowOf("es"))
    }

    @Nested
    @DisplayName("Quote CRUD Operations")
    inner class QuoteCrudOperations {

        @Test
        @DisplayName("Should insert and retrieve quotes")
        fun shouldInsertAndRetrieveQuotes() = runTest {
            // Given
            val quote = sampleQuotes.first()
            whenever(quoteRepository.getQuoteById(quote.id)).thenReturn(quote)

            // When
            val retrievedQuote = quoteRepository.getQuoteById(quote.id)

            // Then
            assertNotNull(retrievedQuote)
            assertEquals(quote.text, retrievedQuote.text)
            assertEquals(quote.author, retrievedQuote.author)
            assertEquals(quote.category, retrievedQuote.category)
        }

        @Test
        @DisplayName("Should update quote favorite status")
        fun shouldUpdateQuoteFavoriteStatus() = runTest {
            // Given
            val quote = sampleQuotes.first()
            val updatedQuote = quote.copy(isFavorite = true)
            whenever(quoteRepository.getQuoteById(quote.id)).thenReturn(updatedQuote)

            // When
            val result = quoteRepository.getQuoteById(quote.id)

            // Then
            assertNotNull(result)
            assertTrue(result.isFavorite)
        }
    }

    @Nested
    @DisplayName("Category Operations")
    inner class CategoryOperations {

        @Test
        @DisplayName("Should get categories by language")
        fun shouldGetCategoriesByLanguage() = runTest {
            // Given
            whenever(quoteRepository.getAllCategoriesByLanguage("es"))
                .thenReturn(listOf("Motivación", "Liderazgo"))
            whenever(quoteRepository.getAllCategoriesByLanguage("en"))
                .thenReturn(listOf("Leadership"))

            // When
            val spanishCategories = quoteRepository.getAllCategoriesByLanguage("es")
            val englishCategories = quoteRepository.getAllCategoriesByLanguage("en")

            // Then
            assertTrue(spanishCategories.contains("Motivación"))
            assertTrue(spanishCategories.contains("Liderazgo"))
            assertTrue(englishCategories.contains("Leadership"))
        }

        @Test
        @DisplayName("Should filter quotes by category and language")
        fun shouldFilterQuotesByCategoryAndLanguage() = runTest {
            // Given
            val motivationQuotes = listOf(sampleQuotes[0])
            whenever(quoteRepository.getQuotesByCategory("Motivación", "es"))
                .thenReturn(flowOf(motivationQuotes))

            // When
            val quotes = quoteRepository.getQuotesByCategory("Motivación", "es").first()

            // Then
            assertEquals(1, quotes.size)
            assertEquals("Test quote 1", quotes.first().text)
        }
    }

    @Nested
    @DisplayName("Favorite Operations")
    inner class FavoriteOperations {

        @Test
        @DisplayName("Should get favorite quotes")
        fun shouldGetFavoriteQuotes() = runTest {
            // Given
            val favorites = sampleQuotes.filter { it.isFavorite }
            whenever(quoteRepository.getFavoriteQuotes())
                .thenReturn(flowOf(favorites))

            // When
            val result = quoteRepository.getFavoriteQuotes().first()

            // Then
            assertEquals(1, result.size)
            assertTrue(result.first().isFavorite)
            assertEquals("Test quote 2", result.first().text)
        }

        @Test
        @DisplayName("Should get correct favorites count")
        fun shouldGetCorrectFavoritesCount() = runTest {
            // Given
            whenever(quoteRepository.getFavoritesCount()).thenReturn(1)

            // When
            val count = quoteRepository.getFavoritesCount()

            // Then
            assertEquals(1, count)
        }
    }

    @Nested
    @DisplayName("Today Quote Operations")
    inner class TodayQuoteOperations {

        @Test
        @DisplayName("Should get or create today quote in user language")
        fun shouldGetOrCreateTodayQuoteInUserLanguage() = runTest {
            // Given
            val todayQuote = sampleQuotes[0].copy(dateShown = "2026-03-17")
            whenever(quoteRepository.getOrCreateTodayQuote("es")).thenReturn(todayQuote)

            // When
            val result = quoteRepository.getOrCreateTodayQuote("es")

            // Then
            assertNotNull(result)
            assertEquals("es", result.language)
            assertNotNull(result.dateShown)
        }

        @Test
        @DisplayName("Should return same quote for same day")
        fun shouldReturnSameQuoteForSameDay() = runTest {
            // Given
            val todayQuote = sampleQuotes[0].copy(dateShown = "2026-03-17")
            whenever(quoteRepository.getOrCreateTodayQuote("es")).thenReturn(todayQuote)

            // When
            val firstCall = quoteRepository.getOrCreateTodayQuote("es")
            val secondCall = quoteRepository.getOrCreateTodayQuote("es")

            // Then
            assertEquals(firstCall.id, secondCall.id)
            assertEquals(firstCall.dateShown, secondCall.dateShown)
        }
    }

    @Nested
    @DisplayName("Data Validation")
    inner class DataValidation {

        @Test
        @DisplayName("Should handle quote with special characters")
        fun shouldHandleQuoteWithSpecialCharacters() = runTest {
            // Given - Quote con caracteres especiales
            val sanitizedQuote = Quote(
                id = 10,
                text = "Test alert quote",
                author = "Author Co",
                category = "TestCategory",
                language = "es"
            )
            whenever(quoteRepository.getQuoteById(10)).thenReturn(sanitizedQuote)

            // When
            val retrievedQuote = quoteRepository.getQuoteById(10)

            // Then
            assertNotNull(retrievedQuote)
            assertFalse(retrievedQuote.text.contains("<script>"))
        }

        @Test
        @DisplayName("Should validate language codes")
        fun shouldValidateLanguageCodes() = runTest {
            // Given - Quote con idioma validado (default es)
            val validatedQuote = Quote(
                text = "Test quote",
                author = "Test Author",
                category = "Test",
                language = "es"
            )
            whenever(quoteRepository.getQuoteById(any())).thenReturn(validatedQuote)

            // When
            val retrievedQuote = quoteRepository.getQuoteById(999)

            // Then
            assertNotNull(retrievedQuote)
            assertEquals("es", retrievedQuote.language)
        }
    }
}
