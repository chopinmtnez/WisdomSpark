package com.albertowisdom.wisdomspark.di

import android.content.Context
import com.albertowisdom.wisdomspark.security.PlayIntegrityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para componentes de seguridad
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun providePlayIntegrityManager(
        @ApplicationContext context: Context
    ): PlayIntegrityManager {
        return PlayIntegrityManager(context)
    }
}