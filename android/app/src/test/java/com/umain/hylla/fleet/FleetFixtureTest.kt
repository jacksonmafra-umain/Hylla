package com.umain.hylla.fleet

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FleetFixtureTest {

    // Unit tests run with the module directory as the working directory.
    private val sharedFixture = File("../../fixtures/devices.json").readText()

    @Test
    fun `shared fixture parses and validates`() {
        val fleet = FleetFixture.parse(sharedFixture)

        assertEquals(19, fleet.devices.size)
        assertEquals(6, fleet.people.size)
        assertEquals(41, fleet.assignments.size)
    }

    @Test
    fun `shared fixture covers every enum value`() {
        val fleet = FleetFixture.parse(sharedFixture)

        assertEquals(DeviceType.entries.toSet(), fleet.devices.map { it.deviceType }.toSet())
        assertEquals(Platform.entries.toSet(), fleet.devices.map { it.platform }.toSet())
        assertEquals(AssignmentStatus.entries.toSet(), fleet.devices.map { it.assignmentStatus }.toSet())
        assertEquals(HomeUse.entries.toSet(), fleet.devices.map { it.homeUse }.toSet())
        assertEquals(Lifecycle.entries.toSet(), fleet.devices.map { it.lifecycle }.toSet())
    }

    @Test
    fun `fields decode to native types`() {
        val fold = FleetFixture.parse(sharedFixture).devices.first { it.id == DeviceId("HYL-001") }

        assertEquals(Platform.Android, fold.platform)
        assertEquals("One UI 8", fold.uiVersion)
        assertEquals(LocalDate.of(2026, 9, 22), fold.since)
        assertEquals(PersonId("p-01"), fold.currentUser)
    }

    @Test
    fun `null optional fields stay null`() {
        val iphone = FleetFixture.parse(sharedFixture).devices.first { it.id == DeviceId("HYL-012") }

        assertNull(iphone.uiVersion)
        assertNull(iphone.currentUser)
        assertNull(iphone.notes)
    }

    @Test
    fun `fixture never carries hardware identifiers`() {
        val forbidden = listOf("serial", "imei", "eid", "udid", "applepay")
        val keys = Regex("\"([A-Za-z0-9 _]+)\"\\s*:").findAll(sharedFixture)
            .map { it.groupValues[1].lowercase().replace(" ", "").replace("_", "") }
            .toSet()

        for (key in keys) {
            assertFalse("forbidden key $key", forbidden.any { key.startsWith(it) })
        }
    }

    @Test
    fun `unknown keys fail the parse`() {
        val withSerial = sharedFixture.replaceFirst(
            "\"deviceName\": \"Fold7 Blue\"",
            "\"deviceName\": \"Fold7 Blue\", \"serialNumber\": \"X\"",
        )

        assertThrows(SerializationException::class.java) { FleetFixture.parse(withSerial) }
    }

    @Test
    fun `missing nullable key fails the parse`() {
        val withoutNotes = sharedFixture.replaceFirst(",\n      \"notes\": \"Primary book-posture test device.\"", "")

        assertThrows(SerializationException::class.java) { FleetFixture.parse(withoutNotes) }
    }

    @Test
    fun `in use without a user is rejected`() {
        val broken = sharedFixture.replaceFirst("\"currentUser\": \"p-01\"", "\"currentUser\": null")

        val error = assertThrows(InvalidFleetException::class.java) { FleetFixture.parse(broken) }
        assertTrue(error.problems.contains("HYL-001 is in use without a current user"))
    }

    @Test
    fun `unknown person is rejected`() {
        val broken = sharedFixture.replaceFirst("\"currentUser\": \"p-01\"", "\"currentUser\": \"p-99\"")

        val error = assertThrows(InvalidFleetException::class.java) { FleetFixture.parse(broken) }
        assertTrue(error.problems.contains("HYL-001 references unknown person p-99"))
    }

    @Test
    fun `invalid calendar day fails the parse`() {
        val broken = sharedFixture.replaceFirst("\"since\": \"2026-09-22\"", "\"since\": \"2026-02-30\"")

        assertThrows(DateTimeParseException::class.java) { FleetFixture.parse(broken) }
    }

    @Test
    fun `history is newest first and the open assignment matches the current holder`() {
        val fleet = FleetFixture.parse(sharedFixture)
        val history = fleet.history(DeviceId("HYL-001"))

        assertEquals(5, history.size)
        assertEquals(history.sortedByDescending { it.from }, history)
        assertNull(history.first().to)
        assertEquals(PersonId("p-01"), history.first().person)
    }

    @Test
    fun `a returned device with an open assignment is rejected`() {
        val broken = sharedFixture.replaceFirst("\"assignmentStatus\": \"inUse\",\n      \"currentUser\": \"p-01\"",
            "\"assignmentStatus\": \"available\",\n      \"currentUser\": null")

        val error = assertThrows(InvalidFleetException::class.java) { FleetFixture.parse(broken) }
        assertEquals(listOf("HYL-001 is available but has an open assignment"), error.problems)
    }

    @Test
    fun `unknown assignment keys fail the parse`() {
        val broken = sharedFixture.replaceFirst("\"deviceId\": \"HYL-001\",", "\"deviceId\": \"HYL-001\", \"udid\": \"X\",")

        assertThrows(SerializationException::class.java) { FleetFixture.parse(broken) }
    }
}
