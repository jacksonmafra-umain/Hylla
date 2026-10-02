package com.umain.hylla.permission

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionStateTest {
    @Test
    fun `granted wins whatever else is true`() {
        assertEquals(PermissionState.Granted, PermissionState.of(granted = true, askedBefore = true, shouldShowRationale = false))
    }

    @Test
    fun `never asked and blocked look the same to Android, the asked flag tells them apart`() {
        assertEquals(PermissionState.NotAsked, PermissionState.of(granted = false, askedBefore = false, shouldShowRationale = false))
        assertEquals(PermissionState.Blocked, PermissionState.of(granted = false, askedBefore = true, shouldShowRationale = false))
    }

    @Test
    fun `denied once can still be asked`() {
        assertEquals(PermissionState.Denied, PermissionState.of(granted = false, askedBefore = true, shouldShowRationale = true))
    }
}
