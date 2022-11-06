package com.ssverma.feature.account.ui

import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.shared.domain.failure.Failure

sealed interface ProfileUiState {
    data class ShowProfileContent(
        val profile: Profile
    ) : ProfileUiState

    object Loading : ProfileUiState

    data class Error(
        val failure: Failure.CoreFailure
    ) : ProfileUiState
}