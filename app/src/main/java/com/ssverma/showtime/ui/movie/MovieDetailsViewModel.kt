package com.ssverma.showtime.ui.movie

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ssverma.showtime.api.MovieDetailsQueryMap
import com.ssverma.showtime.api.QueryMultiValue
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.data.repository.MovieRepository
import com.ssverma.showtime.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

val movieDetailsAppendable = QueryMultiValue.andBuilder()
    .and(TmdbApiTiedConstants.MovieDetailsAppendableResponseTypes.Credits)
    .and(TmdbApiTiedConstants.MovieDetailsAppendableResponseTypes.Images)
    .and(TmdbApiTiedConstants.MovieDetailsAppendableResponseTypes.Videos)
    .and(TmdbApiTiedConstants.MovieDetailsAppendableResponseTypes.Reviews)
    .and(TmdbApiTiedConstants.MovieDetailsAppendableResponseTypes.Similar)
    .and(TmdbApiTiedConstants.MovieDetailsAppendableResponseTypes.Keywords)

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    movieRepository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId = savedStateHandle.get<Int>(AppDestination.MovieDetails.ArgMovieId) ?: 0

    val liveMockMovie = movieRepository.fetchMovieDetails(
        movieId = movieId,
        queryMap = MovieDetailsQueryMap.of(appendToResponse = movieDetailsAppendable)
    ).asLiveData()

}