package com.albertowisdom.wisdomspark.data.local.database.dao

import androidx.room.*
import com.albertowisdom.wisdomspark.data.local.database.entities.QuoteEntity
import kotlinx.coroutines.flow.Flow

// Data classes para consultas con COUNT
data class CategoryCount(
    val category: String,
    val count: Int
)

data class AuthorCount(
    val author: String,
    val count: Int
)

@Dao
interface QuoteDao {
    
    // ========== OPERACIONES BÁSICAS ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<QuoteEntity>)

    @Update
    suspend fun updateQuote(quote: QuoteEntity)

    @Delete
    suspend fun deleteQuote(quote: QuoteEntity)

    @Query("DELETE FROM quotes")
    suspend fun deleteAllQuotes()

    // ========== CITAS DIARIAS ==========

    @Query("SELECT * FROM quotes WHERE dateShown = :date LIMIT 1")
    suspend fun getTodayQuote(date: String): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE dateShown IS NULL ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomUnshownQuote(): QuoteEntity?

    @Query("UPDATE quotes SET dateShown = NULL")
    suspend fun resetAllDatesShown()

    @Query("UPDATE quotes SET dateShown = NULL WHERE dateShown = :date")
    suspend fun clearTodayQuotes(date: String)

    // ========== OBTENER CITAS ==========

