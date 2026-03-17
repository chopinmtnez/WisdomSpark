package com.albertowisdom.wisdomspark.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.albertowisdom.wisdomspark.data.local.database.dao.QuoteDao
import com.albertowisdom.wisdomspark.data.local.database.entities.QuoteEntity

@Database(
    entities = [QuoteEntity::class],
    version = 4,
    exportSchema = false
)
abstract class WisdomSparkDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    
    companion object {
        const val DATABASE_NAME = "wisdomspark_db"
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columna language con valor predeterminado 'es'
                database.execSQL("ALTER TABLE quotes ADD COLUMN language TEXT NOT NULL DEFAULT 'es'")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Limpieza automática para resolver problemas de idioma
                // Eliminar todas las citas existentes para forzar nueva sincronización
                database.execSQL("DELETE FROM quotes")
                // Resetear el autoincrement
                database.execSQL("DELETE FROM sqlite_sequence WHERE name='quotes'")
            }
        }
        
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar índices para optimizar consultas de rendimiento
                database.execSQL("CREATE INDEX IF NOT EXISTS `idx_category_language` ON `quotes` (`category`, `language`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `idx_is_favorite` ON `quotes` (`isFavorite`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `idx_date_shown` ON `quotes` (`dateShown`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `idx_language` ON `quotes` (`language`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `idx_text_author` ON `quotes` (`text`, `author`)")
            }
        }
    }
}
