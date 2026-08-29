package com.ssverma.showtime.ui.dashboard

import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.ui.UiState
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.community.DailyPoll
import com.ssverma.shared.domain.model.community.TrendingDiscussion
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.domain.model.trakt.CompletedShowDialogState
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode
import com.ssverma.shared.domain.model.tv.TvShowPreview
import java.time.LocalDate

data class TrendingSpotlightItem(
    val id: Int,
    val title: String,
    val posterImageUrl: String,
    val backdropImageUrl: String,
    val voteAvg: Float,
    val displayDate: String?,
    val mediaType: MediaType
)

data class DashboardUiState(
    val trendingMedia: UiState<List<AdInjectable<TrendingSpotlightItem>>, MovieFailure> = UiState.Loading,
    val popularMovies: UiState<List<AdInjectable<MoviePreview>>, MovieFailure> = UiState.Loading,
    val popularTvShows: UiState<List<AdInjectable<TvShowPreview>>, TvShowFailure> = UiState.Loading,
    val movieProviders: UiState<List<ProviderInfo>, Nothing> = UiState.Loading,
    val tvProviders: UiState<List<ProviderInfo>, Nothing> = UiState.Loading,
    val gameStats: CinemaGameStats = CinemaGameStats(),
    val isTodayGameCompleted: Boolean = false,
    val dailyPoll: DailyPoll = DailyPoll.empty(LocalDate.now()),
    val trendingDiscussions: List<TrendingDiscussion> = emptyList(),
    val isMovieStreamingSelected: Boolean = true,
    val isMoviePopularSelected: Boolean = true,
    val upNextQueue: List<TraktUpNextEpisode> = emptyList(),
    val isTraktConnected: Boolean = false,
    val nativeAd: NativeAd? = null,
    val completedShowDialog: CompletedShowDialogState? = null
)
