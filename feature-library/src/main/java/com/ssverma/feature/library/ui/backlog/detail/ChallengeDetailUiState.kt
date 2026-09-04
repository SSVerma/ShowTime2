package com.ssverma.feature.library.ui.backlog.detail

import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeProgress

data class ChallengeDetailUiState(
    val progress: ChallengeProgress? = null,
    val isJoined: Boolean = false,
    val selectedFilterIndex: Int = 0,
    val mediaItemToLog: ChallengeMediaItem? = null,
    val showJoinConfirmation: Boolean = false,
    val showLeaveConfirmation: Boolean = false,
    val isLoading: Boolean = true
)
