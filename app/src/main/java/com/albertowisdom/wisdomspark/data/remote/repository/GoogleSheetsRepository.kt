package com.albertowisdom.wisdomspark.data.remote.repository

import android.util.Log
import com.albertowisdom.wisdomspark.data.local.database.dao.QuoteDao
import com.albertowisdom.wisdomspark.data.local.database.entities.QuoteEntity
import com.albertowisdom.wisdomspark.data.remote.GoogleSheetsApi
import com.albertowisdom.wisdomspark.data.remote.dto.QuoteDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para sincronización con Google Sheets
 * Maneja la descarga y sincronización de citas desde hojas de cálculo
 */
@Singleton
class GoogleSheetsRepository @Inject constructor(
    private val api: GoogleSheetsApi,
    private val quoteDao: QuoteDao
) {
    
    companion object {
        private const val TAG = "GoogleSheetsRepository"
        
        // TODO: Configurar con tus propias credenciales
        // Obtén tu API key en: https://console.cloud.google.com/apis/credentials
        const val DEFAULT_API_KEY = "YOUR_GOOGLE_SHEETS_API_KEY_HERE"
        const val DEFAULT_SPREADSHEET_ID = "YOUR_SPREADSHEET_ID_HERE"
        
        // Para testing (usar hojas públicas de Google)
        const val TEST_API_KEY = "AIzaSyBe1D-AsJPPsqr15sXo9tVQKxNLEEHfnBs" // Ejemplo público
        const val TEST_SPREADSHEET_ID = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
    }
    
    /**
     * Sincroniza citas desde Google Sheets
     * @param apiKey Clave de API (opcional, usa default si no se proporciona)
     * @param spreadsheetId ID de la hoja (opcional, usa default si no se proporciona) 
     * @param forceUpdate Fuerza actualización aunque ya existan datos
     * @return Resultado de la sincronización
     */
    suspend fun syncQuotes(
        apiKey: String = DEFAULT_API_KEY,
        spreadsheetId: String = DEFAULT_SPREADSHEET_ID,
        forceUpdate: Boolean = false
    ): SyncResult = withContext(Dispatchers.IO) {
        
        try {
            Log.d(TAG, "Iniciando sincronización de citas...")
            
            // Verificar si ya hay datos y no se fuerza actualización
            if (!forceUpdate && quoteDao.getQuotesCount() > 0) {
                Log.d(TAG, "Ya existen citas locales, saltando sincronización")
                return@withContext SyncResult.Success(
                    message = "Citas locales ya disponibles",
                    quotesCount = quoteDao.getQuotesCount()
                )
            }
            
            // Verificar conectividad
            val pingResponse = api.ping(spreadsheetId, apiKey)
            if (!pingResponse.isSuccessful) {
                Log.e(TAG, "Error de conectividad: ${pingResponse.code()}")
                return@withContext SyncResult.Error("Error de conectividad con Google Sheets")
            }
            
            // Obtener citas desde Google Sheets
            val response = api.getQuotes(
                spreadsheetId = spreadsheetId,
                range = GoogleSheetsApi.QUOTES_RANGE,
                apiKey = apiKey
            )
            
            if (response.isSuccessful) {
                val sheetsResponse = response.body()
                if (sheetsResponse?.values != null) {
                    
                    // Mapear filas de Google Sheets a QuoteDto
                    val quoteDtos = sheetsResponse.values.mapNotNull { row ->
                        QuoteDto.fromSheetRow(row)
                    }.filter { it.active } // Solo citas activas
                    
                    if (quoteDtos.isNotEmpty()) {
                        // Convertir a entities y guardar
                        val quoteEntities = quoteDtos.map { dto ->
                            QuoteEntity(
                                text = dto.text,
                                author = dto.author,
                                category = dto.category,
                                isFavorite = false, // Por defecto no favorito
                                dateShown = null // Sin asignar fecha
                            )
                        }
                        
                        // Limpiar tabla si es actualización forzada
                        if (forceUpdate) {
                            quoteDao.deleteAllQuotes()
                            Log.d(TAG, "Tabla de citas limpiada para actualización")
                        }
                        
                        // Insertar nuevas citas
                        quoteDao.insertQuotes(quoteEntities)
                        
                        Log.d(TAG, "Sincronización exitosa: ${quoteEntities.size} citas")
                        SyncResult.Success(
                            message = "Sincronización exitosa",
                            quotesCount = quoteEntities.size
                        )
                        
                    } else {
                        Log.w(TAG, "No se encontraron citas válidas en Google Sheets")
                        SyncResult.Error("No se encontraron citas válidas")
                    }
                } else {
                    Log.e(TAG, "Respuesta vacía de Google Sheets")
                    SyncResult.Error("Respuesta vacía de Google Sheets")
                }
            } else {
                val errorMsg = "Error HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, errorMsg)
                SyncResult.Error(errorMsg)
            }
            
        } catch (e: Exception) {
            val errorMsg = "Error durante sincronización: ${e.message}"
            Log.e(TAG, errorMsg, e)
            SyncResult.Error(errorMsg)
        }
    }
    
    /**
     * Verifica el estado de conectividad con Google Sheets
     */
    suspend fun checkConnectivity(
        apiKey: String = DEFAULT_API_KEY,
        spreadsheetId: String = DEFAULT_SPREADSHEET_ID
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.ping(spreadsheetId, apiKey)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error verificando conectividad", e)
            false
        }
    }
    
    /**
     * Obtiene información de la hoja de cálculo
     */
    suspend fun getSpreadsheetInfo(
        apiKey: String = DEFAULT_API_KEY,
        spreadsheetId: String = DEFAULT_SPREADSHEET_ID
    ): SpreadsheetInfo? = withContext(Dispatchers.IO) {
        try {
            val response = api.getSpreadsheetInfo(spreadsheetId, apiKey)
            if (response.isSuccessful) {
                val data = response.body()
                SpreadsheetInfo(
                    title = data?.get("properties")?.let { 
                        (it as? Map<*, *>)?.get("title") as? String 
                    } ?: "Desconocido",
                    url = "https://docs.google.com/spreadsheets/d/$spreadsheetId"
                )
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo info de spreadsheet", e)
            null
        }
    }
}

/**
 * Resultado de operaciones de sincronización
 */
sealed class SyncResult {
    data class Success(
        val message: String,
        val quotesCount: Int
    ) : SyncResult()
    
    data class Error(
        val message: String
    ) : SyncResult()
}

/**
 * Información de la hoja de cálculo
 */
data class SpreadsheetInfo(
    val title: String,
    val url: String
)
