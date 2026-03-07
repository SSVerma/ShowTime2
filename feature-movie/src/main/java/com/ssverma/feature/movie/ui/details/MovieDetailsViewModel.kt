package com.ssverma.feature.movie.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchYoutubeIntent
import com.ssverma.core.ui.UiState
import com.ssverma.feature.movie.domain.model.MovieDetailsConfig
import com.ssverma.feature.movie.domain.usecase.MovieDetailsUseCase
import com.ssverma.feature.movie.navigation.MovieDetailDestination
import com.ssverma.feature.movie.ui.MovieDetailsUiState
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.movie.imageShots
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieDetailsScreenUiState(
    val movieDetailsUiState: MovieDetailsUiState = UiState.Idle,
    val imageShots: List<ImageShot> = emptyList()
)

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val movieDetailsUseCase: MovieDetailsUseCase
) : AndroidViewModel(application) {

    private val movieId = savedStateHandle.get<Int>(MovieDetailDestination.ArgMovieId) ?: 0

    private val _uiState = MutableStateFlow(MovieDetailsScreenUiState())
    val uiState: StateFlow<MovieDetailsScreenUiState> = _uiState.asStateFlow()

    init {
        fetchMovieDetails()
    }

    fun fetchMovieDetails() {
        _uiState.update { it.copy(movieDetailsUiState = UiState.Loading) }

        val config = MovieDetailsConfig(movieId = movieId)

        viewModelScope.launch {
            val result = movieDetailsUseCase(config)
            _uiState.update {
                when (result) {
                    is Result.Error -> {
                        it.copy(movieDetailsUiState = UiState.Error(result.error))
                    }
                    is Result.Success -> {
                        it.copy(
                            movieDetailsUiState = UiState.Success(result.data),
                            imageShots = result.data.imageShots()
                        )
                    }
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
