package com.ssverma.showtime.ui.movie

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.domain.model.movie.MovieDetailsConfig
import com.ssverma.showtime.domain.usecase.movie.MovieDetailsUseCase
import com.ssverma.showtime.navigation.AppDestination
import com.ssverma.showtime.ui.FetchDataUiState
import com.ssverma.showtime.utils.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    val movieDetailsUseCase: MovieDetailsUseCase
) : AndroidViewModel(application) {

    private val movieId = savedStateHandle.get<Int>(AppDestination.MovieDetails.ArgMovieId) ?: 0

    private val _liveImageShots = MediatorLiveData<List<ImageShot>>()
    val imageShots: LiveData<List<ImageShot>> get() = _liveImageShots

    var movieDetailsUiState by mutableStateOf<MovieDetailsUiState>(FetchDataUiState.Idle)
        private set

    init {
        fetchMovieDetails()
    }

    fun fetchMovieDetails(coroutineScope: CoroutineScope = viewModelScope) {
        movieDetailsUiState = FetchDataUiState.Loading

        val config = MovieDetailsConfig(movieId = movieId)

        coroutineScope.launch {
            val result = movieDetailsUseCase(config)
            movieDetailsUiState = when (result) {
                is DomainResult.Error -> {
                    FetchDataUiState.Error(result.error)
                }
                is DomainResult.Success -> {
                    FetchDataUiState.Success(result.data)
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