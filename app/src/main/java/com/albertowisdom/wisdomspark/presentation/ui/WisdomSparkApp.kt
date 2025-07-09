package com.albertowisdom.wisdomspark.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.presentation.navigation.WisdomNavHost
import com.albertowisdom.wisdomspark.presentation.ui.components.EnhancedBottomNavigation

/**
 * Composable principal de la app - versión simplificada sin insets
 */
@Composable
fun WisdomSparkApp(
    adMobManager: AdMobManager,
    userPreferences: UserPreferences
) {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            EnhancedBottomNavigation(
                navController = navController
            )
        }
    ) { paddingValues ->
        WisdomNavHost(
            navController = navController,
            adMobManager = adMobManager,
            userPreferences = userPreferences,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
