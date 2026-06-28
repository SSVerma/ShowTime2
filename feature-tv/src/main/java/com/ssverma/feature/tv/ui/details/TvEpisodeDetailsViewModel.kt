package com.ssverma.feature.tv.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.feature.tv.domain.model.TvEpisodeConfig
import com.ssverma.feature.tv.domain.usecase.TvEpisodeUseCase
import com.ssverma.feature.tv.ui.common.TvEpisodeUiState
import com.ssverma.shared.domain.Result
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = TvEpisodeDetailsViewModel.Factory::class)
class TvEpisodeDetailsViewModel @AssistedInject constructor(
    @Assisted("tvShowId") private val tvShowId: Int,
    @Assisted("seasonNumber") private val seasonNumber: Int,
    @Assisted("episodeNumber") private val episodeNumber: Int,
    private val tvEpisodeUseCase: TvEpisodeUseCase
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("tvShowId") tvShowId: Int,
            @Assisted("seasonNumber") seasonNumber: Int,
            @Assisted("episodeNumber") episodeNumber: Int
        ): TvEpisodeDetailsViewModel
    }

    private val _uiState = MutableStateFlow<TvEpisodeUiState>(UiState.Idle)
    val uiState: StateFlow<TvEpisodeUiState> = _uiState.asStateFlow()

    init {
        fetchTvEpisode()
    }

    fun fetchTvEpisode() {
        _uiState.update { UiState.Loading }

        viewModelScope.launch {
            val tvEpisodeConfig = TvEpisodeConfig(
                tvShowId = tvShowId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber
            )

            val result = tvEpisodeUseCase(tvEpisodeConfig)

            _uiState.update {
                when (result) {
                    is Result.Error -> UiState.Error(result.error)
                    is Result.Success -> UiState.Success(result.data)
                }
            }
        }
    }
}
