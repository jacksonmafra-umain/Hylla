package com.umain.hylla

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The Accessibility Test Framework's checks, run on every screen: touch target size, contrast,
 * labels, duplicate descriptions. Any failure fails the test with the check's explanation.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class AccessibilityChecksTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun enable() {
        compose.enableAccessibilityChecks()
    }

    private fun check() {
        compose.onRoot().tryPerformAccessibilityChecks()
    }

    private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.onRoot() =
        onNode(SemanticsMatcher("root") { it.parent == null }, useUnmergedTree = true)

    @Test
    fun theFleetPasses() {
        check()
    }

    @Test
    fun aDetailPasses() {
        compose.onNodeWithText("Fold7 Blue").performClick()
        compose.onNodeWithText("SM-F966B").assertIsDisplayed()
        check()
    }

    @Test
    fun theScannerPasses() {
        compose.onNodeWithText("Scan").performClick()
        check()
        Espresso.pressBack()
    }

    @Test
    fun youAndThisDevicePass() {
        compose.onNode(hasText("You") and hasClickAction()).performClick()
        check()
        compose.onNode(hasText("This device") and hasClickAction()).performClick()
        check()
    }

    @Test
    fun panesAreNamed() {
        compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "Fleet")).assertExists()
    }
}
