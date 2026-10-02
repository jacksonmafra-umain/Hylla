package com.umain.hylla.fleet

import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The fleet as the app currently knows it, and the changes made to it.
 *
 * In memory for now: it lives as long as the process. Chapter 16 makes it persistent and queues
 * every change for sync. Every operation keeps the fixture invariants, so [FleetFixture.validate]
 * passes after each one.
 */
class FleetStore(initial: Fleet, private val clock: Clock = Clock.systemDefaultZone()) {
    private val state = MutableStateFlow(initial)
    val fleet: StateFlow<Fleet> = state.asStateFlow()

    /** The fleet before the last successful change, for one level of undo. */
    private var beforeLastChange: Fleet? = null

    private val today: LocalDate get() = LocalDate.now(clock)

    /** [person] takes [device] from the shelf. Fails if someone already holds it. */
    fun claim(device: DeviceId, person: PersonId): Result<Unit> = change { fleet ->
        val current = fleet.device(device) ?: return@change failure("No device $device")
        if (current.assignmentStatus == AssignmentStatus.InUse) return@change failure("${current.deviceName} is in use")
        if (fleet.person(person) == null) return@change failure("No person $person")
        Result.success(fleet.copy(
            devices = fleet.devices.replace(current.copy(
                assignmentStatus = AssignmentStatus.InUse,
                currentUser = person,
                since = today,
            )),
            assignments = fleet.assignments + Assignment(device, person, from = today, to = null),
        ))
    }

    /** [device] goes back on the shelf. Fails if nobody holds it. */
    fun returnDevice(device: DeviceId): Result<Unit> = change { fleet ->
        val current = fleet.device(device) ?: return@change failure("No device $device")
        if (current.assignmentStatus != AssignmentStatus.InUse) return@change failure("${current.deviceName} is not in use")
        Result.success(fleet.copy(
            devices = fleet.devices.replace(current.copy(
                assignmentStatus = AssignmentStatus.Available,
                currentUser = null,
                since = today,
            )),
            assignments = fleet.assignments.map {
                if (it.deviceId == device && it.to == null) it.copy(to = today) else it
            },
        ))
    }

    /**
     * Replaces the editable fields of a device: everything except who holds it, which only
     * [claim] and [returnDevice] change. Moving `since` on a held device moves the start of its
     * open assignment with it. An edit that would break the fleet's invariants is rejected.
     */
    fun update(edited: Device): Result<Unit> = change { fleet ->
        val current = fleet.device(edited.id) ?: return@change failure("No device ${edited.id}")
        if (edited.assignmentStatus != current.assignmentStatus || edited.currentUser != current.currentUser) {
            return@change failure("Claim or return to change who holds ${current.deviceName}")
        }
        if (edited.since > today) return@change failure("Since cannot be in the future")
        val next = fleet.copy(
            devices = fleet.devices.replace(edited),
            assignments = fleet.assignments.map {
                if (it.deviceId == edited.id && it.to == null) it.copy(from = edited.since) else it
            },
        )
        val problems = FleetFixture.validate(next)
        if (problems.isNotEmpty()) failure(problems.joinToString()) else Result.success(next)
    }

    /**
     * Puts the fleet back as it was before the last change. One level only: undo is for the
     * "that was the wrong device" moment right after a claim, not a history.
     */
    fun undo(): Result<Unit> {
        val previous = beforeLastChange ?: return Result.failure(IllegalStateException("Nothing to undo"))
        state.value = previous
        beforeLastChange = null
        return Result.success(Unit)
    }

    /** Applies [transform] atomically; a failure leaves the fleet as it was. */
    private fun change(transform: (Fleet) -> Result<Fleet>): Result<Unit> {
        var outcome = Result.success(Unit)
        state.update { fleet ->
            transform(fleet).fold(
                onSuccess = { next ->
                    beforeLastChange = fleet
                    next
                },
                onFailure = { error ->
                    outcome = Result.failure(error)
                    fleet
                },
            )
        }
        return outcome
    }

    private fun failure(message: String) = Result.failure<Fleet>(IllegalStateException(message))

    private fun List<Device>.replace(device: Device) = map { if (it.id == device.id) device else it }
}

fun Fleet.device(id: DeviceId): Device? = devices.firstOrNull { it.id == id }
