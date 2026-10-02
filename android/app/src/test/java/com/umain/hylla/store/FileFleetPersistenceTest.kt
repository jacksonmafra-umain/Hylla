package com.umain.hylla.store

import com.umain.hylla.fleet.AssignmentStatus
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.FleetFixture
import com.umain.hylla.fleet.FleetStore
import com.umain.hylla.fleet.PersonId
import com.umain.hylla.fleet.device
import java.io.File
import java.nio.file.Files
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FileFleetPersistenceTest {

    private val fixture = FleetFixture.parse(File("../../fixtures/devices.json").readText())
    private val directory: File = Files.createTempDirectory("hylla").toFile()
    private val clock = Clock.fixed(Instant.parse("2026-10-02T09:00:00Z"), ZoneOffset.UTC)
    private val flip = DeviceId("HYL-002")
    private val noah = PersonId("p-02")

    private fun persistence() = FileFleetPersistence(directory, atomic = ::RenamingSnapshot)
    private fun store() = FleetStore(fixture, clock, persistence())

    @Test
    fun `the first run starts from the fixture`() {
        assertNull(persistence().load())
        assertEquals(fixture, store().fleet.value)
    }

    @Test
    fun `a claim survives a new process`() {
        store().claim(flip, noah)

        // A new store over the same directory is what the next process sees.
        val reopened = store()
        assertEquals(AssignmentStatus.InUse, reopened.fleet.value.device(flip)!!.assignmentStatus)
        assertEquals(noah, reopened.fleet.value.device(flip)!!.currentUser)
    }

    @Test
    fun `every change is journalled in order, failures are not`() {
        val store = store()
        store.claim(flip, noah)
        store.claim(flip, noah) // fails: already in use
        store.returnDevice(flip)
        store.undo()

        assertEquals(
            listOf(
                Operation.Claim(flip, noah, LocalDate.of(2026, 10, 2)),
                Operation.Return(flip, LocalDate.of(2026, 10, 2)),
                Operation.Undo,
            ),
            persistence().journal(),
        )
    }

    @Test
    fun `undo is saved too`() {
        val store = store()
        store.claim(flip, noah)
        store.undo()

        assertEquals(AssignmentStatus.Available, store().fleet.value.device(flip)!!.assignmentStatus)
    }

    @Test
    fun `an unreadable snapshot falls back to the fixture instead of crashing`() {
        File(directory, "fleet.json").writeText("{ not json")

        assertEquals(fixture, store().fleet.value)
    }

    @Test
    fun `a snapshot that breaks the fleet's rules is not trusted`() {
        // Valid JSON, but HYL-001 is in use with nobody holding it.
        File(directory, "fleet.json").writeText(
            File("../../fixtures/devices.json").readText().replaceFirst("\"currentUser\": \"p-01\"", "\"currentUser\": null"),
        )

        assertEquals(fixture, store().fleet.value)
    }
}
