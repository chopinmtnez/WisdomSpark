package com.albertowisdom.wisdomspark.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.presentation.ui.screens.categories.CategoriesScreen
import com.albertowisdom.wisdomspark.presentation.ui.screens.categories.CategoryDetailScreen
import com.albertowisdom.wisdomspark.presentation.ui.screens.favorites.FavoritesScreen
import com.albertowisdom.wisdomspark.presentation.ui.screens.home.EnhancedHomeScreen
import com.albertowisdom.wisdomspark.presentation.ui.screens.home.HomeScreen
import com.albertowisdom.wisdomspark.presentation.ui.screens.settings.SettingsScreen

/**
 * NavHost con soporte para AdMob y UserPreferences en todas las pantallas
 */
@Composable
fun WisdomNavHost(
    navController: NavHostController,
    adMobManager: AdMobManager,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            EnhancedHomeScreen(
                adMobManager = adMobManager,
                userPreferences = userPreferences,
                onNavigateToSettings = {
                    println("🔧 Navigating to Settings from Swipe mode")
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        
        composable(Screen.Categories.route) {
            CategoriesScreen(
                adMobManager = adMobManager,
                onCategoryClick = { categoryName ->
                    // Navegar al detalle de la categoría
                    println("📚 Navigating to CategoryDetail: $categoryName")
                    navController.navigate("category_detail/$categoryName")
                }
            )
        }
        
        composable("category_detail/{categoryName}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryDetailScreen(
                categoryName = categoryName,
                adMobManager = adMobManager,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                adMobManager = adMobManager,
                onNavigateToHome = {
                    println("❤️ Navigating to Home from Favorites")
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
                userPreferences = userPreferences
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
}
