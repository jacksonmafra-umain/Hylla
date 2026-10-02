package com.umain.hylla.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Transient feedback inside the app: a snackbar, with an action when the change can be undone.
 *
 * A snackbar, not a toast. The app is on screen, and the message is about something just done in
 * it, so it belongs in the app's own UI: it can carry an Undo, it sits above the navigation bar,
 * and with TalkBack on, Material 3 holds it on screen for the system's recommended time. A toast
 * is for when there is no app UI to put the message in.
 */
class Feedback(private val host: SnackbarHostState, private val scope: CoroutineScope) {
    fun show(message: String, undoLabel: String? = null, onUndo: () -> Unit = {}) {
        scope.launch {
            host.currentSnackbarData?.dismiss()
            val result = host.showSnackbar(
                message = message,
                actionLabel = undoLabel,
                withDismissAction = undoLabel == null,
                duration = if (undoLabel != null) SnackbarDuration.Long else SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) onUndo()
        }
    }
}

val LocalFeedback = staticCompositionLocalOf<Feedback> { error("No Feedback provided") }
