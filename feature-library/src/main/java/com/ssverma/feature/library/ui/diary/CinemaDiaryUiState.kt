package com.ssverma.feature.library.ui.diary

import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.domain.model.diary.DiarySummaryStats

data class DiaryTimelineGroup(
    val monthYearLabel: String,
    val entries: List<DiaryEntry>
)

data class CinemaDiaryUiState(
    val isLoading: Boolean = false,
    val stats: DiarySummaryStats = DiarySummaryStats(),
    val activeFilter: DiaryFilterType = DiaryFilterType.ALL,
    val timelineGroups: List<DiaryTimelineGroup> = emptyList(),
    val totalEntriesCount: Int = 0,
    val entryPendingEdit: DiaryEntry? = null,
    val entryPendingDelete: DiaryEntry? = null,
    val isSearchingToLog: Boolean = false,
    val mediaSearchQuery: String = "",
    val mediaSearchFilter: ChallengeMediaTypeFilter = ChallengeMediaTypeFilter.ALL,
    val mediaSearchSuggestions: List<ChallengeMediaItem> = emptyList(),
    val isSearchingMedia: Boolean = false,
    val mediaItemPendingLog: ChallengeMediaItem? = null
)

