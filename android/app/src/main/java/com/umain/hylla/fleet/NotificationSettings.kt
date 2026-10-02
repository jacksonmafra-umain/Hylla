package com.umain.hylla.fleet

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Whether the holder of this phone wants Hylla's notifications. Separate from the system
 * permission: the permission says Hylla may post, this says the person wants it to.
 */
class NotificationSettings(private val preferences: SharedPreferences) {
    private val state = MutableStateFlow(preferences.getBoolean(KEY, false))
    val enabled: StateFlow<Boolean> = state.asStateFlow()

    fun set(enabled: Boolean) {
        preferences.edit { putBoolean(KEY, enabled) }
        state.value = enabled
    }

    private companion object {
        const val KEY = "notifications.enabled"
    }
}
