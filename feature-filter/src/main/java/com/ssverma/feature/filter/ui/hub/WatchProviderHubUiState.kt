package com.ssverma.feature.filter.ui.hub

import com.ssverma.core.ui.UiState
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.domain.model.tv.TvShowPreview

data class WatchProviderHubUiState(
    val provider: ProviderInfo,
    val themedColor: Int? = null,
    val isMovieMode: Boolean = true,
    val hubContentState: UiState<HubContent, Failure.CoreFailure> = UiState.Idle
)

data class HubContent(
    val heroItems: List<MediaPreview> = emptyList(),
    val newItems: List<MediaPreview> = emptyList(),
    val upcomingItems: List<MediaPreview> = emptyList(),
    val topRatedItems: List<MediaPreview> = emptyList(),
    val genres: List<Genre> = emptyList()
)

sealed interface MediaPreview {
    data class Movie(val movie: MoviePreview) : MediaPreview
    data class TvShow(val tvShow: TvShowPreview) : MediaPreview
}

enum class WatchProviderHubSeeAllType {
    NewThisWeek,
    Upcoming,
    TopRated
}
