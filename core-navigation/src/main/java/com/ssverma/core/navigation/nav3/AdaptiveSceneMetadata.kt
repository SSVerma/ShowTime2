package com.ssverma.core.navigation.nav3

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * Marker interface for primary media/person detail destinations eligible for multi-pane replacement
 * when triggered from an adjacent visible list pane.
 */
interface AdaptiveDetailNavKey : NavKey

/**
 * Marker interface for secondary detail/extra destinations (reviews, discussions, seasons, episodes)
 * eligible for multi-pane rendering beside a detail or list pane.
 */
interface AdaptiveExtraNavKey : NavKey

private const val SharedListDetailSceneKey = "ShowTimeListDetailScene"

/**
 * Metadata marking an entry as a List Pane within ShowTime's shared list-detail workspace.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun adaptiveListPaneMetadata(
    alwaysShowSplit: Boolean = false,
    detailPlaceholder: (@Composable () -> Unit)? = null
): Map<String, Any> {
    val baseMetadata = if (detailPlaceholder != null) {
        ListDetailSceneStrategy.listPane(
            sceneKey = SharedListDetailSceneKey,
            detailPlaceholder = { detailPlaceholder() }
        )
    } else {
        ListDetailSceneStrategy.listPane(sceneKey = SharedListDetailSceneKey)
    }
    val participantMetadata =
        baseMetadata + (Nav3MetadataKeys.ListDetailParticipant.toString() to true)
    return if (alwaysShowSplit) {
        participantMetadata + (Nav3MetadataKeys.AlwaysShowSplit.toString() to true)
    } else {
        participantMetadata
    }
}

/**
 * Metadata marking an entry as a Detail Pane within ShowTime's shared list-detail workspace.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun adaptiveDetailPaneMetadata(): Map<String, Any> {
    return ListDetailSceneStrategy.detailPane(sceneKey = SharedListDetailSceneKey) +
            (Nav3MetadataKeys.ListDetailParticipant.toString() to true)
}

/**
 * Metadata marking an entry as an Extra Pane within ShowTime's shared list-detail workspace.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun adaptiveExtraPaneMetadata(): Map<String, Any> {
    return ListDetailSceneStrategy.extraPane(sceneKey = SharedListDetailSceneKey) +
            (Nav3MetadataKeys.ListDetailParticipant.toString() to true)
}

/**
 * Activates Material 3 two-pane List-Detail rendering only when at least two consecutive panes
 * participate (e.g. List + Detail or Detail + Extra), or when an entry explicitly requests a
 * split workspace placeholder (such as Search). Browse grids without an active detail pane stay
 * full-width so every column of posters remains visible.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class ShowTimeListDetailSceneStrategy(
    private val delegate: ListDetailSceneStrategy<NavKey>
) : SceneStrategy<NavKey> {
    override fun SceneStrategyScope<NavKey>.calculateScene(
        entries: List<NavEntry<NavKey>>
    ): Scene<NavKey>? {
        if (!shouldActivateListDetailScene(entries = entries)) {
            return null
        }

        return with(delegate) {
            calculateScene(entries = entries)
        }
    }
}

internal fun shouldActivateListDetailScene(entries: List<NavEntry<NavKey>>): Boolean {
    val lastEntry = entries.lastOrNull() ?: return false
    val forceSplit = lastEntry.metadata[Nav3MetadataKeys.AlwaysShowSplit.toString()] == true
    if (forceSplit) return true

    val lastNavKey = lastEntry.metadata[Nav3MetadataKeys.NavKey.toString()] as? NavKey
    val isDetailOrExtraOnTop =
        lastNavKey is AdaptiveDetailNavKey || lastNavKey is AdaptiveExtraNavKey
    if (!isDetailOrExtraOnTop) return false

    var trailingPaneCount = 0
    for (entryIndex in entries.lastIndex downTo 0) {
        val metadata = entries[entryIndex].metadata
        if (metadata[Nav3MetadataKeys.ListDetailParticipant.toString()] == true) {
            trailingPaneCount++
        } else {
            break
        }
    }

    return trailingPaneCount >= 2
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberShowTimeListDetailSceneStrategy(
    directive: PaneScaffoldDirective
): SceneStrategy<NavKey> {
    val delegate = rememberListDetailSceneStrategy<NavKey>(
        backNavigationBehavior = BackNavigationBehavior.PopLatest,
        directive = directive
    )
    return remember(delegate) { ShowTimeListDetailSceneStrategy(delegate = delegate) }
}
