package com.ssverma.feature.movie.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchYoutubeIntent
import com.ssverma.core.ui.UiState
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.movie.domain.model.MovieDetailsConfig
import com.ssverma.feature.movie.domain.usecase.MovieDetailsUseCase
import com.ssverma.feature.movie.navigation.MovieDetailDestination
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.movie.imageShots
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieDetailsData(
    val movie: Movie,
    val imageShots: List<ImageShot>
)

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val movieDetailsUseCase: MovieDetailsUseCase
) : AndroidViewModel(application) {

    private val movieId = savedStateHandle.get<Int>(MovieDetailDestination.ArgMovieId) ?: 0

    private val _uiState = MutableStateFlow<UiState<MovieDetailsData, MovieFailure>>(UiState.Idle)
    val uiState: StateFlow<UiState<MovieDetailsData, MovieFailure>> = _uiState.asStateFlow()

    val imageShots: StateFlow<List<ImageShot>> = uiState
        .map { (it as? UiState.Success)?.data?.imageShots ?: emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        fetchMovieDetails()
    }

    fun fetchMovieDetails() {
        _uiState.update { UiState.Loading }

        val config = MovieDetailsConfig(movieId = movieId)

        viewModelScope.launch {
            val result = movieDetailsUseCase(config)
            _uiState.update {
                when (result) {
                    is Result.Error -> UiState.Error(result.error)
                    is Result.Success -> UiState.Success(
                        MovieDetailsData(
                            movie = result.data,
                            imageShots = result.data.imageShots()
                        )
                    )
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
