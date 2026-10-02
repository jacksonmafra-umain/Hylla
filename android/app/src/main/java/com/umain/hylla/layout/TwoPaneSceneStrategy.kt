package com.umain.hylla.layout

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

/** True inside a pane that sits beside another pane, where an up button would be wrong. */
val LocalInMultiPane = compositionLocalOf { false }

/** Which pane an entry belongs to, set through `NavEntry.metadata`. */
enum class PaneRole {
    List, Detail;

    fun metadata(): Map<String, Any> = mapOf(KEY to this)

    companion object {
        const val KEY = "hylla.pane"
        fun of(entry: NavEntry<*>): PaneRole? = entry.metadata[KEY] as? PaneRole
    }
}

/**
 * Shows the list and the selected detail side by side when [layout] has two panes.
 *
 * The back stack does not change shape: it is still `[Fleet, Device]`. Only how it is shown does.
 * With no detail on the stack, the second pane shows [placeholder]. Returning null hands the
 * stack back to Navigation 3's single-pane default.
 */
class TwoPaneSceneStrategy<T : Any>(
    private val layout: PaneLayout,
    private val placeholder: @Composable () -> Unit,
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
        return TwoPaneScene(
            key = list.contentKey,
            list = list,
            detail = detail,
            previousEntries = entries.dropLast(1),
            layout = layout,
            placeholder = placeholder,
        )
    }
}

/** A data class so Navigation 3 can tell "same panes, new detail" from a new scene. */
private data class TwoPaneScene<T : Any>(
    override val key: Any,
    private val list: NavEntry<T>,
    private val detail: NavEntry<T>?,
    override val previousEntries: List<NavEntry<T>>,
    private val layout: PaneLayout,
    private val placeholder: @Composable () -> Unit,
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOfNotNull(list, detail)

    override val content: @Composable () -> Unit = {
        val (first, second) = layout.panes
        CompositionLocalProvider(LocalInMultiPane provides true) {
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.width(first.width.dp).fillMaxHeight()) { list.Content() }
                // An occluding hinge leaves a gap; a seamless one or a flat split gets a divider.
                val gap = second.left - first.right
                if (gap > 0f) Spacer(Modifier.width(gap.dp)) else VerticalDivider()
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    if (detail != null) detail.Content() else placeholder()
                }
            }
        }
    }
}
