package com.albertowisdom.wisdomspark.di

import androidx.room.Room
import android.content.Context
import com.albertowisdom.wisdomspark.data.local.database.WisdomSparkDatabase
import com.albertowisdom.wisdomspark.data.local.database.dao.QuoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideWisdomSparkDatabase(
        @ApplicationContext context: Context
    ): WisdomSparkDatabase {
        return Room.databaseBuilder(
            context,
            WisdomSparkDatabase::class.java,
            WisdomSparkDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideQuoteDao(database: WisdomSparkDatabase): QuoteDao {
        return database.quoteDao()
    }
}
