package com.ssverma.showtime.ui.tv

import com.ssverma.showtime.domain.failure.tv.TvEpisodeFailure
import com.ssverma.showtime.domain.failure.tv.TvSeasonFailure
import com.ssverma.showtime.domain.failure.tv.TvShowFailure
import com.ssverma.showtime.domain.model.tv.TvEpisode
import com.ssverma.showtime.domain.model.tv.TvSeason
import com.ssverma.showtime.domain.model.tv.TvShow
import com.ssverma.core.ui.UiState

typealias TvShowListUiState = UiState<List<TvShow>, TvShowFailure>

typealias TvShowDetailsUiState = UiState<TvShow, TvShowFailure>

typealias TvSeasonUiState = UiState<TvSeason, TvSeasonFailure>

typealias TvEpisodeUiState = UiState<TvEpisode, TvEpisodeFailure>