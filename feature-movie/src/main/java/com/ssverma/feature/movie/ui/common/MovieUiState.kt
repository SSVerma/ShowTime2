package com.ssverma.feature.movie.ui.common

import com.ssverma.core.ui.UiState
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.movie.MoviePreview

//typealias don't support nested sealed classes
// Can't access -> MovieListUiState.Loading
typealias MovieListUiState = UiState<List<Movie>, MovieFailure>

typealias MoviePreviewUiState = UiState<List<AdInjectable<MoviePreview>>, MovieFailure>

typealias GenresUiState = UiState<List<Genre>, Failure.CoreFailure>

typealias MovieDetailsUiState = UiState<Movie, MovieFailure>