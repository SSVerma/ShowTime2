package com.ssverma.feature.tv.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.shared.domain.Result
import com.ssverma.core.ui.UiState
import com.ssverma.feature.tv.domain.model.TvSeasonConfig
import com.ssverma.feature.tv.domain.usecase.TvSeasonUseCase
import com.ssverma.feature.tv.navigation.TvSeasonDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvSeasonDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tvSeasonUseCase: TvSeasonUseCase
) : ViewModel() {

    val tvShowId = savedStateHandle
        .get<Int>(TvSeasonDetailDestination.ArgTvShowId) ?: 0
    private val seasonNumber = savedStateHandle
        .get<Int>(TvSeasonDetailDestination.ArgTvSeasonNumber) ?: 0

    private val _observableSeason = MutableStateFlow<TvSeasonUiState>(UiState.Idle)
    val observableTvSeason = _observableSeason.asStateFlow()

    init {
        fetchTvSeason()
    }

    fun fetchTvSeason(coroutineScope: CoroutineScope = viewModelScope) {
        _observableSeason.update { UiState.Loading }

        coroutineScope.launch {
            val tvSeasonConfig = TvSeasonConfig(
                tvShowId = tvShowId,
                seasonNumber = seasonNumber
            )

            val result = tvSeasonUseCase(tvSeasonConfig)

            _observableSeason.update {
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