package com.ssverma.feature.tv.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.feature.tv.domain.model.TvSeasonConfig
import com.ssverma.feature.tv.domain.usecase.TvSeasonUseCase
import com.ssverma.feature.tv.ui.common.TvSeasonUiState
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

@HiltViewModel(assistedFactory = TvSeasonDetailsViewModel.Factory::class)
class TvSeasonDetailsViewModel @AssistedInject constructor(
    @Assisted("tvShowId") val tvShowId: Int,
    @Assisted("seasonNumber") private val seasonNumber: Int,
    private val tvSeasonUseCase: TvSeasonUseCase
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("tvShowId") tvShowId: Int,
            @Assisted("seasonNumber") seasonNumber: Int
        ): TvSeasonDetailsViewModel
    }

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
