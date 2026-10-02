package com.umain.hylla.fleet

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Who is holding this phone. Stored on the device only: it decides whose devices the cover
 * surface shows and who a claim is for by default. Not an account, and never synced.
 */
class MeStore(private val preferences: SharedPreferences) {
    private val state = MutableStateFlow(preferences.getString(KEY, null)?.let(::PersonId))
    val me: StateFlow<PersonId?> = state.asStateFlow()

    fun set(person: PersonId?) {
        preferences.edit { if (person == null) remove(KEY) else putString(KEY, person.value) }
        state.value = person
    }

    private companion object {
        const val KEY = "me"
    }
}
