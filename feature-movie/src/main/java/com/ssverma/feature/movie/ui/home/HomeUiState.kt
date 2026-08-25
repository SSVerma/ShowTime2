package com.ssverma.feature.movie.ui.home

import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.ui.UiState
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.shared.domain.model.movie.MoviePreview

data class HomeUiState(
    val trendingMovies: UiState<List<AdInjectable<MoviePreview>>, MovieFailure> = UiState.Idle,
    val topRatedMovies: UiState<List<AdInjectable<MoviePreview>>, MovieFailure> = UiState.Idle,
    val popularMovies: UiState<List<AdInjectable<MoviePreview>>, MovieFailure> = UiState.Idle,
    val inCinemasMovies: UiState<List<AdInjectable<MoviePreview>>, MovieFailure> = UiState.Idle,
    val upcomingMovies: UiState<List<AdInjectable<MoviePreview>>, MovieFailure> = UiState.Idle,
    val genres: UiState<List<Genre>, Failure.CoreFailure> = UiState.Idle,
    val watchProviders: UiState<List<ProviderInfo>, Failure.CoreFailure> = UiState.Idle,
    val watchProviderAd: NativeAd? = null,
    val feedInlineAd: NativeAd? = null,
    val gameStats: CinemaGameStats = CinemaGameStats(),
    val isTodayGameCompleted: Boolean = false,
)
