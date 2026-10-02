package com.umain.hylla

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepLinkTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private fun launch(link: String): ActivityScenario<MainActivity> =
        ActivityScenario.launch(
            Intent(Intent.ACTION_VIEW, Uri.parse(link))
                .setClass(ApplicationProvider.getApplicationContext(), MainActivity::class.java),
        )

    @Test
    fun aDeviceLinkOpensThatDeviceAndBackGoesToTheFleet() {
        launch("hylla://device/HYL-003").use {
            compose.onNodeWithText("SM-F968B").assertIsDisplayed()

            Espresso.pressBack()

            compose.onNode(hasText("Fleet") and !hasClickAction()).assertIsDisplayed()
        }
    }

    @Test
    fun anUnknownTagSaysSo() {
        launch("hylla://device/HYL-099").use {
            compose.onNodeWithText("No device with shelf tag HYL-099.").assertIsDisplayed()
        }
    }
}
