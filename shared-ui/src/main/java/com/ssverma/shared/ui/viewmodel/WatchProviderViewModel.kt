package com.ssverma.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.WatchProvider
import com.ssverma.shared.domain.usecase.GetWatchProvidersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchProviderViewModel @Inject constructor(
    private val getWatchProvidersUseCase: GetWatchProvidersUseCase
) : ViewModel() {

    private val _watchProviderState =
        MutableStateFlow<UiState<WatchProvider?, Nothing>>(UiState.Idle)
    val watchProviderState = _watchProviderState.asStateFlow()

    fun fetchWatchProviders(mediaId: Int, isMovie: Boolean) {
        viewModelScope.launch {
            _watchProviderState.value = UiState.Loading
            val result = getWatchProvidersUseCase(mediaId, isMovie)
            _watchProviderState.value = when (result) {
                is Result.Error -> UiState.Error(result.error)
                is Result.Success -> UiState.Success(result.data)
            }
        }
    }

    fun resetState() {
        _watchProviderState.value = UiState.Idle
    }
}
