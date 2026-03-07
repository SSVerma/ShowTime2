package com.ssverma.feature.tv.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchYoutubeIntent
import com.ssverma.core.ui.UiState
import com.ssverma.feature.tv.domain.model.TvShowDetailsConfig
import com.ssverma.feature.tv.domain.usecase.TvShowDetailsUseCase
import com.ssverma.feature.tv.navigation.TvShowDetailDestination
import com.ssverma.feature.tv.ui.TvShowDetailsUiState
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.model.tv.imageShots
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TvShowDetailsScreenUiState(
    val tvShowDetailsUiState: TvShowDetailsUiState = UiState.Idle,
    val imageShots: List<ImageShot> = emptyList()
)

@HiltViewModel
class TvShowDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    application: Application,
    private val tvShowDetailsUseCase: TvShowDetailsUseCase
) : AndroidViewModel(application) {

    val tvShowId = savedStateHandle.get<Int>(TvShowDetailDestination.ArgTvShowId) ?: 0

    private val _uiState = MutableStateFlow(TvShowDetailsScreenUiState())
    val uiState: StateFlow<TvShowDetailsScreenUiState> = _uiState.asStateFlow()

    init {
        fetchTvShowDetails()
    }

    fun fetchTvShowDetails() {
        _uiState.update { it.copy(tvShowDetailsUiState = UiState.Loading) }

        val config = TvShowDetailsConfig(tvShowId = tvShowId)

        viewModelScope.launch {
            val result = tvShowDetailsUseCase(config)
            _uiState.update {
                when (result) {
                    is Result.Error -> {
                        it.copy(tvShowDetailsUiState = UiState.Error(result.error))
                    }
                    is Result.Success -> {
                        it.copy(
                            tvShowDetailsUiState = UiState.Success(result.data),
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

    fun onPlayTrailerClicked(tvShow: TvShow) {
        tvShow.videos.firstOrNull()?.let {
            openYoutubeApp(it.key)
        }
    }
}
