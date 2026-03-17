package com.albertowisdom.wisdomspark.domain.usecase

import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

/**
 * Unit tests para GetTodayQuoteUseCase usando JUnit5
 * Demuestra testing moderno con DSL semántico
 */
@DisplayName("GetTodayQuoteUseCase")
class GetTodayQuoteUseCaseTest {

    private lateinit var quoteRepository: QuoteRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var useCase: GetTodayQuoteUseCase

    private val sampleQuote = Quote(
        id = 1,
        text = "Sample quote text",
        author = "Sample Author",
        category = "Motivación",
        language = "es",
        isFavorite = false
    )

    @BeforeEach
    fun setup() {
        quoteRepository = mock()
        userPreferences = mock()
        useCase = GetTodayQuoteUseCase(quoteRepository, userPreferences)
    }

    @Nested
    @DisplayName("When getting today's quote")
    inner class WhenGettingTodaysQuote {

        @Test
        @DisplayName("Should return quote in user's language")
        fun shouldReturnQuoteInUsersLanguage() = runTest {
            // Given
            val expectedLanguage = "es"
            whenever(userPreferences.appLanguage).thenReturn(flowOf(expectedLanguage))
            whenever(quoteRepository.getOrCreateTodayQuote(expectedLanguage)).thenReturn(sampleQuote)

            // When
            val result = useCase()

            // Then
            assertEquals(sampleQuote, result)
            verify(quoteRepository).getOrCreateTodayQuote(expectedLanguage)
        }

        @Test
        @DisplayName("Should handle English language preference")
        fun shouldHandleEnglishLanguagePreference() = runTest {
            // Given
            val expectedLanguage = "en"
            val englishQuote = sampleQuote.copy(language = "en")
            whenever(userPreferences.appLanguage).thenReturn(flowOf(expectedLanguage))
            whenever(quoteRepository.getOrCreateTodayQuote(expectedLanguage)).thenReturn(englishQuote)

            // When
            val result = useCase()

            // Then
            assertEquals(englishQuote, result)
            assertEquals("en", result.language)
        }

        @Test
        @DisplayName("Should pass through repository exceptions")
        fun shouldPassThroughRepositoryExceptions() = runTest {
            // Given
            val expectedLanguage = "es"
            whenever(userPreferences.appLanguage).thenReturn(flowOf(expectedLanguage))
            whenever(quoteRepository.getOrCreateTodayQuote(expectedLanguage))
                .thenThrow(RuntimeException("Database error"))

            // When & Then
            try {
                useCase()
                throw AssertionError("Expected exception was not thrown")
            } catch (e: RuntimeException) {
                assertEquals("Database error", e.message)
            }
        }
    }
}