package com.ssverma.showtime.ui.tv

import com.ssverma.showtime.domain.failure.tv.TvShowFailure
import com.ssverma.showtime.domain.model.TvShow
import com.ssverma.showtime.ui.FetchDataUiState

typealias TvShowListUiState = FetchDataUiState<List<TvShow>, TvShowFailure>

typealias TvShowDetailsUiState = FetchDataUiState<TvShow, TvShowFailure>