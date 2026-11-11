package com.ssverma.shared.ai.a2ui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top-level document container for dynamic Agent-to-UI (A2UI) rendering.
 */
@Serializable
data class A2UiDocument(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String? = null,
    @SerialName("components")
    val components: List<A2UiComponent> = emptyList()
)

/**
 * Sealed hierarchy of UI primitives supported by the ShowTime A2UI Compose renderer.
 */
@Serializable
sealed interface A2UiComponent {

    @Serializable
    @SerialName("text")
    data class Text(
        @SerialName("id")
        val id: String,
        @SerialName("content")
        val content: String,
        @SerialName("isEmphasized")
        val isEmphasized: Boolean = false
    ) : A2UiComponent

    @Serializable
    @SerialName("media_card")
    data class MediaCard(
        @SerialName("id")
        val id: String,
        @SerialName("tmdbId")
        val tmdbId: Int,
        @SerialName("title")
        val title: String,
        @SerialName("posterPath")
        val posterPath: String? = null,
        @SerialName("releaseYear")
        val releaseYear: String? = null,
        @SerialName("rating")
        val rating: Float = 0.0f,
        @SerialName("overview")
        val overview: String? = null
    ) : A2UiComponent

    @Serializable
    @SerialName("media_carousel")
    data class MediaCarousel(
        @SerialName("id")
        val id: String,
        @SerialName("title")
        val title: String,
        @SerialName("items")
        val items: List<MediaCard> = emptyList()
    ) : A2UiComponent

    @Serializable
    @SerialName("chip_group")
    data class ChipGroup(
        @SerialName("id")
        val id: String,
        @SerialName("title")
        val title: String? = null,
        @SerialName("chips")
        val chips: List<A2UiChip> = emptyList()
    ) : A2UiComponent

    @Serializable
    @SerialName("action_button")
    data class ActionButton(
        @SerialName("id")
        val id: String,
        @SerialName("label")
        val label: String,
        @SerialName("action")
        val action: A2UiAction
    ) : A2UiComponent
}

/**
 * Chip item representation inside [A2UiComponent.ChipGroup].
 */
@Serializable
data class A2UiChip(
    @SerialName("id")
    val id: String,
    @SerialName("label")
    val label: String,
    @SerialName("action")
    val action: A2UiAction? = null
)

/**
 * Interactive actions triggered by user taps on dynamic A2UI components.
 */
@Serializable
sealed interface A2UiAction {

    @Serializable
    @SerialName("open_media")
    data class OpenMedia(
        @SerialName("tmdbId")
        val tmdbId: Int,
        @SerialName("mediaType")
        val mediaType: String = "movie"
    ) : A2UiAction

    @Serializable
    @SerialName("toggle_watchlist")
    data class ToggleWatchlist(
        @SerialName("tmdbId")
        val tmdbId: Int,
        @SerialName("title")
        val title: String,
        @SerialName("mediaType")
        val mediaType: String = "movie"
    ) : A2UiAction

    @Serializable
    @SerialName("prompt_query")
    data class PromptQuery(
        @SerialName("prompt")
        val prompt: String
    ) : A2UiAction
}
