package com.albertowisdom.wisdomspark.di

import com.albertowisdom.wisdomspark.ads.AdMobManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para AdMob dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AdMobModule {

    @Provides
    @Singleton
    fun provideAdMobManager(): AdMobManager {
        return AdMobManager()
    }
}
