package com.ssverma.feature.tv.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.shared.domain.Result
import com.ssverma.core.ui.UiState
import com.ssverma.feature.tv.domain.model.TvEpisodeConfig
import com.ssverma.feature.tv.domain.usecase.TvEpisodeUseCase
import com.ssverma.feature.tv.navigation.TvEpisodeDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvEpisodeDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tvEpisodeUseCase: TvEpisodeUseCase
) : ViewModel() {

    private val tvShowId = savedStateHandle
        .get<Int>(TvEpisodeDetailDestination.ArgTvShowId) ?: 0

    private val seasonNumber = savedStateHandle
        .get<Int>(TvEpisodeDetailDestination.ArgSeasonNumber) ?: 0

    private val episodeNumber = savedStateHandle
        .get<Int>(TvEpisodeDetailDestination.ArgEpisodeNumber) ?: 0

    private val _observableEpisode = MutableStateFlow<TvEpisodeUiState>(UiState.Idle)
    val observableTvEpisode = _observableEpisode.asStateFlow()

    init {
        fetchTvEpisode()
    }

    fun fetchTvEpisode(coroutineScope: CoroutineScope = viewModelScope) {
        _observableEpisode.update { UiState.Loading }

        coroutineScope.launch {
            val tvEpisodeConfig = TvEpisodeConfig(
                tvShowId = tvShowId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber
            )

            val result = tvEpisodeUseCase(tvEpisodeConfig)

            _observableEpisode.update {
                when (result) {
                    is Result.Error -> {
                        UiState.Error(result.error)
                    }
                    is Result.Success -> {
                        UiState.Success(result.data)
                    }
                }
            }
        }
    }

}