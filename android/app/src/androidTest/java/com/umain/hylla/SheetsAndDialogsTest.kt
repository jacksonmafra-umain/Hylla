package com.umain.hylla

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The process-wide store keeps changes between tests in one run, so each test works on a device
 * no other test touches.
 */
@RunWith(AndroidJUnit4::class)
class SheetsAndDialogsTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    /** A lazy grid only composes what is on screen, so scroll the grid to the tile first. */
    private fun open(device: String) {
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(device))
        compose.onNodeWithText(device).performClick()
    }

    @Test
    fun filtersNarrowTheFleet() {
        compose.onNodeWithText("Filters").performClick()
        compose.onNode(hasText("Android") and hasClickAction()).performClick()
        compose.onNode(hasText("Tablet") and hasClickAction()).performClick()
        compose.onNodeWithText("Done").performClick()

        compose.onNodeWithText("Tab S10 FE").assertIsDisplayed()
        compose.onNodeWithText("Fold7 Blue").assertDoesNotExist()
        compose.onNodeWithText("Filters (2)").assertIsDisplayed()
    }

    @Test
    fun quickClaimFromTheDetail() {
        open("Find N5")
        compose.onNode(hasText("Claim") and hasClickAction()).performClick()
        compose.onNodeWithText("Noah Ekström").performClick()
        compose.onAllNodesWithText("Claim").onLast().performClick()

        compose.onNodeWithText("In use · Noah Ekström").assertIsDisplayed()
    }

    @Test
    fun returningAsksFirst() {
        open("Tab S10 FE")
        compose.onNode(hasText("Return to the shelf") and hasClickAction()).performClick()
        compose.onNodeWithText("Return Tab S10 FE?").assertIsDisplayed()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("In use · Elias Holm").assertIsDisplayed()

        compose.onNode(hasText("Return to the shelf") and hasClickAction()).performClick()
        compose.onAllNodesWithText("Return to the shelf").onLast().performClick()
        compose.onNodeWithText("Available").assertIsDisplayed()
    }

    @Test
    fun leavingAnEditWithChangesAsksFirst() {
        open("Galaxy Fold4")
        compose.onNodeWithText("Edit").performClick()
        compose.onNode(hasSetTextAction() and hasText("Galaxy Fold4")).performTextReplacement("Fold4 for IT")
        // The sheet has its own window and back handling, so send back to the focused window as
        // the system does. The first back would only put the keyboard away.
        Espresso.closeSoftKeyboard()
        Espresso.pressBack()

        compose.onNodeWithText("Discard changes?").assertExists()
        compose.onNodeWithText("Keep editing").performClick()
        compose.onNodeWithText("Discard changes?").assertDoesNotExist()
        compose.onNodeWithText("Save").performClick()

        compose.onAllNodesWithText("Fold4 for IT").onLast().assertIsDisplayed()
    }
}
