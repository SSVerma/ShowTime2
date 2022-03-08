package com.ssverma.showtime.ui.movie

import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.domain.movie.MovieFailure
import com.ssverma.showtime.ui.FetchDataUiState

//typealias don't support nested sealed classes
// Can't access -> MovieListUiState.Loading
typealias MovieListUiState = FetchDataUiState<List<Movie>, MovieFailure>