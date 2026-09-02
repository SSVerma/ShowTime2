package com.ssverma.feature.library.ui.wrapped

import com.ssverma.shared.domain.model.stats.CinephileMilestone
import com.ssverma.shared.domain.model.stats.WrappedYearSummary

data class CinephileWrappedUiState(
    val summary: WrappedYearSummary? = null,
    val selectedYear: Int = 0, // 0 = All-Time
    val availableYears: List<Int> = emptyList(),
    val selectedMilestone: CinephileMilestone? = null,
    val isLoading: Boolean = true
)
