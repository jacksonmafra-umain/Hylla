package com.umain.hylla.ui

import androidx.annotation.StringRes
import com.umain.hylla.R
import com.umain.hylla.fleet.AssignmentStatus
import com.umain.hylla.fleet.DeviceType
import com.umain.hylla.fleet.HomeUse
import com.umain.hylla.fleet.Lifecycle
import com.umain.hylla.fleet.Platform

// Fixture codes become display text here and nowhere else.

val Platform.label: Int
    @StringRes get() = when (this) {
        Platform.IOS -> R.string.platform_ios
        Platform.IPadOS -> R.string.platform_ipados
        Platform.Android -> R.string.platform_android
        Platform.VisionOS -> R.string.platform_visionos
        Platform.MacOS -> R.string.platform_macos
        Platform.CarPlay -> R.string.platform_carplay
    }

val DeviceType.label: Int
    @StringRes get() = when (this) {
        DeviceType.Phone -> R.string.device_type_phone
        DeviceType.Tablet -> R.string.device_type_tablet
        DeviceType.Headset -> R.string.device_type_headset
        DeviceType.Desktop -> R.string.device_type_desktop
    }

val AssignmentStatus.label: Int
    @StringRes get() = when (this) {
        AssignmentStatus.InUse -> R.string.status_in_use
        AssignmentStatus.Available -> R.string.status_available
        AssignmentStatus.Missing -> R.string.status_missing
        AssignmentStatus.NeedsSorting -> R.string.status_needs_sorting
    }

val HomeUse.label: Int
    @StringRes get() = when (this) {
        HomeUse.Approved -> R.string.home_use_approved
        HomeUse.OfficeOnly -> R.string.home_use_office_only
        HomeUse.NotApproved -> R.string.home_use_not_approved
    }

val Lifecycle.label: Int
    @StringRes get() = when (this) {
        Lifecycle.InUse -> R.string.lifecycle_in_use
        Lifecycle.Decommission -> R.string.lifecycle_decommission
    }
