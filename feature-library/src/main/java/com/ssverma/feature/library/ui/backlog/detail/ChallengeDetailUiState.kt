package com.ssverma.feature.library.ui.backlog.detail

import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.challenge.ChallengeProgress

data class ChallengeDetailUiState(
    val progress: ChallengeProgress? = null,
    val isJoined: Boolean = false,
    val selectedFilterIndex: Int = 0,
    val mediaItemToLog: ChallengeMediaItem? = null,
    val showJoinConfirmation: Boolean = false,
    val showLeaveConfirmation: Boolean = false,
    val isLoading: Boolean = true,
    val isEditingMetadata: Boolean = false,
    val isSearchingTitlesToAdd: Boolean = false,
    val itemPendingRemoval: ChallengeMediaItem? = null,
    val cannotRemoveLastTitleWarning: Boolean = false,
    val mediaSearchQuery: String = "",
    val mediaSearchFilter: ChallengeMediaTypeFilter = ChallengeMediaTypeFilter.ALL,
    val mediaSearchSuggestions: List<ChallengeMediaItem> = emptyList(),
    val isSearchingMedia: Boolean = false
)
