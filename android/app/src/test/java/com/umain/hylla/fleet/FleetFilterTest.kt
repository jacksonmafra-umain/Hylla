package com.umain.hylla.fleet

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FleetFilterTest {

    private val fleet = FleetFixture.parse(File("../../fixtures/devices.json").readText())

    private fun names(filter: FleetFilter) = fleet.devices.filter(filter::matches).map { it.deviceName }

    @Test
    fun `no conditions match everything`() {
        assertEquals(fleet.devices.size, names(FleetFilter()).size)
        assertEquals(0, FleetFilter().activeCount)
    }

    @Test
    fun `platforms are any-of`() {
        val ipads = names(FleetFilter(platforms = setOf(Platform.IPadOS, Platform.VisionOS)))

        assertEquals(listOf("iPad Pro 13", "iPad mini", "Vision Pro"), ipads)
    }

    @Test
    fun `conditions combine with and`() {
        val filter = FleetFilter(platforms = setOf(Platform.Android), type = DeviceType.Tablet)

        assertEquals(listOf("Tab S10 FE"), names(filter))
        assertEquals(2, filter.activeCount)
    }

    @Test
    fun `claimable excludes held, missing and decommissioned devices`() {
        val claimable = fleet.devices.filter(FleetFilter(claimableOnly = true)::matches)

        assertTrue(claimable.all { it.assignmentStatus == AssignmentStatus.Available && it.lifecycle == Lifecycle.InUse })
        assertTrue("Galaxy Fold4" !in claimable.map { it.deviceName })
        assertEquals(9, claimable.size)
    }
}
