package com.ssverma.feature.tv.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.feature.tv.domain.model.TvSeasonConfig
import com.ssverma.feature.tv.domain.usecase.TvSeasonUseCase
import com.ssverma.feature.tv.navigation.TvSeasonDetailDestination
import com.ssverma.feature.tv.ui.common.TvSeasonUiState
import com.ssverma.shared.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvSeasonDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tvSeasonUseCase: TvSeasonUseCase
) : ViewModel() {

    val tvShowId = savedStateHandle.get<Int>(TvSeasonDetailDestination.ArgTvShowId) ?: 0
    private val seasonNumber = savedStateHandle.get<Int>(TvSeasonDetailDestination.ArgTvSeasonNumber) ?: 0

    private val _uiState = MutableStateFlow<TvSeasonUiState>(UiState.Idle)
    val uiState: StateFlow<TvSeasonUiState> = _uiState.asStateFlow()

    init {
        fetchTvSeason()
    }

    fun fetchTvSeason() {
        _uiState.update { UiState.Loading }

        viewModelScope.launch {
            val tvSeasonConfig = TvSeasonConfig(
                tvShowId = tvShowId,
                seasonNumber = seasonNumber
            )

            val result = tvSeasonUseCase(tvSeasonConfig)

            _uiState.update {
                when (result) {
                    is Result.Error -> UiState.Error(result.error)
                    is Result.Success -> UiState.Success(result.data)
                }
            }
        }
    }
}
