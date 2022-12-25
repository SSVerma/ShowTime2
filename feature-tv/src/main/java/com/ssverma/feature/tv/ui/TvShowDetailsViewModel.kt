package com.ssverma.feature.tv.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchYoutubeIntent
import com.ssverma.core.ui.UiState
import com.ssverma.feature.tv.domain.model.TvShowDetailsConfig
import com.ssverma.feature.tv.domain.usecase.TvShowDetailsUseCase
import com.ssverma.feature.tv.navigation.TvShowDetailDestination
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.model.tv.imageShots
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvShowDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    application: Application,
    private val tvShowDetailsUseCase: TvShowDetailsUseCase
) : AndroidViewModel(application) {

    val tvShowId = savedStateHandle.get<Int>(TvShowDetailDestination.ArgTvShowId) ?: 0

    var imageShots by mutableStateOf<List<ImageShot>>(emptyList())
        private set

    var tvShowDetailsUiState by mutableStateOf<TvShowDetailsUiState>(UiState.Idle)
        private set

    init {
        fetchTvShowDetails()
    }

    fun fetchTvShowDetails(coroutineScope: CoroutineScope = viewModelScope) {
        tvShowDetailsUiState = UiState.Loading

        val config = TvShowDetailsConfig(tvShowId = tvShowId)

        coroutineScope.launch {
            val result = tvShowDetailsUseCase(config)
            tvShowDetailsUiState = when (result) {
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

    fun onPlayTrailerClicked(tvShow: TvShow) {
        tvShow.videos.firstOrNull()?.let {
            openYoutubeApp(it.key)
        }
    }
}