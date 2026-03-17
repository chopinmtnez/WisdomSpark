package com.albertowisdom.wisdomspark.robots

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule

/**
 * Robot Pattern implementation para Categories Screen
 */
class CategoriesScreenRobot(private val composeTestRule: ComposeTestRule) {
    
    /**
     * Actions - Interacciones disponibles
     */
    fun clickCategory(categoryName: String) = apply {
        composeTestRule.onNodeWithTag("category_card_$categoryName")
            .performClick()
    }
    
    fun scrollToCategory(categoryName: String) = apply {
        composeTestRule.onNodeWithTag("categories_grid")
            .performScrollToNode(
                hasTestTag("category_card_$categoryName")
            )
    }
    
    fun clickRefreshButton() = apply {
        composeTestRule.onNodeWithTag("refresh_button")
            .performClick()
    }
    
    /**
     * Assertions - Verificaciones
     */
    fun assertScreenTitle() = apply {
        composeTestRule.onNodeWithTag("categories_title")
            .assertIsDisplayed()
            .assertTextEquals("Categorías")
    }
    
    fun assertCategoryIsDisplayed(categoryName: String) = apply {
        composeTestRule.onNodeWithTag("category_card_$categoryName")
            .assertIsDisplayed()
    }
    
    fun assertCategoryWithEmoji(categoryName: String, emoji: String) = apply {
        composeTestRule.onNodeWithTag("category_emoji_$categoryName")
            .assertTextContains(emoji)
    }
    
    fun assertCategoriesGridIsDisplayed() = apply {
        composeTestRule.onNodeWithTag("categories_grid")
            .assertIsDisplayed()
    }
    
    fun assertLoadingState() = apply {
        composeTestRule.onNodeWithTag("loading_categories")
            .assertIsDisplayed()
    }
    
    fun assertEmptyState() = apply {
        composeTestRule.onNodeWithTag("empty_categories")
            .assertIsDisplayed()
    }
    
    fun assertCategoriesCount(expectedCount: Int) = apply {
        composeTestRule.onAllNodesWithTag("category_card", substring = true)
            .assertCountEquals(expectedCount)
    }
    
    /**
     * Complex Actions - Acciones complejas
     */
    fun scrollAndClickCategory(categoryName: String) = apply {
        scrollToCategory(categoryName)
        clickCategory(categoryName)
    }
    
    fun waitForCategoriesLoad() = apply {
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("category_card", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}