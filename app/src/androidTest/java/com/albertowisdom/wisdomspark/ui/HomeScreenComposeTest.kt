package com.albertowisdom.wisdomspark.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.albertowisdom.wisdomspark.MainActivity
import com.albertowisdom.wisdomspark.robots.HomeScreenRobot
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI Tests para HomeScreen usando Robot Pattern
 * Demuestra testing moderno de UI con patrones escalables
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeScreenComposeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var homeRobot: HomeScreenRobot

    @Before
    fun setUp() {
        hiltRule.inject()
        homeRobot = HomeScreenRobot(composeTestRule)
    }

    @Test
    fun homeScreen_displaysQuoteSuccessfully() {
        homeRobot
            .waitForQuoteToLoad()
            .assertQuoteIsDisplayed()
            .assertActionButtonsAreDisplayed()
    }

    @Test
    fun homeScreen_swipeRightLikesQuote() {
        homeRobot
            .waitForQuoteToLoad()
            .performCompleteSwipeSequence()
            .waitForAnimation()
            .assertQuoteIsDisplayed() // Debe mostrar la siguiente cita
    }

    @Test
    fun homeScreen_swipeLeftPassesQuote() {
        homeRobot
            .waitForQuoteToLoad()
            .swipeQuoteLeft()
            .waitForAnimation()
            .assertQuoteIsDisplayed() // Debe mostrar la siguiente cita
    }

    @Test
    fun homeScreen_clickLikeButtonWorks() {
        homeRobot
            .waitForQuoteToLoad()
            .clickLikeButton()
            .waitForAnimation()
            .assertQuoteIsDisplayed() // Debe mostrar la siguiente cita
    }

    @Test
    fun homeScreen_clickPassButtonWorks() {
        homeRobot
            .waitForQuoteToLoad()
            .clickPassButton()
            .waitForAnimation()
            .assertQuoteIsDisplayed() // Debe mostrar la siguiente cita
    }

    @Test
    fun homeScreen_showsProgressIndicator() {
        homeRobot
            .waitForQuoteToLoad()
            .assertProgressIndicatorShows(1, 10) // Ajustar según datos de test
    }

    @Test
    fun homeScreen_handlesMultipleSwipes() {
        homeRobot
            .waitForQuoteToLoad()
            .skipMultipleQuotes(3)
            .assertQuoteIsDisplayed() // Debe seguir mostrando citas después de varios swipes
    }

    @Test
    fun homeScreen_showsNoMoreCardsWhenEmpty() {
        homeRobot
            .waitForQuoteToLoad()
            .skipMultipleQuotes(10) // Skip más citas que las disponibles
            .assertNoMoreCardsMessage()
            .assertActionButtonsAreNotDisplayed()
    }
}