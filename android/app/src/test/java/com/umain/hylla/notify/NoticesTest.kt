package com.umain.hylla.notify

import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.FleetFixture
import com.umain.hylla.fleet.FleetStore
import com.umain.hylla.fleet.PersonId
import java.io.File
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoticesTest {

    private val fleet = FleetFixture.parse(File("../../fixtures/devices.json").readText())
    private val alva = PersonId("p-01")
    private val noah = PersonId("p-02")

    @Test
    fun `nothing is overdue on the day it was claimed`() {
        assertTrue(Notices.overdue(fleet, alva, LocalDate.of(2026, 9, 22)).isEmpty())
    }

    @Test
    fun `a device held fourteen days is overdue`() {
        // Alva has held Fold7 Blue since 2026-09-22.
        assertEquals(listOf("HYL-001"), Notices.overdue(fleet, alva, LocalDate.of(2026, 10, 6)).map { it.id.value })
        assertEquals(LocalDate.of(2026, 10, 6), Notices.overdueOn(fleet.devices.first { it.id == DeviceId("HYL-001") }))
    }

    @Test
    fun `only the phone holder's devices, longest held first`() {
        // Noah holds the Pixel 9 Pro Fold (since 2026-08-18) and the Mac mini (since 2026-03-09).
        assertEquals(listOf("HYL-017", "HYL-005"), Notices.overdue(fleet, noah, LocalDate.of(2026, 10, 2)).map { it.id.value })
        assertTrue(Notices.overdue(fleet, null, LocalDate.of(2026, 10, 2)).isEmpty())
    }

    @Test
    fun `a watched device returned to the shelf is back`() {
        val store = FleetStore(fleet)
        store.returnDevice(DeviceId("HYL-003"))

        val back = Notices.backOnTheShelf(fleet, store.fleet.value, watched = setOf(DeviceId("HYL-003")))

        assertEquals(listOf("HYL-003"), back.map { it.id.value })
    }

    @Test
    fun `unwatched or still unavailable devices are not`() {
        val store = FleetStore(fleet)
        store.returnDevice(DeviceId("HYL-003"))

        assertTrue(Notices.backOnTheShelf(fleet, store.fleet.value, watched = setOf(DeviceId("HYL-005"))).isEmpty())
        assertTrue(Notices.backOnTheShelf(fleet, store.fleet.value, watched = emptySet()).isEmpty())
    }
}
