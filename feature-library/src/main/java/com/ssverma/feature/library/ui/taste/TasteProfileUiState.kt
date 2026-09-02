package com.ssverma.feature.library.ui.taste

import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.domain.model.stats.RecommendationShelf
import com.ssverma.shared.domain.model.stats.TasteProfileStats

data class TasteProfileUiState(
    val isLoading: Boolean = false,
    val selectedFilter: DiaryFilterType = DiaryFilterType.ALL,
    val stats: TasteProfileStats = TasteProfileStats(),
    val recommendationShelves: List<RecommendationShelf> = emptyList(),
    val isRefreshingRecommendations: Boolean = false
)
