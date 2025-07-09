package com.albertowisdom.wisdomspark.data.repository

import com.albertowisdom.wisdomspark.data.local.database.dao.QuoteDao
import com.albertowisdom.wisdomspark.data.local.database.entities.QuoteEntity
import com.albertowisdom.wisdomspark.data.local.database.entities.toEntity
import com.albertowisdom.wisdomspark.data.local.database.entities.toQuote
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.data.remote.repository.GoogleSheetsRepository
import com.albertowisdom.wisdomspark.data.remote.repository.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepository @Inject constructor(
    private val quoteDao: QuoteDao,
    private val googleSheetsRepository: GoogleSheetsRepository
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // ========== INICIALIZACIÓN ==========
    
    /**
     * Inicializa la base de datos con citas
     * Primero intenta sincronizar desde Google Sheets, si falla usa citas locales
     */
    suspend fun initializeQuotes(forceSync: Boolean = false): SyncResult {
        try {
            val localQuotesCount = quoteDao.getQuotesCount()
            
            if (localQuotesCount == 0 || forceSync) {
                // Intentar sincronizar con Google Sheets
                val syncResult = googleSheetsRepository.syncQuotes(forceUpdate = forceSync)
                
                if (syncResult is SyncResult.Success) {
                    println("✓ Inicialización exitosa: Sincronización exitosa (${localQuotesCount} citas)")
                    return syncResult
                }
                
                // Fallback: usar citas locales predeterminadas
                initializeDefaultQuotes()
                println("✓ Inicialización exitosa: Citas locales inicializadas (${getDefaultQuotes().size} citas)")
                return SyncResult.Success(
                    message = "Citas locales inicializadas",
                    quotesCount = getDefaultQuotes().size
                )
            } else {
                println("✓ Base de datos ya inicializada ($localQuotesCount citas disponibles)")
                return SyncResult.Success(
                    message = "Base de datos ya inicializada",
                    quotesCount = localQuotesCount
                )
            }
        } catch (e: Exception) {
            // En caso de error, usar citas predeterminadas
            initializeDefaultQuotes()
            println("⚠ Error en inicialización, usando datos locales: ${e.message}")
            return SyncResult.Success(
                message = "Citas locales de emergencia",
                quotesCount = getDefaultQuotes().size
            )
        }
    }

    private suspend fun initializeDefaultQuotes() {
        val existingCount = quoteDao.getQuotesCount()
        if (existingCount == 0) {
            getDefaultQuotes().forEach { quote ->
                quoteDao.insertQuote(quote.toEntity())
            }
        }
    }
    
    // ========== CITAS DIARIAS ==========

    suspend fun getOrCreateTodayQuote(): Quote {
        val today = dateFormat.format(Date())
        
        // Buscar cita existente para hoy
        val existingQuote = quoteDao.getTodayQuote(today)
        if (existingQuote != null) return existingQuote.toQuote()
        
        // Si no existe, asignar una aleatoria no mostrada
        val randomQuoteEntity = quoteDao.getRandomUnshownQuote() ?: run {
            // Si todas las citas han sido mostradas, reset y tomar una aleatoria
            quoteDao.resetAllDatesShown()
            quoteDao.getRandomUnshownQuote() ?: getDefaultQuotes().first().toEntity()
        }
        
        val updatedQuote = randomQuoteEntity.copy(dateShown = today)
        quoteDao.updateQuote(updatedQuote)
        
        return updatedQuote.toQuote()
    }

    // ========== MÉTODOS PARA SWIPE SYSTEM ==========

    /**
     * Obtiene cita aleatoria excluyendo IDs específicos
     */
    suspend fun getRandomQuoteExcluding(excludeIds: List<Long>): Quote? {
        return try {
            val availableQuote = if (excludeIds.isEmpty()) {
                quoteDao.getRandomQuote()
            } else {
                quoteDao.getRandomQuoteExcluding(excludeIds)
            }
            
            availableQuote?.toQuote() ?: run {
                // Si no hay citas disponibles, devolver una aleatoria cualquiera
                quoteDao.getRandomQuote()?.toQuote()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene múltiples citas aleatorias
     */
    suspend fun getRandomQuotes(count: Int, excludeIds: List<Long> = emptyList()): List<Quote> {
        return try {
            val quotes = if (excludeIds.isEmpty()) {
                quoteDao.getRandomQuotes(count)
            } else {
                quoteDao.getRandomQuotesExcluding(excludeIds, count)
            }
            quotes.map { it.toQuote() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Actualizar cita
     */
    suspend fun updateQuote(quote: Quote) {
        quoteDao.updateQuote(quote.toEntity())
    }

    // ========== FAVORITOS ==========
    
    fun getFavoriteQuotes(): Flow<List<Quote>> {
        return quoteDao.getFavoriteQuotes().map { entities ->
            entities.map { it.toQuote() }
        }
    }
    
    suspend fun toggleFavorite(quote: Quote) {
        val entity = quote.copy(isFavorite = !quote.isFavorite).toEntity()
        quoteDao.updateQuote(entity)
    }

    // ========== CATEGORÍAS ==========
    
    fun getQuotesByCategory(category: String): Flow<List<Quote>> {
        return quoteDao.getQuotesByCategory(category).map { entities ->
            entities.map { it.toQuote() }
        }
    }
    
    suspend fun getAllCategories(): List<String> {
        return quoteDao.getAllCategories()
    }

    // ========== BÚSQUEDA ==========

    suspend fun searchQuotes(query: String): List<Quote> {
        return quoteDao.searchQuotes("%$query%").map { it.toQuote() }
    }

    // ========== ESTADÍSTICAS ==========

    suspend fun getQuotesCount(): Int = quoteDao.getQuotesCount()
    suspend fun getFavoritesCount(): Int = quoteDao.getFavoritesCount()

    // ========== SINCRONIZACIÓN ==========
    
    /**
     * Fuerza sincronización con Google Sheets
     */
    suspend fun forceSyncWithGoogleSheets(): SyncResult {
        return googleSheetsRepository.syncQuotes(forceUpdate = true)
    }
    
    /**
     * Verifica conectividad con Google Sheets
     */
    suspend fun checkGoogleSheetsConnectivity(): Boolean {
        return googleSheetsRepository.checkConnectivity()
    }
    
    /**
     * Obtiene estadísticas de la base de datos
     */
    suspend fun getDatabaseStats(): DatabaseStats {
        return DatabaseStats(
            totalQuotes = quoteDao.getQuotesCount(),
            favoriteQuotes = quoteDao.getFavoritesCount(),
            categoriesCount = quoteDao.getAllCategories().size,
            quotesShown = quoteDao.getShownQuotesCount(),
            lastSyncDate = "Implementar en preferencias" // TODO: Agregar DataStore
        )
    }

    // ========== FUNCIONES DE MANTENIMIENTO ==========

    suspend fun resetTodayQuotes() {
        val today = dateFormat.format(Date())
        quoteDao.clearTodayQuotes(today)
    }

    suspend fun resetAllDatesShown() {
        quoteDao.resetAllDatesShown()
    }

    // ========== CONVERSIONES ==========

    private fun Quote.toEntity(): QuoteEntity {
        return QuoteEntity(
            id = this.id,
            text = this.text,
            author = this.author,
            category = this.category,
            isFavorite = this.isFavorite,
            dateShown = this.dateShown
        )
    }

    private fun QuoteEntity.toQuote(): Quote {
        return Quote(
            id = this.id,
            text = this.text,
            author = this.author,
            category = this.category,
            isFavorite = this.isFavorite,
            dateShown = this.dateShown
        )
    }

    // ========== CITAS PREDETERMINADAS ==========

    private fun getDefaultQuotes(): List<Quote> {
        return listOf(
            Quote(text = "El único modo de hacer un gran trabajo es amar lo que haces.", author = "Steve Jobs", category = "Motivación"),
            Quote(text = "La vida es lo que te sucede mientras estás ocupado haciendo otros planes.", author = "John Lennon", category = "Vida"),
            Quote(text = "El futuro pertenece a aquellos que creen en la belleza de sus sueños.", author = "Eleanor Roosevelt", category = "Sueños"),
            Quote(text = "No es la especie más fuerte la que sobrevive, sino la que mejor se adapta al cambio.", author = "Charles Darwin", category = "Perseverancia"),
            Quote(text = "La educación es el arma más poderosa que puedes usar para cambiar el mundo.", author = "Nelson Mandela", category = "Educación"),
            Quote(text = "La creatividad es la inteligencia divirtiéndose.", author = "Albert Einstein", category = "Creatividad"),
            Quote(text = "El éxito es ir de fracaso en fracaso sin perder el entusiasmo.", author = "Winston Churchill", category = "Éxito"),
            Quote(text = "Sé tú mismo; todos los demás ya están ocupados.", author = "Oscar Wilde", category = "Autenticidad"),
            Quote(text = "La felicidad no es algo hecho. Viene de tus propias acciones.", author = "Dalai Lama", category = "Felicidad"),
            Quote(text = "La sabiduría comienza en la reflexión.", author = "Sócrates", category = "Sabiduría"),
            Quote(text = "Cree en ti mismo y todo será posible.", author = "Anónimo", category = "Confianza"),
            Quote(text = "El progreso es imposible sin cambio.", author = "George Bernard Shaw", category = "Progreso"),
            Quote(text = "La excelencia no es una habilidad, es una actitud.", author = "Ralph Marston", category = "Excelencia"),
            Quote(text = "Un viaje de mil millas comienza con un solo paso.", author = "Lao Tzu", category = "Acción"),
            Quote(text = "Lo que no te mata, te hace más fuerte.", author = "Friedrich Nietzsche", category = "Perseverancia"),
            Quote(text = "La manera de empezar es dejar de hablar y empezar a hacer.", author = "Walt Disney", category = "Acción"),
            Quote(text = "La imaginación es más importante que el conocimiento.", author = "Albert Einstein", category = "Creatividad"),
            Quote(text = "Nunca es tarde para ser lo que podrías haber sido.", author = "George Eliot", category = "Sueños"),
            Quote(text = "El fracaso es simplemente la oportunidad de comenzar de nuevo de manera más inteligente.", author = "Henry Ford", category = "Perseverancia"),
            Quote(text = "La vida es 10% lo que te sucede y 90% cómo reaccionas a ello.", author = "Charles R. Swindoll", category = "Vida")
        )
    }
}

/**
 * Estadísticas de la base de datos
 */
data class DatabaseStats(
    val totalQuotes: Int,
    val favoriteQuotes: Int,
    val categoriesCount: Int,
    val quotesShown: Int,
    val lastSyncDate: String
)
