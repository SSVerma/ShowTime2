package com.ssverma.feature.tv.ui.common

import com.ssverma.core.ui.UiState
import com.ssverma.feature.tv.domain.failure.TvEpisodeFailure
import com.ssverma.feature.tv.domain.failure.TvSeasonFailure
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.tv.TvEpisode
import com.ssverma.shared.domain.model.tv.TvSeason
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.model.tv.TvShowPreview

typealias TvShowListUiState = UiState<List<TvShow>, TvShowFailure>

typealias TvShowPreviewUiState = UiState<List<TvShowPreview>, TvShowFailure>

typealias TvShowDetailsUiState = UiState<TvShow, TvShowFailure>

typealias TvSeasonUiState = UiState<TvSeason, TvSeasonFailure>

typealias TvEpisodeUiState = UiState<TvEpisode, TvEpisodeFailure>

typealias GenresUiState = UiState<List<Genre>, Failure.CoreFailure>
