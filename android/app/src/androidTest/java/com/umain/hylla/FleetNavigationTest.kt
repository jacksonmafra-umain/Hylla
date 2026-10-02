package com.umain.hylla

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FleetNavigationTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    /** The fleet's title, not the navigation item that shares its label. */
    private val fleetTitle = hasText("Fleet") and !hasClickAction()

    private fun openFold7() {
        compose.onNodeWithText("Fold7 Blue").performClick()
        compose.onNodeWithText("SM-F966B").assertIsDisplayed()
    }

    @Test
    fun fleetToDetailAndBackWithTheUpButton() {
        openFold7()

        compose.onNodeWithContentDescription("Back").performClick()

        compose.onNode(fleetTitle).assertIsDisplayed()
    }

    @Test
    fun systemBackReturnsToTheFleet() {
        openFold7()

        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

        compose.onNode(fleetTitle).assertIsDisplayed()
    }

    @Test
    fun detailSurvivesActivityRecreation() {
        openFold7()

        // What a fold, a rotation or a density change does when configChanges does not absorb it.
        compose.activityRule.scenario.recreate()

        compose.onNodeWithText("SM-F966B").assertIsDisplayed()
    }

    @Test
    fun fleetRowIsOneTargetForScreenReaders() {
        compose.onNode(hasText("Fold7 Blue") and hasText("In use · Alva Berg") and hasClickAction())
            .assertExists()
    }

    @Test
    fun detailFieldReadsAsLabelAndValue() {
        openFold7()

        compose.onNode(hasText("Model") and hasText("Galaxy Z Fold7")).assertExists()
        compose.onNode(hasText("Model number") and hasText("SM-F966B")).assertExists()
    }

    @Test
    fun backFromTheScannerReturnsToTheFleet() {
        compose.onNodeWithText("Scan").performClick()
        compose.onNodeWithText("Check out or return").assertIsDisplayed()

        Espresso.pressBack()

        compose.onNode(fleetTitle).assertIsDisplayed()
    }

    @Test
    fun backFromAnotherDestinationReturnsToTheFleetFirst() {
        compose.onNodeWithText("This device").performClick()

        Espresso.pressBack()

        compose.onNode(fleetTitle).assertIsDisplayed()
    }
}
