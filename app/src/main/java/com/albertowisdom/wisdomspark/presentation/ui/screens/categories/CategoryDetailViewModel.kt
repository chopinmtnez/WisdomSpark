package com.albertowisdom.wisdomspark.presentation.ui.screens.categories

import androidx.lifecycle.ViewModel
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    val quoteRepository: QuoteRepository
) : ViewModel()