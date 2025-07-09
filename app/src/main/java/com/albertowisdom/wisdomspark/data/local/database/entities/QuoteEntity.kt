package com.albertowisdom.wisdomspark.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.albertowisdom.wisdomspark.data.models.Quote

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val author: String,
    val category: String,
    val isFavorite: Boolean = false,
    val dateShown: String? = null
)

// Extension functions for conversion
fun QuoteEntity.toQuote() = Quote(
    id = id,
    text = text,
    author = author,
    category = category,
    isFavorite = isFavorite,
    dateShown = dateShown
)

fun Quote.toEntity() = QuoteEntity(
    id = id,
    text = text,
    author = author,
    category = category,
    isFavorite = isFavorite,
    dateShown = dateShown
)
