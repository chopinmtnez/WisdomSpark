package com.albertowisdom.wisdomspark.data.remote

import com.albertowisdom.wisdomspark.data.remote.dto.GoogleSheetsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API Interface para Google Sheets
 * Permite sincronizar citas y categorías desde una hoja de cálculo pública
 */
interface GoogleSheetsApi {
    
    companion object {
        const val BASE_URL = "https://sheets.googleapis.com/v4/spreadsheets/"
        
        // IDs y rangos por defecto
        const val DEFAULT_API_KEY = "AIzaSyBDNXIp1HhdJhfIycQX6kFPhiS5G0MlyBc"
        const val DEFAULT_SPREADSHEET_ID = "1cQZNUZ0CmlC8kUuu3X69E1n0dea48N1kTwMzJhWu-fA"
        const val QUOTES_RANGE = "Quotes!A2:F" // A partir de fila 2 (sin headers)
        const val CATEGORIES_RANGE = "Categories!A2:E" // A partir de fila 2
        
        // Para desarrollo/testing (hojas públicas de ejemplo)
        const val TEST_SPREADSHEET_ID = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
        const val TEST_QUOTES_RANGE = "Citas!A2:F"
    }
    
    /**
     * Obtiene citas desde Google Sheets
     * @param spreadsheetId ID de la hoja de cálculo
     * @param range Rango de celdas (ej: "Citas!A2:F")
     * @param apiKey Clave de API de Google Sheets
     */
    @GET("{spreadsheetId}/values/{range}")
    suspend fun getQuotes(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("key") apiKey: String
    ): Response<GoogleSheetsResponse>
    
    /**
     * Obtiene categorías desde Google Sheets
     */
    @GET("{spreadsheetId}/values/{range}")  
    suspend fun getCategories(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("key") apiKey: String
    ): Response<GoogleSheetsResponse>
    
    /**
     * Obtiene metadatos de la hoja de cálculo
     */
    @GET("{spreadsheetId}")
    suspend fun getSpreadsheetInfo(
        @Path("spreadsheetId") spreadsheetId: String,
        @Query("key") apiKey: String,
        @Query("fields") fields: String = "properties.title,sheets.properties"
    ): Response<Map<String, Any>>
    
    /**
     * Verifica conectividad con una hoja específica
     */
    @GET("{spreadsheetId}/values/A1:A1")
    suspend fun ping(
        @Path("spreadsheetId") spreadsheetId: String,
        @Query("key") apiKey: String
    ): Response<GoogleSheetsResponse>
}
