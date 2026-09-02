package com.ssverma.feature.library.ui.backlog

import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeProgress
import com.ssverma.shared.domain.model.challenge.CinephileChallenge

data class BacklogChallengeUiState(
    val activeChallenges: List<ChallengeProgress> = emptyList(),
    val curatedChallenges: List<CinephileChallenge> = emptyList(),
    val blindspots: List<BlindspotPriorityItem> = emptyList(),
    val selectedCategory: ChallengeCategory? = null,
    val selectedChallengeDetail: ChallengeProgress? = null,
    val isCreatingCustomGoal: Boolean = false,
    val isRefreshing: Boolean = false
)
