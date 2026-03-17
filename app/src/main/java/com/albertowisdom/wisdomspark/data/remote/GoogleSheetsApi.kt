package com.albertowisdom.wisdomspark.data.remote

import com.albertowisdom.wisdomspark.BuildConfig
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

        // Credenciales desde BuildConfig (definidas en gradle.properties)
        val DEFAULT_API_KEY: String get() = BuildConfig.SHEETS_API_KEY
        val DEFAULT_SPREADSHEET_ID: String get() = BuildConfig.SHEETS_SPREADSHEET_ID
        const val QUOTES_RANGE = "Quotes!A2:F"
        const val CATEGORIES_RANGE = "Categories!A2:E"
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
