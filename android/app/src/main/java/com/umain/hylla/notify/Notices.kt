package com.umain.hylla.notify

import com.umain.hylla.fleet.AssignmentStatus
import com.umain.hylla.fleet.Device
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.PersonId
import com.umain.hylla.fleet.isClaimable
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** What deserves a notification. Pure, so the rules are tested apart from how they are posted. */
object Notices {
    /** A device held this many days or more is overdue for return. */
    const val OVERDUE_AFTER_DAYS = 14L

    /** Devices [me] has held for [OVERDUE_AFTER_DAYS] or more, longest first. */
    fun overdue(fleet: Fleet, me: PersonId?, today: LocalDate): List<Device> {
        if (me == null) return emptyList()
        return fleet.devices
            .filter { it.assignmentStatus == AssignmentStatus.InUse && it.currentUser == me }
            .filter { ChronoUnit.DAYS.between(it.since, today) >= OVERDUE_AFTER_DAYS }
            .sortedBy { it.since }
    }

    /** The day [device] becomes overdue, for scheduling. */
    fun overdueOn(device: Device): LocalDate = device.since.plusDays(OVERDUE_AFTER_DAYS)

    /** Watched devices that could not be taken [before] and can be [after]. */
    fun backOnTheShelf(before: Fleet, after: Fleet, watched: Set<DeviceId>): List<Device> =
        after.devices.filter { device ->
            device.id in watched &&
                device.isClaimable &&
                before.devices.firstOrNull { it.id == device.id }?.isClaimable == false
        }
}
