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
                    add("$id has a current user but is ${device.assignmentStatus}")
                user != null && user !in people ->
                    add("$id references unknown person ${user.value}")
            }
        }
    }
}

class InvalidFleetException(val problems: List<String>) :
    IllegalArgumentException(problems.joinToString(prefix = "Invalid fleet fixture: ", separator = "; "))
