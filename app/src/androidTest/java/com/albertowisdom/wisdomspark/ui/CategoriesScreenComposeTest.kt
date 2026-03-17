package com.albertowisdom.wisdomspark.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.albertowisdom.wisdomspark.MainActivity
import com.albertowisdom.wisdomspark.robots.CategoriesScreenRobot
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI Tests para CategoriesScreen usando Robot Pattern
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CategoriesScreenComposeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var categoriesRobot: CategoriesScreenRobot

    @Before
    fun setUp() {
        hiltRule.inject()
        categoriesRobot = CategoriesScreenRobot(composeTestRule)
        
        // Navegar a la pantalla de categorías
        // TODO: Implementar navegación desde HomeScreen
    }

    @Test
    fun categoriesScreen_displaysTitle() {
        categoriesRobot
            .assertScreenTitle()
    }

    @Test
    fun categoriesScreen_loadsCategoriesSuccessfully() {
        categoriesRobot
            .waitForCategoriesLoad()
            .assertCategoriesGridIsDisplayed()
    }

    @Test
    fun categoriesScreen_showsCorrectEmojis() {
        categoriesRobot
            .waitForCategoriesLoad()
            .assertCategoryWithEmoji("Motivación", "💪")
            .assertCategoryWithEmoji("Liderazgo", "👑")
            .assertCategoryWithEmoji("Vida", "🌱")
    }

    @Test
    fun categoriesScreen_categoryClickNavigation() {
        categoriesRobot
            .waitForCategoriesLoad()
            .scrollAndClickCategory("Motivación")
        
        // TODO: Verificar navegación a CategoryDetailScreen
    }

    @Test
    fun categoriesScreen_handlesRefresh() {
        categoriesRobot
            .waitForCategoriesLoad()
            .clickRefreshButton()
            .assertLoadingState()
            .waitForCategoriesLoad()
            .assertCategoriesGridIsDisplayed()
    }
}