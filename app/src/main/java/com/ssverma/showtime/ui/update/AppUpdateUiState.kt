package com.ssverma.showtime.ui.update

import androidx.compose.runtime.Immutable
import com.ssverma.shared.domain.model.AppUpdateStatus

@Immutable
data class AppUpdateUiState(
    val status: AppUpdateStatus = AppUpdateStatus.UpToDate,
    val showSoftUpdatePrompt: Boolean = false
)
