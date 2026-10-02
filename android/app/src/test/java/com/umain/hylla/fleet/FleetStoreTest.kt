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

    @Test
    fun `editing a record changes its fields and keeps the invariants`() {
        val fold4 = store.fleet.value.device(DeviceId("HYL-019"))!!

        assertTrue(store.update(fold4.copy(notes = "With IT", lifecycle = Lifecycle.Decommission)).isSuccess)
        assertEquals("With IT", store.fleet.value.device(DeviceId("HYL-019"))!!.notes)
        assertEquals(emptyList<String>(), FleetFixture.validate(store.fleet.value))
    }

    @Test
    fun `moving since on a held device moves its open assignment`() {
        val device = store.fleet.value.device(fold)!!
        val earlier = LocalDate.of(2026, 9, 21)

        assertTrue(store.update(device.copy(since = earlier)).isSuccess)
        assertEquals(earlier, store.fleet.value.history(fold).first().from)
    }

    @Test
    fun `an edit cannot change who holds the device`() {
        val device = store.fleet.value.device(fold)!!

        assertTrue(store.update(device.copy(currentUser = noah)).isFailure)
    }

    @Test
    fun `since cannot move into the future`() {
        val device = store.fleet.value.device(flip)!!

        assertTrue(store.update(device.copy(since = LocalDate.of(2026, 10, 3))).isFailure)
    }

    @Test
    fun `an edit that overlaps the previous holder is rejected`() {
        // HYL-001's previous assignment ended on 2026-09-20; the open one cannot start before it.
        val device = store.fleet.value.device(fold)!!
        val before = store.fleet.value

        assertTrue(store.update(device.copy(since = LocalDate.of(2026, 8, 1))).isFailure)
        assertEquals(before, store.fleet.value)
    }
}
