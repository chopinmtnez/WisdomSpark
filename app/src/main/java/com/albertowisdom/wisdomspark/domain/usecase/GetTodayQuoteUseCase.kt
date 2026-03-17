package com.albertowisdom.wisdomspark.domain.usecase

import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case para obtener la cita del día
 * Encapsula la lógica de negocio de obtener/crear la cita diaria
 */
class GetTodayQuoteUseCase @Inject constructor(
    private val quoteRepository: QuoteRepository,
    private val userPreferences: UserPreferences
) {
    
    /**
     * Ejecuta el caso de uso para obtener la cita del día
     * @return Quote la cita del día en el idioma del usuario
     */
    suspend operator fun invoke(): Quote {
        val currentLanguage = userPreferences.appLanguage.first()
        return quoteRepository.getOrCreateTodayQuote(currentLanguage)
    }
}