package com.albertowisdom.wisdomspark.robots

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule

/**
 * Robot Pattern implementation para Home Screen
 * Encapsula las interacciones de UI y hace los tests más legibles
 */
class HomeScreenRobot(private val composeTestRule: ComposeTestRule) {
    
    /**
     * Actions - Interacciones que el usuario puede realizar
     */
    fun swipeQuoteLeft() = apply {
        composeTestRule.onNodeWithTag("swipeable_quote_card")
            .performTouchInput {
                swipeLeft()
            }
    }
    
    fun swipeQuoteRight() = apply {
        composeTestRule.onNodeWithTag("swipeable_quote_card")
            .performTouchInput {
                swipeRight()
            }
    }
    
    fun swipeQuoteUp() = apply {
        composeTestRule.onNodeWithTag("swipeable_quote_card")
            .performTouchInput {
                swipeUp()
            }
    }
    
    fun clickLikeButton() = apply {
        composeTestRule.onNodeWithTag("like_button")
            .performClick()
    }
    
    fun clickPassButton() = apply {
        composeTestRule.onNodeWithTag("pass_button")
            .performClick()
    }
    
    fun clickShareButton() = apply {
        composeTestRule.onNodeWithTag("share_button")
            .performClick()
    }
    
    fun pullToRefresh() = apply {
        composeTestRule.onNodeWithTag("quote_content")
            .performTouchInput {
                swipeDown()
            }
    }
    
    /**
     * Assertions - Verificaciones del estado de la UI
     */
    fun assertQuoteIsDisplayed() = apply {
        composeTestRule.onNodeWithTag("swipeable_quote_card")
            .assertIsDisplayed()
    }
    
    fun assertQuoteText(expectedText: String) = apply {
        composeTestRule.onNodeWithTag("quote_text")
            .assertTextContains(expectedText, substring = true)
    }
    
    fun assertQuoteAuthor(expectedAuthor: String) = apply {
        composeTestRule.onNodeWithTag("quote_author")
            .assertTextContains(expectedAuthor, substring = true)
    }
    
    fun assertProgressIndicatorShows(current: Int, total: Int) = apply {
        composeTestRule.onNodeWithTag("progress_indicator")
            .assertIsDisplayed()
        // Verificar que el texto del progreso contenga los números correctos
        composeTestRule.onNodeWithText("$current/$total")
            .assertIsDisplayed()
    }
    
    fun assertNoMoreCardsMessage() = apply {
        composeTestRule.onNodeWithTag("no_more_cards")
            .assertIsDisplayed()
    }
    
    fun assertLoadingIsDisplayed() = apply {
        composeTestRule.onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
    }
    
    fun assertLoadingIsNotDisplayed() = apply {
        composeTestRule.onNodeWithTag("loading_indicator")
            .assertIsNotDisplayed()
    }
    
    fun assertActionButtonsAreDisplayed() = apply {
        composeTestRule.onNodeWithTag("like_button")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("pass_button")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("share_button")
            .assertIsDisplayed()
    }
    
    fun assertActionButtonsAreNotDisplayed() = apply {
        composeTestRule.onNodeWithTag("like_button")
            .assertDoesNotExist()
        composeTestRule.onNodeWithTag("pass_button")
            .assertDoesNotExist()
        composeTestRule.onNodeWithTag("share_button")
            .assertDoesNotExist()
    }
    
    /**
     * Waits - Esperas para sincronización
     */
    fun waitForQuoteToLoad() = apply {
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("swipeable_quote_card")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
    
    fun waitForAnimation() = apply {
        // Esperar que las animaciones terminen
        composeTestRule.mainClock.advanceTimeBy(1000)
    }
    
    /**
     * Complex Actions - Secuencias de acciones complejas
     */
    fun performCompleteSwipeSequence() = apply {
        assertQuoteIsDisplayed()
        swipeQuoteRight()
        waitForAnimation()
    }
    
    fun skipMultipleQuotes(count: Int) = apply {
        repeat(count) {
            if (composeTestRule.onAllNodesWithTag("swipeable_quote_card")
                    .fetchSemanticsNodes().isNotEmpty()) {
                swipeQuoteLeft()
                waitForAnimation()
            }
        }
    }
}