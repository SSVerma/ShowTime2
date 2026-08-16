package com.ssverma.feature.tv.ui.details

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchYoutubeIntent
import com.ssverma.core.ui.UiState
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.feature.tv.domain.model.TvShowDetailsConfig
import com.ssverma.feature.tv.domain.usecase.TvShowDetailsUseCase
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.model.tv.imageShots
import com.ssverma.shared.domain.repository.AppConfigRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TvShowDetailsData(
    val tvShow: TvShow,
    val imageShots: List<ImageShot>
)

@HiltViewModel(assistedFactory = TvShowDetailsViewModel.Factory::class)
class TvShowDetailsViewModel @AssistedInject constructor(
    private val application: Application,
    @Assisted val tvShowId: Int,
    private val tvShowDetailsUseCase: TvShowDetailsUseCase,
    val appConfigRepository: AppConfigRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(tvShowId: Int): TvShowDetailsViewModel
    }

    private val _uiState = MutableStateFlow<UiState<TvShowDetailsData, TvShowFailure>>(UiState.Idle)
    val uiState: StateFlow<UiState<TvShowDetailsData, TvShowFailure>> = _uiState.asStateFlow()

    val imageShots: StateFlow<List<ImageShot>> = uiState
        .map { (it as? UiState.Success)?.data?.imageShots ?: emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val watchProviderRegion: StateFlow<String> = appConfigRepository.watchProviderRegion

    init {
        fetchTvShowDetails()
    }

    fun fetchTvShowDetails() {
        _uiState.update { UiState.Loading }

        val config = TvShowDetailsConfig(tvShowId = tvShowId)

        viewModelScope.launch {
            val result = tvShowDetailsUseCase(config)
            _uiState.update {
                when (result) {
                    is Result.Error -> UiState.Error(result.error)
                    is Result.Success -> UiState.Success(
                        TvShowDetailsData(
                            tvShow = result.data,
                            imageShots = result.data.imageShots()
                        )
                    )
                }
            }
        }
    }

    fun openYoutubeApp(videoId: String) {
        application.dispatchYoutubeIntent(videoId = videoId)
    }

    fun onPlayTrailerClicked(tvShow: TvShow) {
        tvShow.primaryTrailer?.let {
            openYoutubeApp(it.key)
        }
    }
}
