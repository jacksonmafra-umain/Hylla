package com.umain.hylla.fleet

import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FleetStoreTest {

    private val fixture = FleetFixture.parse(File("../../fixtures/devices.json").readText())
    private val clock = Clock.fixed(Instant.parse("2026-10-02T09:00:00Z"), ZoneOffset.UTC)
    private val store = FleetStore(fixture, clock)
    private val flip = DeviceId("HYL-002")
    private val fold = DeviceId("HYL-001")
    private val noah = PersonId("p-02")

    @Test
    fun `claiming an available device puts it in use from today`() {
        assertTrue(store.claim(flip, noah).isSuccess)

        val device = store.fleet.value.device(flip)!!
        assertEquals(AssignmentStatus.InUse, device.assignmentStatus)
        assertEquals(noah, device.currentUser)
        assertEquals(LocalDate.of(2026, 10, 2), device.since)
        assertEquals(Assignment(flip, noah, LocalDate.of(2026, 10, 2), null), store.fleet.value.history(flip).first())
    }

    @Test
    fun `returning closes the open assignment`() {
        assertTrue(store.returnDevice(fold).isSuccess)

        val device = store.fleet.value.device(fold)!!
        assertEquals(AssignmentStatus.Available, device.assignmentStatus)
        assertNull(device.currentUser)
        assertEquals(LocalDate.of(2026, 10, 2), store.fleet.value.history(fold).first().to)
    }

    @Test
    fun `every change keeps the fixture invariants`() {
        store.claim(flip, noah)
        store.returnDevice(fold)
        store.claim(fold, noah)

        assertEquals(emptyList<String>(), FleetFixture.validate(store.fleet.value))
    }

    @Test
    fun `claiming a device someone holds fails and changes nothing`() {
        val before = store.fleet.value

        assertTrue(store.claim(fold, noah).isFailure)
        assertEquals(before, store.fleet.value)
    }

    @Test
    fun `returning a device nobody holds fails and changes nothing`() {
        val before = store.fleet.value

        assertTrue(store.returnDevice(flip).isFailure)
        assertEquals(before, store.fleet.value)
    }
}
