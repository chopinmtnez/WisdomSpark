package com.albertowisdom.wisdomspark.di

import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import com.albertowisdom.wisdomspark.domain.usecase.GetCategoriesUseCase
import com.albertowisdom.wisdomspark.domain.usecase.GetTodayQuoteUseCase
import com.albertowisdom.wisdomspark.domain.usecase.ToggleFavoriteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para inyección de Use Cases
 * Siguiendo Clean Architecture principles
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    @Provides
    @Singleton
    fun provideGetTodayQuoteUseCase(
        quoteRepository: QuoteRepository,
        userPreferences: UserPreferences
    ): GetTodayQuoteUseCase {
        return GetTodayQuoteUseCase(quoteRepository, userPreferences)
    }
    
    @Provides
    @Singleton
    fun provideToggleFavoriteUseCase(
        quoteRepository: QuoteRepository
    ): ToggleFavoriteUseCase {
        return ToggleFavoriteUseCase(quoteRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetCategoriesUseCase(
        quoteRepository: QuoteRepository,
        userPreferences: UserPreferences
    ): GetCategoriesUseCase {
        return GetCategoriesUseCase(quoteRepository, userPreferences)
    }
}