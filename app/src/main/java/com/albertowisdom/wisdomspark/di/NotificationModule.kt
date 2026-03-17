package com.albertowisdom.wisdomspark.di

import android.content.Context
import androidx.work.WorkManager
import com.albertowisdom.wisdomspark.utils.NotificationHelper
import com.albertowisdom.wisdomspark.utils.NotificationScheduler
import com.albertowisdom.wisdomspark.utils.NotificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    
    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper {
        return NotificationHelper(context)
    }
    
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationScheduler(
        workManager: WorkManager
    ): NotificationScheduler {
        return NotificationScheduler(workManager)
    }
}