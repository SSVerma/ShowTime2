package com.ssverma.showtime.ui.movie

import com.ssverma.showtime.domain.failure.movie.MovieFailure
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.ui.FetchDataUiState

//typealias don't support nested sealed classes
// Can't access -> MovieListUiState.Loading
typealias MovieListUiState = FetchDataUiState<List<Movie>, MovieFailure>

typealias GenresUiState = FetchDataUiState<List<Genre>, Nothing>

typealias MovieDetailsUiState = FetchDataUiState<Movie, MovieFailure>