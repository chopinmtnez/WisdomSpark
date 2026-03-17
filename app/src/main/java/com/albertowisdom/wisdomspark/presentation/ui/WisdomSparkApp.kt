package com.albertowisdom.wisdomspark.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.managers.LanguageManager
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.presentation.navigation.Screen
import com.albertowisdom.wisdomspark.presentation.navigation.WisdomNavHost
import com.albertowisdom.wisdomspark.presentation.ui.components.EnhancedBottomNavigation
import com.albertowisdom.wisdomspark.presentation.ui.components.NotificationPermissionHandler
import com.albertowisdom.wisdomspark.utils.NotificationHelper

/**
 * Composable principal de la app - con soporte para navegación del sistema
 */
@Composable
fun WisdomSparkApp(
    adMobManager: AdMobManager,
    userPreferences: UserPreferences,
    languageManager: LanguageManager,
    notificationHelper: NotificationHelper,
    shouldOpenDailyQuote: Boolean = false
) {
    val isFirstLaunch by userPreferences.isFirstLaunch.collectAsState(initial = null)

    // Mientras DataStore no ha resuelto el valor, mostrar splash mínimo
    // para evitar que NavHost se componga con un startDestination incorrecto.
    if (isFirstLaunch == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            // Pantalla vacía con el color de fondo; se resuelve casi instantáneamente
        }
        return
    }

    // A partir de aquí isFirstLaunch es true o false (nunca null)
    val startDestination = if (isFirstLaunch == true) {
        Screen.LanguageSelection.route
    } else {
        Screen.Home.route
    }

    val navController = rememberNavController()

    // Efecto para navegar a la pantalla home cuando se abre desde notificación
    LaunchedEffect(shouldOpenDailyQuote) {
        if (shouldOpenDailyQuote && isFirstLaunch == false) {
            navController.navigate(Screen.Home.route) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
    }

    // Handler para solicitar permisos de notificación automáticamente
    NotificationPermissionHandler(
        userPreferences = userPreferences,
        notificationHelper = notificationHelper
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Solo mostrar bottom navigation si no es primer lanzamiento
            if (isFirstLaunch == false) {
                EnhancedBottomNavigation(
                    navController = navController,
                    userPreferences = userPreferences
                )
            }
        }
    ) { paddingValues ->
        WisdomNavHost(
            navController = navController,
            adMobManager = adMobManager,
            userPreferences = userPreferences,
            languageManager = languageManager,
            notificationHelper = notificationHelper,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
