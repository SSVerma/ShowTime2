package com.ssverma.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.usecase.GetAvailableWatchRegionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WatchRegionViewModel @Inject constructor(
    private val getAvailableWatchRegionsUseCase: GetAvailableWatchRegionsUseCase,
    private val appConfigRepository: AppConfigRepository
) : ViewModel() {

    private val _regionsState = MutableStateFlow<UiState<List<WatchProviderRegion>, Nothing>>(UiState.Idle)
    val regionsState: StateFlow<UiState<List<WatchProviderRegion>, Nothing>> = _regionsState.asStateFlow()

    val currentRegion: StateFlow<String> = appConfigRepository.watchProviderRegion

    fun loadAvailableRegions() {
        if (_regionsState.value is UiState.Success) return

        viewModelScope.launch {
            _regionsState.value = UiState.Loading
            val result = getAvailableWatchRegionsUseCase()
            _regionsState.value = when (result) {
                is Result.Error -> UiState.Error(result.error)
                is Result.Success -> UiState.Success(result.data)
            }
        }
    }

    fun updateRegion(regionCode: String) {
        viewModelScope.launch {
            appConfigRepository.updateWatchProviderRegion(regionCode)
        }
    }
}
