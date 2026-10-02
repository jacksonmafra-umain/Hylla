package com.umain.hylla.permission

/**
 * Where a runtime permission stands, from the three things Android tells us.
 *
 * Android does not say "never asked" and "denied for good" apart: both have
 * `shouldShowRequestPermissionRationale` false. Remembering whether we asked tells them apart.
 */
enum class PermissionState {
    /** Not asked yet: explain, then ask. */
    NotAsked,

    Granted,

    /** Denied once; the system will still show its dialog. Explain why before asking again. */
    Denied,

    /** Denied with "don't ask again", or turned off in Settings. Only Settings can change it. */
    Blocked;

    companion object {
        fun of(granted: Boolean, askedBefore: Boolean, shouldShowRationale: Boolean): PermissionState = when {
            granted -> Granted
            !askedBefore -> NotAsked
            shouldShowRationale -> Denied
            else -> Blocked
        }
    }
}
