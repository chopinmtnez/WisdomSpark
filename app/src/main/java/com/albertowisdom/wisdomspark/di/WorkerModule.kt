package com.albertowisdom.wisdomspark.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    // No necesitamos providers específicos para WorkManager con Hilt
    // HiltWorkerFactory se encarga automáticamente
}