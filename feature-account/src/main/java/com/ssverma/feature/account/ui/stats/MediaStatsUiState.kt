package com.ssverma.feature.account.ui.stats

import com.ssverma.feature.account.domain.model.MediaStats
import com.ssverma.shared.domain.failure.Failure

sealed interface MediaStatsUiState {
    object Unauthorized : MediaStatsUiState
    object Loading : MediaStatsUiState
    data class Error(val failure: Failure.CoreFailure) : MediaStatsUiState
    data class Success(val mediaStats: MediaStats) : MediaStatsUiState
}