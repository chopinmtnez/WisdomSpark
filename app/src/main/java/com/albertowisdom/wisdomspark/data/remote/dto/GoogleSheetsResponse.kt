package com.albertowisdom.wisdomspark.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response de Google Sheets API para citas
 */
data class GoogleSheetsResponse(
    @SerializedName("range")
    val range: String,
    
    @SerializedName("majorDimension")
    val majorDimension: String,
    
    @SerializedName("values")
    val values: List<List<String>>
)

/**
 * DTO para mapear una fila de Google Sheets a Quote
 */
data class QuoteDto(
    val text: String,
    val author: String,
    val category: String,
    val language: String = "es",
    val tags: String = "",
    val active: Boolean = true
) {
    companion object {
        /**
         * Mapea una fila de Google Sheets (List<String>) a QuoteDto
         * Formato esperado: [text, author, category, language?, tags?, active?]
         */
        fun fromSheetRow(row: List<String>): QuoteDto? {
            if (row.size < 3) return null // Mínimo: text, author, category
            
            return try {
                QuoteDto(
                    text = row.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return null,
                    author = row.getOrNull(2)?.takeIf { it.isNotBlank() } ?: return null,
                    category = row.getOrNull(3)?.takeIf { it.isNotBlank() } ?: return null,
                    language = row.getOrNull(4)?.takeIf { it.isNotBlank() } ?: "es",
                    tags = row.getOrNull(6) ?: "",
                    active = row.getOrNull(5)?.lowercase()?.let {
                        it == "true" || it == "1" || it == "sí" || it == "si" 
                    } ?: true
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * DTO para categorías desde Google Sheets
 */
data class CategoryDto(
    val name: String,
    val emoji: String,
    val description: String = "",
    val color: String = "",
    val active: Boolean = true
) {
    companion object {
        fun fromSheetRow(row: List<String>): CategoryDto? {
            if (row.isEmpty()) return null
            
            return try {
                CategoryDto(
                    name = row.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return null,
                    emoji = row.getOrNull(1) ?: "💫",
                    description = row.getOrNull(2) ?: "",
                    color = row.getOrNull(3) ?: "",
                    active = row.getOrNull(4)?.lowercase()?.let { 
                        it == "true" || it == "1" || it == "sí" || it == "si" 
                    } ?: true
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
