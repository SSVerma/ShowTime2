package com.ssverma.showtime.ui.movie

import com.ssverma.showtime.domain.failure.movie.MovieFailure
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.core.ui.UiState

//typealias don't support nested sealed classes
// Can't access -> MovieListUiState.Loading
typealias MovieListUiState = UiState<List<Movie>, MovieFailure>

typealias GenresUiState = UiState<List<Genre>, Nothing>

typealias MovieDetailsUiState = UiState<Movie, MovieFailure>