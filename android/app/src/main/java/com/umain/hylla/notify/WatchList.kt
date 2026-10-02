package com.umain.hylla.notify

import android.content.SharedPreferences
import androidx.core.content.edit
import com.umain.hylla.fleet.DeviceId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Devices the phone holder is waiting for. Local, like the holder themselves. */
class WatchList(private val preferences: SharedPreferences) {
    private val state = MutableStateFlow(preferences.getStringSet(KEY, emptySet()).orEmpty().map(::DeviceId).toSet())
    val watched: StateFlow<Set<DeviceId>> = state.asStateFlow()

    fun toggle(device: DeviceId) = save(if (device in state.value) state.value - device else state.value + device)

    fun remove(device: DeviceId) = save(state.value - device)

    private fun save(next: Set<DeviceId>) {
        preferences.edit { putStringSet(KEY, next.map { it.value }.toSet()) }
        state.value = next
    }

    private companion object {
        const val KEY = "watched"
    }
}
