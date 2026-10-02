package com.umain.hylla

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedbackTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun aClaimShowsASnackbarThatCanUndoIt() {
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Pixel 9a"))
        compose.onNodeWithText("Pixel 9a").performClick()
        compose.onNode(hasText("Claim") and hasClickAction()).performClick()
        compose.onNodeWithText("Saga Nyberg").performClick()
        compose.onAllNodesWithText("Claim").onLast().performClick()

        compose.onNodeWithText("Pixel 9a is now with Saga Nyberg.").assertIsDisplayed()
        compose.onNodeWithText("Undo").performClick()

        compose.onNodeWithText("Available").assertIsDisplayed()
    }
}
