package com.albertowisdom.wisdomspark.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.presentation.navigation.Screen
import com.albertowisdom.wisdomspark.presentation.navigation.WisdomNavHost
import com.albertowisdom.wisdomspark.presentation.ui.components.EnhancedBottomNavigation

/**
 * Composable principal de la app - con soporte para navegación del sistema
 */
@Composable
fun WisdomSparkApp(
    adMobManager: AdMobManager,
    userPreferences: UserPreferences,
    shouldOpenDailyQuote: Boolean = false
) {
    val navController = rememberNavController()
    
    // Efecto para navegar a la pantalla home cuando se abre desde notificación
    LaunchedEffect(shouldOpenDailyQuote) {
        if (shouldOpenDailyQuote) {
            navController.navigate(Screen.Home.route) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            EnhancedBottomNavigation(
                navController = navController,
                userPreferences = userPreferences
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
