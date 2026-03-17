package com.albertowisdom.wisdomspark.domain.usecase

import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests para ToggleFavoriteUseCase usando JUnit5
 */
@DisplayName("ToggleFavoriteUseCase")
class ToggleFavoriteUseCaseTest {

    private lateinit var quoteRepository: QuoteRepository
    private lateinit var useCase: ToggleFavoriteUseCase

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
        useCase = ToggleFavoriteUseCase(quoteRepository)
    }

    @Nested
    @DisplayName("When toggling favorite status")
    inner class WhenTogglingFavoriteStatus {

        @Test
        @DisplayName("Should mark non-favorite quote as favorite")
        fun shouldMarkNonFavoriteQuoteAsFavorite() = runTest {
            // Given
            val nonFavoriteQuote = sampleQuote.copy(isFavorite = false)

            // When
            val result = useCase(nonFavoriteQuote)

            // Then
            assertTrue(result.isFavorite)
            assertEquals(sampleQuote.id, result.id)
            verify(quoteRepository).updateQuote(result)
        }

        @Test
        @DisplayName("Should unmark favorite quote as non-favorite")
        fun shouldUnmarkFavoriteQuoteAsNonFavorite() = runTest {
            // Given
            val favoriteQuote = sampleQuote.copy(isFavorite = true)

            // When
            val result = useCase(favoriteQuote)

            // Then
            assertFalse(result.isFavorite)
            assertEquals(sampleQuote.id, result.id)
            verify(quoteRepository).updateQuote(result)
        }

        @Test
        @DisplayName("Should preserve all other quote properties")
        fun shouldPreserveAllOtherQuoteProperties() = runTest {
            // Given
            val originalQuote = sampleQuote.copy(
                text = "Custom text",
                author = "Custom Author",
                category = "Custom Category",
                language = "en",
                isFavorite = false
            )

            // When
            val result = useCase(originalQuote)

            // Then
            assertEquals(originalQuote.text, result.text)
            assertEquals(originalQuote.author, result.author)
            assertEquals(originalQuote.category, result.category)
            assertEquals(originalQuote.language, result.language)
            assertEquals(originalQuote.id, result.id)
            assertTrue(result.isFavorite) // Solo este campo debe cambiar
        }

        @Test
        @DisplayName("Should propagate repository exceptions")
        fun shouldPropagateRepositoryExceptions() = runTest {
            // Given
            whenever(quoteRepository.updateQuote(any()))
                .thenThrow(RuntimeException("Database update failed"))

            // When & Then
            val exception = assertThrows<RuntimeException> {
                useCase(sampleQuote)
            }
            assertEquals("Database update failed", exception.message)
        }
    }
}