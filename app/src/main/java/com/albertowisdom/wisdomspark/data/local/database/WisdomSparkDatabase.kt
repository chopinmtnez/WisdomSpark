package com.albertowisdom.wisdomspark.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.albertowisdom.wisdomspark.data.local.database.dao.QuoteDao
import com.albertowisdom.wisdomspark.data.local.database.entities.QuoteEntity

@Database(
    entities = [QuoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WisdomSparkDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    
    companion object {
        const val DATABASE_NAME = "wisdomspark_db"
    }
}
