package com.ssverma.showtime.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.shared.domain.model.AppUpdateStatus
import com.ssverma.shared.domain.repository.AppUpdateRepository
import com.ssverma.showtime.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val appUpdateRepository: AppUpdateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUpdateUiState())
    val uiState: StateFlow<AppUpdateUiState> = _uiState.asStateFlow()

    init {
        observeUpdateStatus()
    }

    private fun observeUpdateStatus() {
        viewModelScope.launch {
            appUpdateRepository.observeAppUpdateStatus(
                currentVersionCode = BuildConfig.VERSION_CODE.toLong()
            ).collect { status ->
                _uiState.update { currentState ->
                    currentState.copy(
                        status = status,
                        showSoftUpdatePrompt = status is AppUpdateStatus.SoftUpdate
                    )
                }
            }
        }
    }

    fun dismissSoftUpdate(versionCode: Long) {
        _uiState.update { it.copy(showSoftUpdatePrompt = false) }
        viewModelScope.launch {
            appUpdateRepository.dismissSoftUpdate(versionCode = versionCode)
        }
    }
}
