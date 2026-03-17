package com.albertowisdom.wisdomspark.integration

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.albertowisdom.wisdomspark.data.local.database.WisdomSparkDatabase
import com.albertowisdom.wisdomspark.data.local.database.dao.QuoteDao
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.remote.repository.GoogleSheetsRepository
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests para QuoteRepository
 * Testa la integración entre Repository, DAO y Database
 */
@RunWith(RobolectricTestRunner::class)
@DisplayName("QuoteRepository Integration Tests")
class QuoteRepositoryIntegrationTest {

    private lateinit var database: WisdomSparkDatabase
    private lateinit var quoteDao: QuoteDao
    private lateinit var googleSheetsRepository: GoogleSheetsRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var quoteRepository: QuoteRepository

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
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WisdomSparkDatabase::class.java
        ).allowMainThreadQueries().build()

        quoteDao = database.quoteDao()
        googleSheetsRepository = mock()
        userPreferences = mock()

        quoteRepository = QuoteRepository(
            quoteDao = quoteDao,
            googleSheetsRepository = googleSheetsRepository,
            userPreferences = userPreferences
        )

        // Setup default user preferences
        whenever(userPreferences.appLanguage).thenReturn(flowOf("es"))
    }

    @AfterEach
    fun teardown() {
        database.close()
    }

    @Nested
    @DisplayName("Quote CRUD Operations")
    inner class QuoteCrudOperations {

        @Test
        @DisplayName("Should insert and retrieve quotes")
        fun shouldInsertAndRetrieveQuotes() = runTest {
            // Given
            val quote = sampleQuotes.first()

            // When
            quoteRepository.insertQuote(quote)
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
            quoteRepository.insertQuote(quote)

            // When
            quoteRepository.toggleFavorite(quote)
            val updatedQuote = quoteRepository.getQuoteById(quote.id)

            // Then
            assertNotNull(updatedQuote)
            assertTrue(updatedQuote.isFavorite)
        }
    }

    @Nested
    @DisplayName("Category Operations")
    inner class CategoryOperations {

        @BeforeEach
        fun setupQuotes() = runTest {
            sampleQuotes.forEach { quote ->
                quoteRepository.insertQuote(quote)
            }
        }

        @Test
        @DisplayName("Should get categories by language")
        fun shouldGetCategoriesByLanguage() = runTest {
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
            // When
            val motivationQuotes = quoteRepository.getQuotesByCategory("Motivación", "es")
                .test {
                    val quotes = awaitItem()
                    assertEquals(1, quotes.size)
                    assertEquals("Test quote 1", quotes.first().text)
                    cancelAndIgnoreRemainingEvents()
                }
        }
    }

    @Nested
    @DisplayName("Favorite Operations")
    inner class FavoriteOperations {

        @BeforeEach
        fun setupQuotes() = runTest {
            sampleQuotes.forEach { quote ->
                quoteRepository.insertQuote(quote)
            }
        }

        @Test
        @DisplayName("Should get favorite quotes")
        fun shouldGetFavoriteQuotes() = runTest {
            // When
            quoteRepository.getFavoriteQuotes().test {
                val favorites = awaitItem()
                
                // Then
                assertEquals(1, favorites.size)
                assertTrue(favorites.first().isFavorite)
                assertEquals("Test quote 2", favorites.first().text)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        @DisplayName("Should get correct favorites count")
        fun shouldGetCorrectFavoritesCount() = runTest {
            // When
            val count = quoteRepository.getFavoritesCount()

            // Then
            assertEquals(1, count)
        }
    }

    @Nested
    @DisplayName("Today Quote Operations")
    inner class TodayQuoteOperations {

        @BeforeEach
        fun setupQuotes() = runTest {
            sampleQuotes.forEach { quote ->
                quoteRepository.insertQuote(quote)
            }
        }

        @Test
        @DisplayName("Should get or create today quote in user language")
        fun shouldGetOrCreateTodayQuoteInUserLanguage() = runTest {
            // When
            val todayQuote = quoteRepository.getOrCreateTodayQuote("es")

            // Then
            assertNotNull(todayQuote)
            assertEquals("es", todayQuote.language)
            assertNotNull(todayQuote.dateShown)
        }

        @Test
        @DisplayName("Should return same quote for same day")
        fun shouldReturnSameQuoteForSameDay() = runTest {
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
        @DisplayName("Should sanitize quote text on insert")
        fun shouldSanitizeQuoteTextOnInsert() = runTest {
            // Given
            val maliciousQuote = Quote(
                text = "Test <script>alert('xss')</script> quote",
                author = "Author & Co",
                category = "Test\"Category",
                language = "es"
            )

            // When
            quoteRepository.insertQuote(maliciousQuote)
            val retrievedQuote = quoteRepository.getQuoteById(maliciousQuote.id)

            // Then
            assertNotNull(retrievedQuote)
            // Verify that dangerous characters were removed
            assertFalse(retrievedQuote.text.contains("<script>"))
            assertFalse(retrievedQuote.author.contains("&"))
            assertFalse(retrievedQuote.category.contains("\""))
        }

        @Test
        @DisplayName("Should validate language codes")
        fun shouldValidateLanguageCodes() = runTest {
            // Given
            val quoteWithInvalidLanguage = Quote(
                text = "Test quote",
                author = "Test Author",
                category = "Test",
                language = "invalid_lang"
            )

            // When
            quoteRepository.insertQuote(quoteWithInvalidLanguage)
            val retrievedQuote = quoteRepository.getQuoteById(quoteWithInvalidLanguage.id)

            // Then
            assertNotNull(retrievedQuote)
            assertEquals("es", retrievedQuote.language) // Should default to Spanish
        }
    }
}

// Extension function for testing Flows
private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.test(
    validation: suspend kotlinx.coroutines.flow.FlowCollector<T>.() -> Unit
) {
    validation(this)
}