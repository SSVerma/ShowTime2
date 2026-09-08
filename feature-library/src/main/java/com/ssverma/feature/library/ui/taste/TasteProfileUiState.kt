package com.ssverma.feature.library.ui.taste

import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.domain.model.stats.RecommendationShelf
import com.ssverma.shared.domain.model.stats.TasteProfileStats

data class TasteProfileUiState(
    val isLoading: Boolean = false,
    val selectedFilter: DiaryFilterType = DiaryFilterType.ALL,
    val stats: TasteProfileStats = TasteProfileStats(),
    val recommendationShelves: List<RecommendationShelf> = emptyList(),
    val isRefreshingRecommendations: Boolean = false,
    val isProActive: Boolean = false,
    val isPassActive: Boolean = false,
    val isProPaymentEnabled: Boolean = true,
    val isGateOpen: Boolean = false,
    val userName: String? = null,
    val isShareSheetOpen: Boolean = false,
    val isExporting: Boolean = false
)