    @Query("SELECT * FROM quotes")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes")
    suspend fun getAllQuotesSync(): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Long): QuoteEntity?

    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE id NOT IN (:excludeIds) ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuoteExcluding(excludeIds: List<Long>): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE id NOT IN (:excludeIds) ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomQuotesExcluding(excludeIds: List<Long>, count: Int): List<QuoteEntity>

    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuotes(limit: Int): List<QuoteEntity>

    // ========== FAVORITOS ==========

    @Query("SELECT * FROM quotes WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE isFavorite = 1")
    suspend fun getFavoriteQuotesSync(): List<QuoteEntity>

    @Query("UPDATE quotes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    // ========== CATEGORÍAS ==========

    @Query("SELECT * FROM quotes WHERE category = :category ORDER BY id DESC")
    fun getQuotesByCategory(category: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE category = :category")
    suspend fun getQuotesByCategorySync(category: String): List<QuoteEntity>

    @Query("SELECT DISTINCT category FROM quotes ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT category, COUNT(*) as count FROM quotes GROUP BY category ORDER BY count DESC")
    suspend fun getCategoriesWithCount(): List<CategoryCount>

    // ========== BÚSQUEDA ==========

    @Query("""
        SELECT * FROM quotes 
        WHERE text LIKE :query 
           OR author LIKE :query 
           OR category LIKE :query
        ORDER BY 
            CASE 
                WHEN text LIKE :query THEN 1
                WHEN author LIKE :query THEN 2
                WHEN category LIKE :query THEN 3
                ELSE 4
            END
    """)
    suspend fun searchQuotes(query: String): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE author LIKE :authorQuery")
    suspend fun searchByAuthor(authorQuery: String): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE text LIKE :textQuery")
    suspend fun searchByText(textQuery: String): List<QuoteEntity>

    // ========== ESTADÍSTICAS ==========

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun getQuotesCount(): Int

    @Query("SELECT COUNT(*) FROM quotes WHERE isFavorite = 1")
    suspend fun getFavoritesCount(): Int

    @Query("SELECT COUNT(*) FROM quotes WHERE dateShown IS NOT NULL")
    suspend fun getShownQuotesCount(): Int

    @Query("SELECT COUNT(*) FROM quotes WHERE dateShown IS NULL")
    suspend fun getUnshownQuotesCount(): Int

    @Query("SELECT COUNT(*) FROM quotes WHERE category = :category")
    suspend fun getQuotesCountByCategory(category: String): Int

    @Query("SELECT COUNT(DISTINCT author) FROM quotes")
    suspend fun getUniqueAuthorsCount(): Int

    @Query("SELECT COUNT(DISTINCT category) FROM quotes")
    suspend fun getUniqueCategoriesCount(): Int

    // ========== AUTORES ==========

    @Query("SELECT DISTINCT author FROM quotes ORDER BY author ASC")
    suspend fun getAllAuthors(): List<String>

    @Query("SELECT author, COUNT(*) as count FROM quotes GROUP BY author ORDER BY count DESC")
    suspend fun getAuthorsWithCount(): List<AuthorCount>

    @Query("SELECT * FROM quotes WHERE author = :author ORDER BY id DESC")
    suspend fun getQuotesByAuthor(author: String): List<QuoteEntity>

    // ========== FUNCIONES DE MANTENIMIENTO ==========

    @Query("UPDATE quotes SET isFavorite = 0")
    suspend fun clearAllFavorites()

    @Query("DELETE FROM quotes WHERE category = :category")
    suspend fun deleteQuotesByCategory(category: String)

    @Query("DELETE FROM quotes WHERE author = :author")
    suspend fun deleteQuotesByAuthor(author: String)

    // ========== ORDENAMIENTO Y FILTROS ==========

    @Query("SELECT * FROM quotes ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentQuotes(limit: Int): List<QuoteEntity>

    @Query("SELECT * FROM quotes ORDER BY author ASC")
    suspend fun getQuotesOrderedByAuthor(): List<QuoteEntity>

    @Query("SELECT * FROM quotes ORDER BY category ASC")
    suspend fun getQuotesOrderedByCategory(): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE LENGTH(text) <= :maxLength")
    suspend fun getShortQuotes(maxLength: Int): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE LENGTH(text) >= :minLength")
    suspend fun getLongQuotes(minLength: Int): List<QuoteEntity>

    // ========== FUNCIONES AVANZADAS ==========

    @Query("""
        SELECT * FROM quotes 
        WHERE category = :category 
        AND id NOT IN (:excludeIds) 
        ORDER BY RANDOM() 
        LIMIT 1
    """)
    suspend fun getRandomQuoteFromCategory(category: String, excludeIds: List<Long>): QuoteEntity?

    @Query("""
        SELECT * FROM quotes 
        WHERE author = :author 
        AND id NOT IN (:excludeIds) 
        ORDER BY RANDOM() 
        LIMIT 1
    """)
    suspend fun getRandomQuoteFromAuthor(author: String, excludeIds: List<Long>): QuoteEntity?

    @Query("""
        SELECT * FROM quotes 
        WHERE isFavorite = 0 
        AND dateShown IS NULL 
        ORDER BY RANDOM() 
        LIMIT :count
    """)
    suspend fun getUnseenNonFavoriteQuotes(count: Int): List<QuoteEntity>

    @Query("""
        SELECT * FROM quotes 
        WHERE dateShown IS NOT NULL 
        ORDER BY dateShown DESC 
        LIMIT :count
    """)
    suspend fun getRecentlyShownQuotes(count: Int): List<QuoteEntity>

    // ========== VALIDACIÓN Y LIMPIEZA ==========

    @Query("SELECT COUNT(*) FROM quotes WHERE text = '' OR author = '' OR category = ''")
    suspend fun getInvalidQuotesCount(): Int

    @Query("DELETE FROM quotes WHERE text = '' OR author = '' OR category = ''")
    suspend fun deleteInvalidQuotes(): Int

    @Query("SELECT * FROM quotes WHERE text = :text AND author = :author")
    suspend fun findDuplicateQuote(text: String, author: String): QuoteEntity?

    @Query("""
        DELETE FROM quotes 
        WHERE id NOT IN (
            SELECT MIN(id) 
            FROM quotes 
            GROUP BY text, author
        )
    """)
    suspend fun removeDuplicateQuotes(): Int

    // ========== COMPATIBILIDAD CON VERSIÓN ANTERIOR ==========
    
    @Query("SELECT COUNT(*) FROM quotes WHERE isFavorite = 1")
    suspend fun getFavoriteQuotesCount(): Int
    
    @Query("UPDATE quotes SET dateShown = NULL WHERE dateShown IS NOT NULL")
    suspend fun resetAllQuotesAsUnshown()
    
    @Query("SELECT * FROM quotes ORDER BY category, author")
    suspend fun getAllQuotesOrderedByCategory(): List<QuoteEntity>
}
