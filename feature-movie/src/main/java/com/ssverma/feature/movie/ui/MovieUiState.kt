package com.ssverma.feature.movie.ui

import com.ssverma.core.domain.model.Genre
import com.ssverma.core.ui.UiState
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.movie.domain.model.Movie

//typealias don't support nested sealed classes
// Can't access -> MovieListUiState.Loading
typealias MovieListUiState = UiState<List<Movie>, MovieFailure>

typealias GenresUiState = UiState<List<Genre>, Nothing>

typealias MovieDetailsUiState = UiState<Movie, MovieFailure>