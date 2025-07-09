package com.albertowisdom.wisdomspark.data.models

data class Quote(
    val id: Long = 0,
    val text: String,
    val author: String,
    val category: String,
    val isFavorite: Boolean = false,
    val dateShown: String? = null
)
