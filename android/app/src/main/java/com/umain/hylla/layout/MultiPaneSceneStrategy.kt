package com.umain.hylla.layout

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/** How many panes are showing. 1 means one at a time; above 1 an up button would be wrong. */
val LocalPaneCount = compositionLocalOf { 1 }

/** Which pane an entry belongs to, set through `NavEntry.metadata`. */
enum class PaneRole {
    List, Detail;

    /** [subject] is what a supporting pane shows alongside this entry, such as a device id. */
    fun metadata(subject: Any? = null): Map<String, Any> =
        buildMap {
            put(KEY, this@PaneRole)
            if (subject != null) put(SUBJECT, subject)
        }

    companion object {
        const val KEY = "hylla.pane"
        const val SUBJECT = "hylla.subject"
        fun of(entry: NavEntry<*>): PaneRole? = entry.metadata[KEY] as? PaneRole
    }
}

/**
 * Lays the list entry and the top detail entry side by side when [layout] has two or three panes.
 *
 * The back stack does not change shape: it is still `[Fleet, Device]`. Only how it is shown does.
 * With no detail on the stack, the detail pane shows [placeholder]. A third pane shows [supporting]
 * for the detail's subject; it is not a back stack entry, because nothing navigates to it.
 * Returning null hands the stack back to Navigation 3's one-at-a-time default.
 */
class MultiPaneSceneStrategy<T : Any>(
    private val layout: PaneLayout,
    private val placeholder: @Composable () -> Unit,
    private val supporting: @Composable (subject: Any?) -> Unit,
) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (layout.paneCount < 2) return null
        val last = entries.lastOrNull() ?: return null
        val (list, detail) = when (PaneRole.of(last)) {
            PaneRole.List -> last to null
            PaneRole.Detail -> {
                val list = entries.getOrNull(entries.lastIndex - 1)?.takeIf { PaneRole.of(it) == PaneRole.List }
                    ?: return null
                list to last
            }
            null -> return null
        }
        return MultiPaneScene(
            key = list.contentKey,
            list = list,
            detail = detail,
            previousEntries = entries.dropLast(1),
            layout = layout,
            placeholder = placeholder,
            supporting = supporting,
        )
    }
}

/** A data class so Navigation 3 can tell "same panes, new detail" from a new scene. */
private data class MultiPaneScene<T : Any>(
    override val key: Any,
    private val list: NavEntry<T>,
    private val detail: NavEntry<T>?,
    override val previousEntries: List<NavEntry<T>>,
    private val layout: PaneLayout,
    private val placeholder: @Composable () -> Unit,
    private val supporting: @Composable (subject: Any?) -> Unit,
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOfNotNull(list, detail)

    override val content: @Composable () -> Unit = {
        val panes = layout.panes
        val contents: List<@Composable () -> Unit> = listOf(
            { list.Content() },
            { if (detail != null) detail.Content() else placeholder() },
            { supporting(detail?.metadata?.get(PaneRole.SUBJECT)) },
        )
        CompositionLocalProvider(LocalPaneCount provides panes.size) {
            Row(Modifier.fillMaxSize()) {
                panes.forEachIndexed { index, pane ->
                    if (index > 0) {
                        // An occluding hinge leaves a gap; a seamless one or a flat split gets a divider.
                        val gap = pane.left - panes[index - 1].right
                        if (gap > 0f) Spacer(Modifier.width(gap.dp)) else VerticalDivider()
                    }
                    val width = if (index == panes.lastIndex) Modifier.weight(1f) else Modifier.width(pane.width.dp)
                    // Each pane is a focus group: Tab moves between panes, arrows move inside one.
                    Box(width.fillMaxHeight().focusGroup()) { contents[index]() }
                }
            }
        }
    }
}
