package com.umain.hylla.fleet

import kotlinx.serialization.json.Json

/** Parses and validates the shared fixture. */
object FleetFixture {
    const val SCHEMA_VERSION = 1

    /**
     * Strict on purpose: an unknown key fails the parse, so a field that must never be modelled
     * (serial number, IMEI, EID, UDID) cannot slip into the fixture unnoticed.
     */
    private val json = Json {
        ignoreUnknownKeys = false
        explicitNulls = true
    }

    fun parse(text: String): Fleet {
        val fleet = json.decodeFromString<Fleet>(text)
        val problems = validate(fleet)
        if (problems.isNotEmpty()) throw InvalidFleetException(problems)
        return fleet
    }

    fun validate(fleet: Fleet): List<String> = buildList {
        if (fleet.schemaVersion != SCHEMA_VERSION) {
            add("schemaVersion ${fleet.schemaVersion}, expected $SCHEMA_VERSION")
        }
        fleet.devices.groupBy { it.id }.filterValues { it.size > 1 }.keys.forEach {
            add("duplicate device id ${it.value}")
        }
        val people = fleet.people.map { it.id }.toSet()
        for (device in fleet.devices) {
            val id = device.id.value
            val user = device.currentUser
            when {
                device.assignmentStatus == AssignmentStatus.InUse && user == null ->
                    add("$id is in use without a current user")
                device.assignmentStatus != AssignmentStatus.InUse && user != null ->
                    add("$id has a current user but is ${device.assignmentStatus.code}")
                user != null && user !in people ->
                    add("$id references unknown person ${user.value}")
            }
        }
        validateAssignments(fleet, people)
    }

    private fun MutableList<String>.validateAssignments(fleet: Fleet, people: Set<PersonId>) {
        val devices = fleet.devices.associateBy { it.id }
        for (assignment in fleet.assignments) {
            val id = assignment.deviceId.value
            if (assignment.deviceId !in devices) add("assignment for unknown device $id")
            if (assignment.person !in people) add("assignment on $id references unknown person ${assignment.person.value}")
            val to = assignment.to
            if (to != null && to < assignment.from) add("assignment on $id ends before it starts")
        }
        for ((device, periods) in fleet.assignments.groupBy { it.deviceId }) {
            // Nobody holds a device twice at once: each period starts after the one before ends.
            val overlaps = periods.sortedBy { it.from }.zipWithNext().any { (earlier, later) ->
                val end = earlier.to
                end == null || later.from < end
            }
            if (overlaps) add("assignments on ${device.value} overlap")
        }
        for (device in fleet.devices) {
            val open = fleet.assignments.filter { it.deviceId == device.id && it.to == null }
            val id = device.id.value
            if (device.assignmentStatus == AssignmentStatus.InUse) {
                val current = open.singleOrNull()
                when {
                    current == null -> add("$id is in use with ${open.size} open assignments, expected 1")
                    current.person != device.currentUser -> add("$id open assignment is not held by its current user")
                    current.from != device.since -> add("$id open assignment does not start on since")
                }
            } else if (open.isNotEmpty()) {
                add("$id is ${device.assignmentStatus.code} but has an open assignment")
            }
        }
    }
}

class InvalidFleetException(val problems: List<String>) :
    IllegalArgumentException(problems.joinToString(prefix = "Invalid fleet fixture: ", separator = "; "))

/** The fixture code, as written in devices.json, for messages that quote it. */
private val AssignmentStatus.code: String get() = name.replaceFirstChar { it.lowercase() }
