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

    /** Applies [transform] atomically; a failure leaves the fleet as it was. */
    private fun change(transform: (Fleet) -> Result<Fleet>): Result<Unit> {
        var outcome = Result.success(Unit)
        state.update { fleet ->
            transform(fleet).getOrElse { error ->
                outcome = Result.failure(error)
                fleet
            }
        }
        return outcome
    }

    private fun failure(message: String) = Result.failure<Fleet>(IllegalStateException(message))

    private fun List<Device>.replace(device: Device) = map { if (it.id == device.id) device else it }
}

fun Fleet.device(id: DeviceId): Device? = devices.firstOrNull { it.id == id }
