package com.ssverma.showtime.ui.tv

import com.ssverma.showtime.domain.failure.tv.TvEpisodeFailure
import com.ssverma.showtime.domain.failure.tv.TvSeasonFailure
import com.ssverma.showtime.domain.failure.tv.TvShowFailure
import com.ssverma.showtime.domain.model.tv.TvEpisode
import com.ssverma.showtime.domain.model.tv.TvSeason
import com.ssverma.showtime.domain.model.tv.TvShow
import com.ssverma.showtime.ui.FetchDataUiState

typealias TvShowListUiState = FetchDataUiState<List<TvShow>, TvShowFailure>

typealias TvShowDetailsUiState = FetchDataUiState<TvShow, TvShowFailure>

typealias TvSeasonUiState = FetchDataUiState<TvSeason, TvSeasonFailure>

typealias TvEpisodeUiState = FetchDataUiState<TvEpisode, TvEpisodeFailure>