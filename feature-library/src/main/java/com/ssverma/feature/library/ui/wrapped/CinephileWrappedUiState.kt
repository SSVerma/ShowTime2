package com.ssverma.feature.library.ui.wrapped

import com.ssverma.feature.library.ui.wrapped.component.WrappedStoryStyle
import com.ssverma.shared.domain.model.stats.CinephileMilestone
import com.ssverma.shared.domain.model.stats.WrappedYearSummary

data class CinephileWrappedUiState(
    val summary: WrappedYearSummary? = null,
    val selectedYear: Int = 0, // 0 = All-Time
    val availableYears: List<Int> = emptyList(),
    val selectedMilestone: CinephileMilestone? = null,
    val isLoading: Boolean = true,
    val selectedStyle: WrappedStoryStyle = WrappedStoryStyle.CLASSIC_VELVET,
    val isWatermarkFree: Boolean = false,
    val isProActive: Boolean = false,
    val isPassActive: Boolean = false,
    val isExporting: Boolean = false,
    val isProPaymentEnabled: Boolean = true,
    val isGateOpen: Boolean = false,
    val isExportSheetOpen: Boolean = false,
    val pendingStyle: WrappedStoryStyle? = null,
    val userName: String? = null
)
