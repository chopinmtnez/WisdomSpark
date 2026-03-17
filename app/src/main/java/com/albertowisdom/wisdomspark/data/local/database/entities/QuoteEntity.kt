package com.albertowisdom.wisdomspark.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.albertowisdom.wisdomspark.data.models.Quote

@Entity(
    tableName = "quotes",
    indices = [
        Index(value = ["category", "language"], name = "idx_category_language"),
        Index(value = ["isFavorite"], name = "idx_is_favorite"),
        Index(value = ["dateShown"], name = "idx_date_shown"),
        Index(value = ["language"], name = "idx_language"),
        Index(value = ["text", "author"], name = "idx_text_author", unique = false)
    ]
)
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val author: String,
    val category: String,
    val language: String = "es",
    val isFavorite: Boolean = false,
    val dateShown: String? = null
)

// Extension functions for conversion
fun QuoteEntity.toQuote() = Quote(
    id = id,
    text = text,
    author = author,
    category = category,
    language = language,
    isFavorite = isFavorite,
    dateShown = dateShown
)

fun Quote.toEntity() = QuoteEntity(
    id = id,
    text = text,
    author = author,
    category = category,
    language = language,
    isFavorite = isFavorite,
    dateShown = dateShown
)
