package com.ssverma.showtime.ui.tv

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.model.tv.TvEpisodeConfig
import com.ssverma.showtime.domain.usecase.tv.TvEpisodeUseCase
import com.ssverma.showtime.navigation.AppDestination
import com.ssverma.showtime.ui.FetchDataUiState
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
        .get<Int>(AppDestination.TvEpisodeDetails.ArgTvShowId) ?: 0

    private val seasonNumber = savedStateHandle
        .get<Int>(AppDestination.TvEpisodeDetails.ArgSeasonNumber) ?: 0

    private val episodeNumber = savedStateHandle
        .get<Int>(AppDestination.TvEpisodeDetails.ArgEpisodeNumber) ?: 0

    private val _observableEpisode = MutableStateFlow<TvEpisodeUiState>(FetchDataUiState.Idle)
    val observableTvEpisode = _observableEpisode.asStateFlow()

    init {
        fetchTvEpisode()
    }

    fun fetchTvEpisode(coroutineScope: CoroutineScope = viewModelScope) {
        _observableEpisode.update { FetchDataUiState.Loading }

        coroutineScope.launch {
            val tvEpisodeConfig = TvEpisodeConfig(
                tvShowId = tvShowId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber
            )

            val result = tvEpisodeUseCase(tvEpisodeConfig)

            _observableEpisode.update {
                when (result) {
                    is DomainResult.Error -> {
                        FetchDataUiState.Error(result.error)
                    }
                    is DomainResult.Success -> {
                        FetchDataUiState.Success(result.data)
                    }
                }
            }
        }
    }

}