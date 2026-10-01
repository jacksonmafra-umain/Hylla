package com.umain.hylla.fleet

import java.time.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The whole QA fleet as read from `fixtures/devices.json`. */
@Serializable
data class Fleet(
    val schemaVersion: Int,
    val people: List<Person>,
    val devices: List<Device>,
) {
    fun person(id: PersonId): Person? = people.firstOrNull { it.id == id }
}

@JvmInline
@Serializable
value class PersonId(val value: String)

/** Shelf tag, `HYL-NNN`. The value the scanner reads. */
@JvmInline
@Serializable
value class DeviceId(val value: String)

@Serializable
data class Person(
    val id: PersonId,
    val name: String,
)

@Serializable
data class Device(
    val id: DeviceId,
    val deviceName: String,
    val deviceType: DeviceType,
    val platform: Platform,
    val modelName: String,
    val modelNumber: String,
    val osVersion: String,
    /** Manufacturer skin such as One UI or HyperOS. Null when the platform has none. */
    val uiVersion: String?,
    val assignmentStatus: AssignmentStatus,
    val currentUser: PersonId?,
    /** In use or available since. A calendar date with no time or zone. */
    @Serializable(with = LocalDateSerializer::class)
    val since: LocalDate,
    val homeUse: HomeUse,
    val lifecycle: Lifecycle,
    val notes: String?,
)

@Serializable
enum class DeviceType {
    @SerialName("phone") Phone,
    @SerialName("tablet") Tablet,
    @SerialName("headset") Headset,
    @SerialName("desktop") Desktop,
}

@Serializable
enum class Platform {
    @SerialName("ios") IOS,
    @SerialName("ipados") IPadOS,
    @SerialName("android") Android,
    @SerialName("visionos") VisionOS,
    @SerialName("macos") MacOS,
    @SerialName("carplay") CarPlay,
}

@Serializable
enum class AssignmentStatus {
    @SerialName("inUse") InUse,
    @SerialName("available") Available,
    @SerialName("missing") Missing,
    @SerialName("needsSorting") NeedsSorting,
}

@Serializable
enum class HomeUse {
    @SerialName("approved") Approved,
    @SerialName("officeOnly") OfficeOnly,
    @SerialName("notApproved") NotApproved,
}

@Serializable
enum class Lifecycle {
    @SerialName("inUse") InUse,
    @SerialName("decommission") Decommission,
}
