package com.ssverma.feature.movie.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ssverma.core.domain.Result
import com.ssverma.core.domain.model.ImageShot
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchYoutubeIntent
import com.ssverma.core.ui.UiState
import com.ssverma.feature.movie.domain.model.Movie
import com.ssverma.feature.movie.domain.model.MovieDetailsConfig
import com.ssverma.feature.movie.domain.model.imageShots
import com.ssverma.feature.movie.domain.usecase.MovieDetailsUseCase
import com.ssverma.feature.movie.navigation.MovieDetailDestination
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

    private val movieId = savedStateHandle.get<Int>(MovieDetailDestination.ArgMovieId) ?: 0

    var imageShots by mutableStateOf<List<ImageShot>>(emptyList())

    var movieDetailsUiState by mutableStateOf<MovieDetailsUiState>(UiState.Idle)
        private set

    init {
        fetchMovieDetails()
    }

    fun fetchMovieDetails(coroutineScope: CoroutineScope = viewModelScope) {
        movieDetailsUiState = UiState.Loading

        val config = MovieDetailsConfig(movieId = movieId)

        coroutineScope.launch {
            val result = movieDetailsUseCase(config)
            movieDetailsUiState = when (result) {
                is Result.Error -> {
                    UiState.Error(result.error)
                }
                is Result.Success -> {
                    imageShots = result.data.imageShots()
                    UiState.Success(result.data)
                }
            }
        }
    }

    fun openYoutubeApp(videoId: String) {
        getApplication<Application>()
            .dispatchYoutubeIntent(videoId = videoId)
    }

    fun onPlayTrailerClicked(movie: Movie) {
        movie.videos.firstOrNull()?.let {
            openYoutubeApp(it.key)
        }
    }
}