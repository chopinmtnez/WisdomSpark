package com.albertowisdom.wisdomspark.di

import com.albertowisdom.wisdomspark.data.managers.LanguageManager
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagerModule {
    
    @Provides
    @Singleton
    fun provideLanguageManager(
        quoteRepository: QuoteRepository,
        userPreferences: UserPreferences
    ): LanguageManager {
        return LanguageManager(quoteRepository, userPreferences)
    }
}