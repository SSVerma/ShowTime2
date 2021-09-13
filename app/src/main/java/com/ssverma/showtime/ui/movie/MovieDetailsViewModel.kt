package com.ssverma.showtime.ui.movie

import android.app.Application
import androidx.lifecycle.*
import com.ssverma.showtime.api.AppendableQueryMap
import com.ssverma.showtime.api.QueryMultiValue
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.data.repository.MovieRepository
import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.domain.model.imageShots
import com.ssverma.showtime.navigation.AppDestination
import com.ssverma.showtime.utils.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
    savedStateHandle: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {

    private val movieId = savedStateHandle.get<Int>(AppDestination.MovieDetails.ArgMovieId) ?: 0

    private val _liveImageShots = MediatorLiveData<List<ImageShot>>()
    val imageShots: LiveData<List<ImageShot>> get() = _liveImageShots

    val liveMovieDetails: LiveData<Result<Movie>> = liveData {
        movieRepository.fetchMovieDetails(
            movieId = movieId,
            queryMap = AppendableQueryMap.of(appendToResponse = movieDetailsAppendable)
        ).collect {
            emit(it)

            if (it is Result.Success) {
                viewModelScope.launch {
                    val imageShots = it.data.imageShots()
                    _liveImageShots.postValue(imageShots)
                }
            }
        }
    }

    fun openYoutubeApp(videoId: String) {
        AppUtils.dispatchOpenYoutubeIntent(getApplication(), videoId)
    }

    fun onPlayTrailerClicked(movie: Movie) {
        movie.videos.firstOrNull()?.let {
            openYoutubeApp(it.key)
        }
    }
}