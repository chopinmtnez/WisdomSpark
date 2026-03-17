package com.albertowisdom.wisdomspark.domain.usecase

import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import javax.inject.Inject

/**
 * Use case para alternar el estado de favorito de una cita
 * Encapsula la lógica de negocio de gestión de favoritos
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val quoteRepository: QuoteRepository
) {
    
    /**
     * Ejecuta el toggle de favorito para una cita
     * @param quote La cita a modificar
     * @return Quote La cita actualizada
     */
    suspend operator fun invoke(quote: Quote): Quote {
        val updatedQuote = quote.copy(isFavorite = !quote.isFavorite)
        quoteRepository.updateQuote(updatedQuote)
        return updatedQuote
    }
}