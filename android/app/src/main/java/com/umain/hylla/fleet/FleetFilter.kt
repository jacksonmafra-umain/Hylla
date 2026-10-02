package com.umain.hylla.fleet

import kotlinx.serialization.Serializable

/**
 * Which devices the fleet shows. Empty sets mean "any". Serializable so it survives a fold, a
 * rotation and process death as saved state.
 */
@Serializable
data class FleetFilter(
    val platforms: Set<Platform> = emptySet(),
    val type: DeviceType? = null,
    val statuses: Set<AssignmentStatus> = emptySet(),
    /** Only devices someone could take right now: available and still in service. */
    val claimableOnly: Boolean = false,
) {
    /** How many separate conditions are set, for the "Filters (n)" label. */
    val activeCount: Int
        get() = listOf(platforms.isNotEmpty(), type != null, statuses.isNotEmpty(), claimableOnly).count { it }

    fun matches(device: Device): Boolean =
        (platforms.isEmpty() || device.platform in platforms) &&
            (type == null || device.deviceType == type) &&
            (statuses.isEmpty() || device.assignmentStatus in statuses) &&
            (!claimableOnly || device.isClaimable)
}

val Device.isClaimable: Boolean
    get() = assignmentStatus == AssignmentStatus.Available && lifecycle == Lifecycle.InUse
