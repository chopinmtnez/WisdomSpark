package com.albertowisdom.wisdomspark.di

import android.content.Context
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.premium.billing.BillingManager
import com.albertowisdom.wisdomspark.premium.manager.PremiumFeatureManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para funcionalidades Premium
 */
@Module
@InstallIn(SingletonComponent::class)
object PremiumModule {

    @Provides
    @Singleton
    fun provideBillingManager(
        @ApplicationContext context: Context
    ): BillingManager {
        return BillingManager(context)
    }

    @Provides
    @Singleton
    fun providePremiumFeatureManager(
        @ApplicationContext context: Context,
        billingManager: BillingManager,
        userPreferences: UserPreferences
    ): PremiumFeatureManager {
        return PremiumFeatureManager(context, billingManager, userPreferences)
    }
}