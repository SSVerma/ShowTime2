package com.ssverma.feature.library.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.domain.usecase.diary.DeleteDiaryEntryUseCase
import com.ssverma.shared.domain.usecase.diary.GetDiaryEntriesUseCase
import com.ssverma.shared.domain.usecase.diary.GetDiarySummaryStatsUseCase
import com.ssverma.shared.domain.usecase.diary.SaveDiaryEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CinemaDiaryViewModel @Inject constructor(
    private val getDiaryEntriesUseCase: GetDiaryEntriesUseCase,
    private val getDiarySummaryStatsUseCase: GetDiarySummaryStatsUseCase,
    private val saveDiaryEntryUseCase: SaveDiaryEntryUseCase,
    private val deleteDiaryEntryUseCase: DeleteDiaryEntryUseCase
) : ViewModel() {

    private val _activeFilter = MutableStateFlow(DiaryFilterType.ALL)
    private val _entryPendingEdit = MutableStateFlow<DiaryEntry?>(null)
    private val _entryPendingDelete = MutableStateFlow<DiaryEntry?>(null)

    private val monthYearFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    val uiState: StateFlow<CinemaDiaryUiState> = combine(
        _activeFilter.flatMapLatest { filter ->
            getDiaryEntriesUseCase(filter)
        },
        getDiarySummaryStatsUseCase(),
        _activeFilter,
        _entryPendingEdit,
        _entryPendingDelete
    ) { entries, stats, filter, pendingEdit, pendingDelete ->
        val groups = entries
            .groupBy { entry ->
                monthYearFormatter.format(Date(entry.loggedAt))
            }
            .map { (monthYear, groupEntries) ->
                DiaryTimelineGroup(
                    monthYearLabel = monthYear,
                    entries = groupEntries
                )
            }

        CinemaDiaryUiState(
            isLoading = false,
            stats = stats,
            activeFilter = filter,
            timelineGroups = groups,
            totalEntriesCount = entries.size,
            entryPendingEdit = pendingEdit,
            entryPendingDelete = pendingDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CinemaDiaryUiState(isLoading = true)
    )

    fun setFilter(filter: DiaryFilterType) {
        _activeFilter.value = filter
    }

    fun onEditEntry(entry: DiaryEntry) {
        _entryPendingEdit.value = entry
    }

    fun onDismissEdit() {
        _entryPendingEdit.value = null
    }

    fun onSaveEditedEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            saveDiaryEntryUseCase(entry)
            _entryPendingEdit.value = null
        }
    }

    fun onRequestDeleteEntry(entry: DiaryEntry) {
        _entryPendingDelete.value = entry
    }

    fun onDismissDelete() {
        _entryPendingDelete.value = null
    }

    fun onConfirmDeleteEntry() {
        val entry = _entryPendingDelete.value ?: return
        viewModelScope.launch {
            deleteDiaryEntryUseCase(entry.id)
            _entryPendingDelete.value = null
        }
    }
}
