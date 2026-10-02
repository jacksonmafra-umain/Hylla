package com.umain.hylla.register

import com.umain.hylla.fleet.AssignmentStatus
import com.umain.hylla.fleet.Device
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.DeviceType
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.HomeUse
import com.umain.hylla.fleet.Lifecycle
import com.umain.hylla.fleet.Platform
import com.umain.hylla.posture.FoldOrientation
import com.umain.hylla.posture.WindowPosture
import java.time.LocalDate
import kotlin.math.roundToInt

/** What the running device says about itself. A plain value, so drafts are unit tested. */
data class BuildInfo(
    val manufacturer: String,
    val model: String,
    val release: String,
    /** The name the owner gave the device in Settings, if any. */
    val deviceName: String?,
)

/**
 * A new record drafted from the device the app is running on. The loop that makes Hylla
 * self-demonstrating: the posture APIs the codelab teaches describe the very device being added.
 */
object Registration {
    /** The skin each manufacturer ships, without a version: no public API reports the version. */
    private val skins = mapOf(
        "samsung" to "One UI",
        "xiaomi" to "HyperOS",
        "oppo" to "ColorOS",
        "oneplus" to "OxygenOS",
        "honor" to "MagicOS",
        "huawei" to "EMUI",
    )

    fun draft(build: BuildInfo, posture: WindowPosture, fleet: Fleet, today: LocalDate): Device {
        val manufacturer = build.manufacturer.replaceFirstChar { it.uppercase() }
        val model = if (build.model.startsWith(manufacturer, ignoreCase = true)) build.model else "$manufacturer ${build.model}"
        return Device(
            id = nextTag(fleet),
            deviceName = build.deviceName?.takeIf { it.isNotBlank() } ?: model,
            deviceType = DeviceType.Phone,
            platform = Platform.Android,
            modelName = model,
            modelNumber = build.model,
            osVersion = build.release,
            uiVersion = skins[build.manufacturer.lowercase()],
            assignmentStatus = AssignmentStatus.Available,
            currentUser = null,
            since = today,
            homeUse = HomeUse.Approved,
            lifecycle = Lifecycle.InUse,
            notes = windowNote(posture),
        )
    }

    /** The first shelf tag not taken, `HYL-020` after `HYL-019`. */
    fun nextTag(fleet: Fleet): DeviceId {
        val taken = fleet.devices.mapNotNull { it.id.value.removePrefix("HYL-").toIntOrNull() }.toSet()
        return DeviceId("HYL-%03d".format(generateSequence(1) { it + 1 }.first { it !in taken }))
    }

    /** The window as the posture model saw it at registration, in words. */
    fun windowNote(posture: WindowPosture): String {
        val hinges = posture.hinges.joinToString { hinge ->
            val at = if (hinge.orientation == FoldOrientation.Vertical) hinge.bounds.left else hinge.bounds.top
            "${hinge.orientation.name.lowercase()} at ${at.roundToInt()} dp"
        }
        return buildString {
            append("Registered from the device. Window ${posture.widthDp.roundToInt()} × ${posture.heightDp.roundToInt()} dp, ")
            append("${posture.widthClass.name.lowercase()} × ${posture.heightClass.name.lowercase()}, ")
            append("posture ${posture.posture.name.lowercase()}")
            append(if (posture.folds.isEmpty()) ", no folds." else ", ${posture.folds.size} fold(s)")
            if (hinges.isNotEmpty()) append(", separating: $hinges.") else if (posture.folds.isNotEmpty()) append(".")
        }
    }
}
