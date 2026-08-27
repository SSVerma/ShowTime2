package com.ssverma.feature.filter.ui.hub

import com.ssverma.core.ui.UiState
import com.ssverma.shared.ads.injection.AdInjectable
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
    val heroItems: List<AdInjectable<MediaPreview>> = emptyList(),
    val newItems: List<AdInjectable<MediaPreview>> = emptyList(),
    val upcomingItems: List<AdInjectable<MediaPreview>> = emptyList(),
    val topRatedItems: List<AdInjectable<MediaPreview>> = emptyList(),
    val genres: List<Genre> = emptyList()
)

sealed interface MediaPreview {
    val id: Int
    val title: String
    val posterImageUrl: String
    val backdropImageUrl: String
    val voteAvg: Float
    val displayDate: String?

    data class Movie(val movie: MoviePreview) : MediaPreview {
        override val id: Int get() = movie.id
        override val title: String get() = movie.title
        override val posterImageUrl: String get() = movie.posterImageUrl
        override val backdropImageUrl: String get() = movie.backdropImageUrl
        override val voteAvg: Float get() = movie.voteAvg
        override val displayDate: String? get() = movie.displayReleaseDate
    }

    data class TvShow(val tvShow: TvShowPreview) : MediaPreview {
        override val id: Int get() = tvShow.id
        override val title: String get() = tvShow.title
        override val posterImageUrl: String get() = tvShow.posterImageUrl
        override val backdropImageUrl: String get() = tvShow.backdropImageUrl
        override val voteAvg: Float get() = tvShow.voteAvg
        override val displayDate: String? get() = tvShow.displayFirstAirDate
    }
}
