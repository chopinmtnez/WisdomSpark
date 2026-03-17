package com.albertowisdom.wisdomspark.data.repository

import android.util.Log
import com.albertowisdom.wisdomspark.data.local.database.dao.QuoteDao
import com.albertowisdom.wisdomspark.data.local.database.entities.QuoteEntity
import com.albertowisdom.wisdomspark.data.local.database.entities.toEntity
import com.albertowisdom.wisdomspark.data.local.database.entities.toQuote
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.remote.repository.GoogleSheetsRepository
import com.albertowisdom.wisdomspark.data.remote.repository.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepository @Inject constructor(
    private val quoteDao: QuoteDao,
    private val googleSheetsRepository: GoogleSheetsRepository,
    private val userPreferences: UserPreferences
) {
    companion object {
        private const val TAG = "QuoteRepository"
    }

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // Thread-safe
    private val syncMutex = Mutex()
    
    // ========== INICIALIZACIÓN ==========
    
    /**
     * Inicializa la base de datos con citas
     * Primero intenta sincronizar desde Google Sheets, si falla usa citas locales
     */
    suspend fun initializeQuotes(forceSync: Boolean = false): SyncResult {
        try {
            val localQuotesCount = quoteDao.getQuotesCount()
            
            // Verificar si necesita limpieza automática por problemas de idioma
            val needsLanguageCleanup = checkIfNeedsLanguageCleanup()
            
            if (localQuotesCount == 0 || forceSync || needsLanguageCleanup) {
                if (needsLanguageCleanup) {
                    Log.d(TAG,"🔄 Detectados problemas de idioma, limpiando base de datos...")
                    quoteDao.deleteAllQuotes()
                }
                
                // Intentar sincronizar con Google Sheets usando el idioma actual
                val currentLanguage = userPreferences.appLanguage.first()
                val syncResult = googleSheetsRepository.syncQuotes(forceUpdate = true, language = currentLanguage)
                
                if (syncResult is SyncResult.Success) {
                    Log.d(TAG,"✓ Inicialización exitosa: Sincronización exitosa (${syncResult.quotesCount} citas)")
                    return syncResult
                }
                
                // Fallback: usar citas locales predeterminadas
                initializeDefaultQuotes()
                Log.d(TAG,"✓ Inicialización exitosa: Citas locales inicializadas (${getDefaultQuotes().size} citas)")
                return SyncResult.Success(
                    message = "Citas locales inicializadas",
                    quotesCount = getDefaultQuotes().size
                )
            } else {
                Log.d(TAG,"✓ Base de datos ya inicializada ($localQuotesCount citas disponibles)")
                return SyncResult.Success(
                    message = "Base de datos ya inicializada",
                    quotesCount = localQuotesCount
                )
            }
        } catch (e: Exception) {
            // En caso de error, usar citas predeterminadas
            initializeDefaultQuotes()
            Log.d(TAG,"⚠ Error en inicialización, usando datos locales: ${e.message}")
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
    
    /**
     * Verifica si la base de datos necesita limpieza por problemas de idioma
     */
    private suspend fun checkIfNeedsLanguageCleanup(): Boolean {
        try {
            val totalQuotes = quoteDao.getQuotesCount()
            if (totalQuotes == 0) return false
            
            // Verificar si hay citas en la categoría "Liderazgo" 
            val leadershipQuotes = quoteDao.getQuotesByCategorySync("Liderazgo")
            
            // Si hay citas de liderazgo pero ninguna tiene idioma "es", necesita limpieza
            if (leadershipQuotes.isNotEmpty()) {
                val spanishLeadershipQuotes = leadershipQuotes.filter { it.language == "es" }
                
                // Si menos del 50% de las citas de liderazgo están en español, limpiar
                if (spanishLeadershipQuotes.size < leadershipQuotes.size * 0.5) {
                    Log.d(TAG,"🔍 Detectadas ${leadershipQuotes.size - spanishLeadershipQuotes.size} citas de liderazgo sin idioma correcto")
                    return true
                }
            }
            
            return false
        } catch (e: Exception) {
            Log.d(TAG,"⚠ Error verificando idiomas: ${e.message}")
            return false
        }
    }
    
    // ========== CITAS DIARIAS ==========

    suspend fun getOrCreateTodayQuote(language: String = "es"): Quote {
        val today = LocalDate.now().format(dateFormatter)
        
        // Buscar cita existente para hoy
        val existingQuote = quoteDao.getTodayQuote(today)
        if (existingQuote != null) return existingQuote.toQuote()
        
        // Si no existe, asignar una aleatoria no mostrada del idioma específico
        val randomQuoteEntity = quoteDao.getRandomUnshownQuoteByLanguage(language) ?: run {
            // Si no hay citas del idioma específico, usar cualquier idioma
            quoteDao.getRandomUnshownQuote() ?: run {
                // Si todas las citas han sido mostradas, reset y tomar una aleatoria
                quoteDao.resetAllDatesShown()
                quoteDao.getRandomUnshownQuoteByLanguage(language) ?: 
                quoteDao.getRandomUnshownQuote() ?: getDefaultQuotes().first().toEntity()
            }
        }
        
        val updatedQuote = randomQuoteEntity.copy(dateShown = today)
        quoteDao.updateQuote(updatedQuote)
        
        return updatedQuote.toQuote()
    }

    // ========== MÉTODOS PARA SWIPE SYSTEM ==========

    /**
     * Obtiene cita aleatoria excluyendo IDs específicos
     */
    suspend fun getRandomQuoteExcluding(excludeIds: List<Long>, language: String = "es"): Quote? {
        return try {
            val availableQuote = if (excludeIds.isEmpty()) {
                quoteDao.getRandomQuoteByLanguage(language) ?: quoteDao.getRandomQuote()
            } else {
                quoteDao.getRandomQuoteExcludingByLanguage(excludeIds, language) ?: 
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
    suspend fun getRandomQuotes(count: Int, excludeIds: List<Long> = emptyList(), language: String = "es"): List<Quote> {
        return try {
            val quotes = if (excludeIds.isEmpty()) {
                quoteDao.getRandomQuotesByLanguage(language, count).takeIf { it.isNotEmpty() } ?: 
                quoteDao.getRandomQuotes(count)
            } else {
                quoteDao.getRandomQuotesExcludingByLanguage(excludeIds, language, count).takeIf { it.isNotEmpty() } ?: 
                quoteDao.getRandomQuotesExcluding(excludeIds, count)
            }
            quotes.map { it.toQuote() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Actualizar cita con validaciones de seguridad
     */
    suspend fun updateQuote(quote: Quote) {
        val sanitizedQuote = sanitizeQuote(quote)
        quoteDao.updateQuote(sanitizedQuote.toEntity())
    }
    
    /**
     * Insertar cita con validaciones de seguridad
     */
    suspend fun insertQuote(quote: Quote) {
        // Validaciones de seguridad antes de insertar
        val sanitizedQuote = sanitizeQuote(quote)
        quoteDao.insertQuote(sanitizedQuote.toEntity())
    }
    
    /**
     * Insertar o actualizar cita (usa REPLACE de Room) con validaciones
     */
    suspend fun insertOrUpdateQuote(quote: Quote) {
        val sanitizedQuote = sanitizeQuote(quote)
        quoteDao.insertQuote(sanitizedQuote.toEntity()) // Room ya maneja INSERT OR REPLACE
    }
    
    /**
     * Obtener cita por ID
     */
    suspend fun getQuoteById(id: Long): Quote? {
        return quoteDao.getQuoteById(id)?.toQuote()
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

    suspend fun getFavoriteCount(): Int = quoteDao.getFavoriteQuotesCount()

    // ========== CATEGORÍAS ==========
    
    fun getQuotesByCategory(category: String, language: String = "es"): Flow<List<Quote>> {
        Log.d(TAG,"🔄 QuoteRepository: Consultando citas de categoría '$category' en idioma '$language'")
        return quoteDao.getQuotesByCategoryAndLanguage(category, language).map { entities ->
            Log.d(TAG,"🔄 QuoteRepository: Encontradas ${entities.size} citas para '$category' en '$language'")
            entities.map { it.toQuote() }
        }
    }
    
    suspend fun getAllCategories(): List<String> {
        return quoteDao.getAllCategories()
    }
    
    /**
     * Obtener todas las categorías disponibles filtradas por idioma
     */
    suspend fun getAllCategoriesByLanguage(language: String): List<String> {
        return quoteDao.getAllCategoriesByLanguage(language)
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
        val currentLanguage = userPreferences.appLanguage.first()
        return googleSheetsRepository.syncQuotes(forceUpdate = true, language = currentLanguage)
    }
    
    /**
     * Limpia la base de datos y fuerza una nueva sincronización
     * Útil para resolver problemas de idioma en datos existentes
     */
    suspend fun cleanAndResyncDatabase(): SyncResult {
        try {
            // Limpiar toda la base de datos
            quoteDao.deleteAllQuotes()
            
            // Forzar nueva sincronización usando el idioma actual
            val currentLanguage = userPreferences.appLanguage.first()
            val syncResult = googleSheetsRepository.syncQuotes(forceUpdate = true, language = currentLanguage)
            
            // Si falla la sincronización, restaurar datos predeterminados
            if (syncResult is SyncResult.Error) {
                initializeDefaultQuotes()
                return SyncResult.Success(
                    message = "Base de datos limpiada y restaurada con datos locales",
                    quotesCount = getDefaultQuotes().size
                )
            }
            
            return syncResult
        } catch (e: Exception) {
            // En caso de error, restaurar datos predeterminados
            initializeDefaultQuotes()
            return SyncResult.Success(
                message = "Error durante limpieza, restaurado con datos locales",
                quotesCount = getDefaultQuotes().size
            )
        }
    }
    
    /**
     * Sincroniza la base de datos preservando los favoritos del usuario
     * Usado específicamente cuando cambia el idioma
     * PROTEGIDO CON MUTEX para evitar operaciones concurrentes
     */
    suspend fun syncDatabasePreservingFavorites(language: String): SyncResult {
        return syncMutex.withLock {
            try {
                Log.d(TAG, "Database sync locked for language: $language")

                // Guardar favoritas con tipos seguros (no string concatenation con pipe)
                data class FavoriteId(val text: String, val author: String)
                val currentFavorites = quoteDao.getFavoriteQuotes().first().map { quote ->
                    FavoriteId(text = quote.text, author = quote.author)
                }

                Log.d(TAG, "Sincronizando preservando ${currentFavorites.size} favoritos")

                quoteDao.deleteAllQuotes()

                val syncResult = googleSheetsRepository.syncQuotes(forceUpdate = true, language = language)

                if (syncResult is SyncResult.Success) {
                    quoteDao.removeDuplicateQuotes()

                    // Restaurar favoritos con datos tipados
                    var favoritesRestored = 0
                    currentFavorites.forEach { fav ->
                        val matchingQuote = quoteDao.findDuplicateQuote(fav.text, fav.author)
                        if (matchingQuote != null) {
                            quoteDao.updateQuote(matchingQuote.copy(isFavorite = true))
                            favoritesRestored++
                        }
                    }
                    Log.d(TAG, "$favoritesRestored/${currentFavorites.size} favoritos restaurados")
                }
                
                // Si falla la sincronización, restaurar datos predeterminados
                if (syncResult is SyncResult.Error) {
                    Log.d(TAG,"⚠ Sincronización falló, restaurando datos locales")
                    initializeDefaultQuotes()
                    return SyncResult.Success(
                        message = "Base de datos limpiada y restaurada con datos locales",
                        quotesCount = getDefaultQuotes().size
                    )
                }
                
                Log.d(TAG,"🔓 QuoteRepository: Database sync unlocked")
                return syncResult
                
            } catch (e: Exception) {
                Log.d(TAG,"❌ Error en syncDatabasePreservingFavorites: ${e.message}")
                e.printStackTrace()
                // En caso de error, restaurar datos predeterminados
                initializeDefaultQuotes()
                return SyncResult.Success(
                    message = "Error durante sincronización, restaurado con datos locales: ${e.message}",
                    quotesCount = getDefaultQuotes().size
                )
            }
        }
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
        val today = LocalDate.now().format(dateFormatter)
        quoteDao.clearTodayQuotes(today)
    }

    suspend fun resetAllDatesShown() {
        quoteDao.resetAllDatesShown()
    }
    
    /**
     * Actualiza el idioma de todas las citas existentes
     * Útil para migrar datos antiguos
     */
    suspend fun updateAllQuotesLanguage(language: String = "es") {
        try {
            val allQuotes = quoteDao.getAllQuotesSync()
            allQuotes.forEach { quote ->
                val updatedQuote = quote.copy(language = language)
                quoteDao.updateQuote(updatedQuote)
            }
        } catch (e: Exception) {
            // Log error but don't throw to avoid breaking the app
            Log.d(TAG,"Error updating quotes language: ${e.message}")
        }
    }

    // ========== VALIDACIONES DE SEGURIDAD ==========
    
    /**
     * Sanitiza una cita para prevenir problemas de seguridad
     */
    private fun sanitizeQuote(quote: Quote): Quote {
        return quote.copy(
            text = sanitizeText(quote.text),
            author = sanitizeText(quote.author),
            category = sanitizeText(quote.category),
            language = sanitizeLanguageCode(quote.language)
        )
    }
    
    /**
     * Sanitiza texto para prevenir inyección y caracteres peligrosos
     */
    private fun sanitizeText(input: String): String {
        return input
            .trim()
            .take(500)
            .replace(Regex("[<>]"), "") // Solo remover etiquetas HTML (Room usa queries parametrizadas)
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    /**
     * Valida y sanitiza códigos de idioma
     */
    private fun sanitizeLanguageCode(language: String): String {
        val validLanguages = setOf("es", "en", "fr", "de", "pt", "it")
        val sanitized = language.lowercase().trim().take(2)
        return if (sanitized in validLanguages) sanitized else "es"
    }

    // ========== CITAS PREDETERMINADAS ==========

    private fun getDefaultQuotes(): List<Quote> {
        return listOf(
            Quote(text = "El único modo de hacer un gran trabajo es amar lo que haces.", author = "Steve Jobs", category = "Motivación", language = "es"),
            Quote(text = "La vida es lo que te sucede mientras estás ocupado haciendo otros planes.", author = "John Lennon", category = "Vida", language = "es"),
            Quote(text = "El futuro pertenece a aquellos que creen en la belleza de sus sueños.", author = "Eleanor Roosevelt", category = "Sueños", language = "es"),
            Quote(text = "No es la especie más fuerte la que sobrevive, sino la que mejor se adapta al cambio.", author = "Charles Darwin", category = "Perseverancia", language = "es"),
            Quote(text = "La educación es el arma más poderosa que puedes usar para cambiar el mundo.", author = "Nelson Mandela", category = "Educación", language = "es"),
            Quote(text = "La creatividad es la inteligencia divirtiéndose.", author = "Albert Einstein", category = "Creatividad", language = "es"),
            Quote(text = "El éxito es ir de fracaso en fracaso sin perder el entusiasmo.", author = "Winston Churchill", category = "Éxito", language = "es"),
            Quote(text = "Sé tú mismo; todos los demás ya están ocupados.", author = "Oscar Wilde", category = "Autenticidad", language = "es"),
            Quote(text = "La felicidad no es algo hecho. Viene de tus propias acciones.", author = "Dalai Lama", category = "Felicidad", language = "es"),
            Quote(text = "La sabiduría comienza en la reflexión.", author = "Sócrates", category = "Sabiduría", language = "es"),
            Quote(text = "Cree en ti mismo y todo será posible.", author = "Anónimo", category = "Confianza", language = "es"),
            Quote(text = "El progreso es imposible sin cambio.", author = "George Bernard Shaw", category = "Progreso", language = "es"),
            Quote(text = "La excelencia no es una habilidad, es una actitud.", author = "Ralph Marston", category = "Excelencia", language = "es"),
            Quote(text = "Un viaje de mil millas comienza con un solo paso.", author = "Lao Tzu", category = "Acción", language = "es"),
            Quote(text = "Lo que no te mata, te hace más fuerte.", author = "Friedrich Nietzsche", category = "Perseverancia", language = "es"),
            Quote(text = "La manera de empezar es dejar de hablar y empezar a hacer.", author = "Walt Disney", category = "Acción", language = "es"),
            Quote(text = "La imaginación es más importante que el conocimiento.", author = "Albert Einstein", category = "Creatividad", language = "es"),
            Quote(text = "Nunca es tarde para ser lo que podrías haber sido.", author = "George Eliot", category = "Sueños", language = "es"),
            Quote(text = "El fracaso es simplemente la oportunidad de comenzar de nuevo de manera más inteligente.", author = "Henry Ford", category = "Perseverancia", language = "es"),
            Quote(text = "La vida es 10% lo que te sucede y 90% cómo reaccionas a ello.", author = "Charles R. Swindoll", category = "Vida", language = "es"),
            
            // Citas de Liderazgo en Español
            Quote(text = "Un líder es alguien que conoce el camino, anda el camino y muestra el camino.", author = "John C. Maxwell", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo es la capacidad de traducir la visión en realidad.", author = "Warren Bennis", category = "Liderazgo", language = "es"),
            Quote(text = "No puedes liderar a otros si no puedes liderarte a ti mismo.", author = "John C. Maxwell", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo no es sobre estar a cargo. Es sobre cuidar de aquellos a tu cargo.", author = "Simon Sinek", category = "Liderazgo", language = "es"),
            Quote(text = "Un verdadero líder tiene la confianza de estar solo, el coraje de tomar decisiones difíciles y la compasión de escuchar las necesidades de los demás.", author = "Douglas MacArthur", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo es el arte de conseguir que otra persona haga algo que tú quieres que haga porque ella quiere hacerlo.", author = "Dwight D. Eisenhower", category = "Liderazgo", language = "es"),
            Quote(text = "La función del liderazgo es producir más líderes, no más seguidores.", author = "Ralph Nader", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo es influencia, nada más, nada menos.", author = "John C. Maxwell", category = "Liderazgo", language = "es"),
            Quote(text = "Un líder es mejor cuando la gente apenas sabe que existe.", author = "Lao Tzu", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo es la capacidad de hacer que la gente ordinaria haga cosas extraordinarias.", author = "Alan Keith", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo no es un derecho, es una responsabilidad.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "Un líder sin visión y pasión es sólo un jefe.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo es servicio, no ego.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "Los líderes nacen de la determinación, no de las circunstancias.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo es la capacidad de conectar con la gente y crear valor.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "Un líder efectivo es aquel que puede hacer que otros hagan lo que no quieren hacer y que les guste.", author = "Harry S. Truman", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo es el proceso de influir en otros para lograr objetivos importantes.", author = "Gary Yukl", category = "Liderazgo", language = "es"),
            Quote(text = "Un líder es alguien que puede ver el potencial en otros y ayudarlos a desarrollarlo.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo es la capacidad de inspirar a otros a alcanzar su máximo potencial.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "Los grandes líderes no buscan el poder, buscan el propósito.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo es la capacidad de tomar decisiones difíciles cuando nadie más quiere hacerlo.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "Un líder auténtico es aquel que se mantiene fiel a sus valores incluso bajo presión.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo es la capacidad de crear un ambiente donde otros puedan brillar.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "Los líderes no crean seguidores, crean más líderes.", author = "Tom Peters", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo es la capacidad de comunicar una visión de manera que otros se sientan inspirados a seguirla.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "Un líder es alguien que sabe cómo escuchar antes de hablar.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo es la capacidad de mantener la calma en medio de la tormenta.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "Los líderes exitosos son aquellos que pueden adaptarse al cambio y guiar a otros a través de él.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "El liderazgo es la capacidad de hacer que otros se sientan importantes y valorados.", author = "Anónimo", category = "Liderazgo", language = "es"),
            Quote(text = "Un líder verdadero es aquel que puede admitir sus errores y aprender de ellos.", author = "Anónimo", category = "Liderazgo", language = "es"),
            
            // English Quotes
            Quote(text = "Life is what happens to you while you're busy making other plans.", author = "John Lennon", category = "Life", language = "en"),
            Quote(text = "Wisdom begins in wonder.", author = "Socrates", category = "Wisdom", language = "en"),
            Quote(text = "Believe in yourself and all that you are.", author = "Anonymous", category = "Confidence", language = "en"),
            Quote(text = "Progress is impossible without change.", author = "George Bernard Shaw", category = "Progress", language = "en"),
            Quote(text = "Excellence is not a skill, it's an attitude.", author = "Ralph Marston", category = "Excellence", language = "en"),
            Quote(text = "A journey of a thousand miles begins with a single step.", author = "Lao Tzu", category = "Action", language = "en"),
            Quote(text = "What doesn't kill you makes you stronger.", author = "Friedrich Nietzsche", category = "Perseverance", language = "en"),
            Quote(text = "The way to get started is to quit talking and begin doing.", author = "Walt Disney", category = "Action", language = "en"),
            Quote(text = "Imagination is more important than knowledge.", author = "Albert Einstein", category = "Creativity", language = "en"),
            Quote(text = "It's never too late to be what you might have been.", author = "George Eliot", category = "Dreams", language = "en"),
            Quote(text = "Failure is simply the opportunity to begin again, this time more intelligently.", author = "Henry Ford", category = "Perseverance", language = "en"),
            Quote(text = "Life is 10% what happens to you and 90% how you react to it.", author = "Charles R. Swindoll", category = "Life", language = "en"),
            
            // English Leadership Quotes
            Quote(text = "A leader is one who knows the way, goes the way, and shows the way.", author = "John C. Maxwell", category = "Leadership", language = "en"),
            Quote(text = "Leadership is the capacity to translate vision into reality.", author = "Warren Bennis", category = "Leadership", language = "en"),
            Quote(text = "You can't lead others if you can't lead yourself.", author = "John C. Maxwell", category = "Leadership", language = "en"),
            Quote(text = "Leadership is not about being in charge. It's about taking care of those in your charge.", author = "Simon Sinek", category = "Leadership", language = "en"),
            Quote(text = "A true leader has the confidence to stand alone, the courage to make tough decisions, and the compassion to listen to the needs of others.", author = "Douglas MacArthur", category = "Leadership", language = "en"),
            Quote(text = "Leadership is the art of getting someone else to do something you want done because he wants to do it.", author = "Dwight D. Eisenhower", category = "Leadership", language = "en"),
            Quote(text = "The function of leadership is to produce more leaders, not more followers.", author = "Ralph Nader", category = "Leadership", language = "en"),
            Quote(text = "Leadership is influence, nothing more, nothing less.", author = "John C. Maxwell", category = "Leadership", language = "en"),
            Quote(text = "A leader is best when people barely know he exists.", author = "Lao Tzu", category = "Leadership", language = "en"),
            Quote(text = "Leadership is the capacity to enable ordinary people to do extraordinary things.", author = "Alan Keith", category = "Leadership", language = "en"),
            
            // French Quotes
            Quote(text = "La vie est ce qui vous arrive pendant que vous êtes occupé à faire d'autres projets.", author = "John Lennon", category = "Vie", language = "fr"),
            Quote(text = "La sagesse commence dans l'émerveillement.", author = "Socrate", category = "Sagesse", language = "fr"),
            Quote(text = "Croyez en vous et en tout ce que vous êtes.", author = "Anonyme", category = "Confiance", language = "fr"),
            Quote(text = "Le progrès est impossible sans changement.", author = "George Bernard Shaw", category = "Progrès", language = "fr"),
            Quote(text = "L'excellence n'est pas une compétence, c'est une attitude.", author = "Ralph Marston", category = "Excellence", language = "fr"),
            
            // French Leadership Quotes
            Quote(text = "Un leader est quelqu'un qui connaît le chemin, suit le chemin et montre le chemin.", author = "John C. Maxwell", category = "Leadership", language = "fr"),
            Quote(text = "Le leadership est la capacité de traduire la vision en réalité.", author = "Warren Bennis", category = "Leadership", language = "fr"),
            Quote(text = "Vous ne pouvez pas diriger les autres si vous ne pouvez pas vous diriger vous-même.", author = "John C. Maxwell", category = "Leadership", language = "fr"),
            
            // German Quotes
            Quote(text = "Das Leben ist das, was dir passiert, während du eifrig dabei bist, andere Pläne zu machen.", author = "John Lennon", category = "Leben", language = "de"),
            Quote(text = "Weisheit beginnt im Staunen.", author = "Sokrates", category = "Weisheit", language = "de"),
            Quote(text = "Glaube an dich selbst und an alles, was du bist.", author = "Anonym", category = "Vertrauen", language = "de"),
            
            // German Leadership Quotes
            Quote(text = "Ein Anführer ist jemand, der den Weg kennt, den Weg geht und den Weg zeigt.", author = "John C. Maxwell", category = "Führung", language = "de"),
            Quote(text = "Führung ist die Fähigkeit, Vision in Realität zu übersetzen.", author = "Warren Bennis", category = "Führung", language = "de"),
            
            // Portuguese Quotes
            Quote(text = "A vida é o que acontece com você enquanto você está ocupado fazendo outros planos.", author = "John Lennon", category = "Vida", language = "pt"),
            Quote(text = "A sabedoria começa na admiração.", author = "Sócrates", category = "Sabedoria", language = "pt"),
            Quote(text = "Acredite em si mesmo e em tudo o que você é.", author = "Anônimo", category = "Confiança", language = "pt"),
            
            // Portuguese Leadership Quotes
            Quote(text = "Um líder é alguém que conhece o caminho, percorre o caminho e mostra o caminho.", author = "John C. Maxwell", category = "Liderança", language = "pt"),
            Quote(text = "Liderança é a capacidade de traduzir visão em realidade.", author = "Warren Bennis", category = "Liderança", language = "pt"),
            
            // Italian Quotes
            Quote(text = "La vita è quello che ti succede mentre sei impegnato a fare altri piani.", author = "John Lennon", category = "Vita", language = "it"),
            Quote(text = "La saggezza inizia nella meraviglia.", author = "Socrate", category = "Saggezza", language = "it"),
            Quote(text = "Credi in te stesso e in tutto ciò che sei.", author = "Anonimo", category = "Fiducia", language = "it"),
            
            // Italian Leadership Quotes
            Quote(text = "Un leader è qualcuno che conosce la strada, percorre la strada e mostra la strada.", author = "John C. Maxwell", category = "Leadership", language = "it"),
            Quote(text = "La leadership è la capacità di tradurre la visione in realtà.", author = "Warren Bennis", category = "Leadership", language = "it")
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
