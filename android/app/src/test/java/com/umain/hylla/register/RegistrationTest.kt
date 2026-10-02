package com.umain.hylla.register

import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.FleetFixture
import com.umain.hylla.fleet.FleetStore
import com.umain.hylla.posture.DpBounds
import com.umain.hylla.posture.Fold
import com.umain.hylla.posture.FoldOcclusion
import com.umain.hylla.posture.FoldOrientation
import com.umain.hylla.posture.FoldState
import com.umain.hylla.posture.WindowPosture
import java.io.File
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationTest {

    private val fleet = FleetFixture.parse(File("../../fixtures/devices.json").readText())
    private val today = LocalDate.of(2026, 10, 2)
    private val fold = BuildInfo(manufacturer = "samsung", model = "SM-F971B", release = "17", deviceName = "QA Fold 8")
    private val book = WindowPosture.compute(
        852f, 883f,
        listOf(Fold(DpBounds(426f, 0f, 426f, 883f), FoldOrientation.Vertical, FoldState.HalfOpened, true, FoldOcclusion.None)),
    )

    @Test
    fun `the draft describes the device it runs on`() {
        val draft = Registration.draft(fold, book, fleet, today)

        assertEquals("QA Fold 8", draft.deviceName)
        assertEquals("Samsung SM-F971B", draft.modelName)
        assertEquals("SM-F971B", draft.modelNumber)
        assertEquals("17", draft.osVersion)
        assertEquals("One UI", draft.uiVersion)
        assertEquals(today, draft.since)
    }

    @Test
    fun `the window at registration goes in the notes`() {
        assertEquals(
            "Registered from the device. Window 852 × 883 dp, expanded × medium, posture book, 1 fold(s), separating: vertical at 426 dp.",
            Registration.draft(fold, book, fleet, today).notes,
        )
    }

    @Test
    fun `it takes the next free shelf tag`() {
        assertEquals(DeviceId("HYL-020"), Registration.nextTag(fleet))
    }

    @Test
    fun `a stock Android device has no skin, and no Settings name falls back to the model`() {
        val pixel = Registration.draft(BuildInfo("Google", "Pixel 10 Pro Fold", "17", null), WindowPosture.compute(411f, 891f), fleet, today)

        assertNull(pixel.uiVersion)
        assertEquals("Google Pixel 10 Pro Fold", pixel.deviceName)
        assertTrue(pixel.notes!!.endsWith("posture flat, no folds."))
    }

    @Test
    fun `a registered device joins the fleet and keeps its rules`() {
        val store = FleetStore(fleet)

        assertTrue(store.register(Registration.draft(fold, book, fleet, today)).isSuccess)
        assertEquals(20, store.fleet.value.devices.size)
        assertTrue(store.register(Registration.draft(fold, book, fleet, today)).isFailure) // HYL-020 is taken now
    }
}
