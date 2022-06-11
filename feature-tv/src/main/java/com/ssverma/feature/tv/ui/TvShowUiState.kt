package com.ssverma.feature.tv.ui

import com.ssverma.shared.domain.model.Genre
import com.ssverma.core.ui.UiState
import com.ssverma.feature.tv.domain.failure.TvEpisodeFailure
import com.ssverma.feature.tv.domain.failure.TvSeasonFailure
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.feature.tv.domain.model.TvEpisode
import com.ssverma.feature.tv.domain.model.TvSeason
import com.ssverma.feature.tv.domain.model.TvShow

typealias TvShowListUiState = UiState<List<TvShow>, TvShowFailure>

typealias TvShowDetailsUiState = UiState<TvShow, TvShowFailure>

typealias TvSeasonUiState = UiState<TvSeason, TvSeasonFailure>

typealias TvEpisodeUiState = UiState<TvEpisode, TvEpisodeFailure>

typealias GenresUiState = UiState<List<Genre>, Nothing>