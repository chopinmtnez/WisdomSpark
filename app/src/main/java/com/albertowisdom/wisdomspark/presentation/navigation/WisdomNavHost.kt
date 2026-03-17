package com.albertowisdom.wisdomspark.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.managers.LanguageManager
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.utils.NotificationHelper
import com.albertowisdom.wisdomspark.presentation.ui.screens.categories.CategoriesScreen
import com.albertowisdom.wisdomspark.presentation.ui.screens.categories.CategoryDetailScreen
import com.albertowisdom.wisdomspark.presentation.ui.screens.favorites.FavoritesScreen
import com.albertowisdom.wisdomspark.presentation.ui.screens.home.EnhancedHomeScreen
import com.albertowisdom.wisdomspark.presentation.ui.screens.onboarding.LanguageSelectionScreen
import com.albertowisdom.wisdomspark.presentation.ui.screens.settings.SettingsScreen
import com.albertowisdom.wisdomspark.premium.ui.PremiumScreen
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * NavHost con soporte para AdMob y UserPreferences en todas las pantallas
 */
@Composable
fun WisdomNavHost(
    navController: NavHostController,
    adMobManager: AdMobManager,
    userPreferences: UserPreferences,
    languageManager: LanguageManager,
    notificationHelper: NotificationHelper,
    startDestination: String = Screen.Home.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                onLanguageSelected = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.LanguageSelection.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            EnhancedHomeScreen(
                adMobManager = adMobManager,
                userPreferences = userPreferences,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToPremium = {
                    navController.navigate(Screen.Premium.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(Screen.Categories.route) {
            CategoriesScreen(
                adMobManager = adMobManager,
                onCategoryClick = { categoryName ->
                    // Navegar al detalle de la categoría (URL-encoded para caracteres especiales)
                    val encodedName = URLEncoder.encode(categoryName, "UTF-8")
                    navController.navigate("category_detail/$encodedName")
                }
            )
        }
        
        composable("category_detail/{categoryName}") { backStackEntry ->
            val categoryName = URLDecoder.decode(
                backStackEntry.arguments?.getString("categoryName") ?: "", "UTF-8"
            )
            CategoryDetailScreen(
                categoryName = categoryName,
                adMobManager = adMobManager,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPremium = {
                    navController.navigate(Screen.Premium.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                adMobManager = adMobManager,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                userPreferences = userPreferences,
                languageManager = languageManager,
                notificationHelper = notificationHelper,
                onNavigateToPremium = {
                    navController.navigate(Screen.Premium.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(Screen.Premium.route) {
            PremiumScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Definición de pantallas con iconos para bottom navigation
 */
sealed class Screen(
    val route: String,
    val title: String,
    val selectedEmoji: String,
    val unselectedIcon: String
) {
    object Home : Screen(
        route = "home",
        title = "Inicio",
        selectedEmoji = "✨",
        unselectedIcon = "🏠"
    )
    
    object Categories : Screen(
        route = "categories", 
        title = "Categorías",
        selectedEmoji = "📚",
        unselectedIcon = "📖"
    )
    
    object Favorites : Screen(
        route = "favorites",
        title = "Favoritos", 
        selectedEmoji = "❤️",
        unselectedIcon = "🤍"
    )
    
    object Settings : Screen(
        route = "settings",
        title = "Ajustes",
        selectedEmoji = "⚙️",
        unselectedIcon = "🔧"
    )
    
    object Premium : Screen(
        route = "premium",
        title = "Premium",
        selectedEmoji = "👑",
        unselectedIcon = "💎"
    )
    
    object LanguageSelection : Screen(
        route = "language_selection",
        title = "Selección de idioma",
        selectedEmoji = "🌐",
        unselectedIcon = "🌍"
    )
}
