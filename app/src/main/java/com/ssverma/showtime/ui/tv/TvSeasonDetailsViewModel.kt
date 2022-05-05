package com.ssverma.showtime.ui.tv

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.model.tv.TvSeasonConfig
import com.ssverma.showtime.domain.usecase.tv.TvSeasonUseCase
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
class TvSeasonDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tvSeasonUseCase: TvSeasonUseCase
) : ViewModel() {

    val tvShowId = savedStateHandle
        .get<Int>(AppDestination.TvSeasonDetails.ArgTvShowId) ?: 0
    private val seasonNumber = savedStateHandle
        .get<Int>(AppDestination.TvSeasonDetails.ArgTvSeasonNumber) ?: 0

    private val _observableSeason = MutableStateFlow<TvSeasonUiState>(FetchDataUiState.Idle)
    val observableTvSeason = _observableSeason.asStateFlow()

    init {
        fetchTvSeason()
    }

    fun fetchTvSeason(coroutineScope: CoroutineScope = viewModelScope) {
        _observableSeason.update { FetchDataUiState.Loading }

        coroutineScope.launch {
            val tvSeasonConfig = TvSeasonConfig(
                tvShowId = tvShowId,
                seasonNumber = seasonNumber
            )

            val result = tvSeasonUseCase(tvSeasonConfig)

            _observableSeason.update {
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