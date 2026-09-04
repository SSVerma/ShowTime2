package com.ssverma.feature.library.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.convertToTmdbBackdropUrl
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.domain.usecase.diary.DeleteDiaryEntryUseCase
import com.ssverma.shared.domain.usecase.diary.GetDiaryEntriesUseCase
import com.ssverma.shared.domain.usecase.diary.GetDiarySummaryStatsUseCase
import com.ssverma.shared.domain.usecase.diary.SaveDiaryEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

private data class SearchToLogState(
    val isSearchingToLog: Boolean = false,
    val mediaSearchQuery: String = "",
    val mediaSearchFilter: ChallengeMediaTypeFilter = ChallengeMediaTypeFilter.ALL,
    val mediaSearchSuggestions: List<ChallengeMediaItem> = emptyList(),
    val isSearchingMedia: Boolean = false,
    val mediaItemPendingLog: ChallengeMediaItem? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CinemaDiaryViewModel @Inject constructor(
    private val getDiaryEntriesUseCase: GetDiaryEntriesUseCase,
    private val getDiarySummaryStatsUseCase: GetDiarySummaryStatsUseCase,
    private val saveDiaryEntryUseCase: SaveDiaryEntryUseCase,
    private val deleteDiaryEntryUseCase: DeleteDiaryEntryUseCase,
    private val tmdbApiService: TmdbApiService
) : ViewModel() {

    private val _activeFilter = MutableStateFlow(DiaryFilterType.ALL)
    private val _entryPendingEdit = MutableStateFlow<DiaryEntry?>(null)
    private val _entryPendingDelete = MutableStateFlow<DiaryEntry?>(null)

    private val _isSearchingToLog = MutableStateFlow(false)
    private val _mediaSearchQuery = MutableStateFlow("")
    private val _mediaSearchFilter = MutableStateFlow(ChallengeMediaTypeFilter.ALL)
    private val _mediaSearchSuggestions = MutableStateFlow<List<ChallengeMediaItem>>(emptyList())
    private val _isSearchingMedia = MutableStateFlow(false)
    private val _mediaItemPendingLog = MutableStateFlow<ChallengeMediaItem?>(null)

    private var mediaSearchJob: Job? = null

    private val monthYearFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    private val searchToLogStateFlow = combine(
        combine(
            _isSearchingToLog,
            _mediaSearchQuery,
            _mediaSearchFilter
        ) { isSearching, query, filter ->
            Triple(isSearching, query, filter)
        },
        combine(
            _mediaSearchSuggestions,
            _isSearchingMedia,
            _mediaItemPendingLog
        ) { suggestions, searching, pendingLog ->
            Triple(suggestions, searching, pendingLog)
        }
    ) { (isSearching, query, filter), (suggestions, searching, pendingLog) ->
        SearchToLogState(
            isSearchingToLog = isSearching,
            mediaSearchQuery = query,
            mediaSearchFilter = filter,
            mediaSearchSuggestions = suggestions,
            isSearchingMedia = searching,
            mediaItemPendingLog = pendingLog
        )
    }

    val uiState: StateFlow<CinemaDiaryUiState> = combine(
        _activeFilter.flatMapLatest { filter ->
            getDiaryEntriesUseCase(filter)
        },
        getDiarySummaryStatsUseCase(),
        _activeFilter,
        combine(_entryPendingEdit, _entryPendingDelete) { edit, delete -> Pair(edit, delete) },
        searchToLogStateFlow
    ) { entries, stats, filter, (pendingEdit, pendingDelete), searchState ->
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
            entryPendingDelete = pendingDelete,
            isSearchingToLog = searchState.isSearchingToLog,
            mediaSearchQuery = searchState.mediaSearchQuery,
            mediaSearchFilter = searchState.mediaSearchFilter,
            mediaSearchSuggestions = searchState.mediaSearchSuggestions,
            isSearchingMedia = searchState.isSearchingMedia,
            mediaItemPendingLog = searchState.mediaItemPendingLog
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

    fun onOpenLogSearch() {
        _isSearchingToLog.value = true
        _mediaSearchQuery.value = ""
        _mediaSearchSuggestions.value = emptyList()
        _isSearchingMedia.value = false
    }

    fun onDismissLogSearch() {
        _isSearchingToLog.value = false
        mediaSearchJob?.cancel()
    }

    fun onSearchQueryChange(
        query: String,
        filter: ChallengeMediaTypeFilter = _mediaSearchFilter.value
    ) {
        _mediaSearchQuery.value = query
        _mediaSearchFilter.value = filter
        mediaSearchJob?.cancel()

        val trimmed = query.trim()
        if (trimmed.length < 2) {
            _mediaSearchSuggestions.value = emptyList()
            _isSearchingMedia.value = false
            return
        }

        mediaSearchJob = viewModelScope.launch {
            delay(250)
            _isSearchingMedia.value = true

            when (val response = tmdbApiService.multiSearch(query = trimmed)) {
                is ApiResponse.Success -> {
                    val rawResults = response.body.results.orEmpty()
                    val filteredSuggestions = rawResults
                        .filter { item ->
                            val type = item.mediaType?.lowercase().orEmpty()
                            when (filter) {
                                ChallengeMediaTypeFilter.ALL -> type == "movie" || type == "tv"
                                ChallengeMediaTypeFilter.MOVIE -> type == "movie"
                                ChallengeMediaTypeFilter.TV -> type == "tv"
                            }
                        }
                        .distinctBy { it.id }
                        .take(10)
                        .map { item ->
                            val mediaType = if (item.mediaType.equals("tv", ignoreCase = true)) {
                                MediaType.Tv
                            } else {
                                MediaType.Movie
                            }
                            val year = (item.releaseDate ?: item.firstAirDate)?.take(4).orEmpty()
                            ChallengeMediaItem(
                                id = item.id,
                                title = item.name.orEmpty(),
                                mediaType = mediaType,
                                posterImageUrl = item.posterPath.convertToTmdbPosterUrl(),
                                backdropImageUrl = item.backdropPath.convertToTmdbBackdropUrl(),
                                releaseYear = year,
                                directorOrCreator = "",
                                overview = item.overview.orEmpty(),
                                voteAvg = item.voteAvg
                            )
                        }

                    _mediaSearchSuggestions.value = filteredSuggestions
                    _isSearchingMedia.value = false
                }

                else -> {
                    _mediaSearchSuggestions.value = emptyList()
                    _isSearchingMedia.value = false
                }
            }
        }
    }

    fun onClearSearch() {
        _mediaSearchQuery.value = ""
        _mediaSearchSuggestions.value = emptyList()
        _isSearchingMedia.value = false
        mediaSearchJob?.cancel()
    }

    fun onSelectMediaToLog(item: ChallengeMediaItem) {
        _isSearchingToLog.value = false
        _mediaItemPendingLog.value = item
    }

    fun onDismissLogDialog() {
        _mediaItemPendingLog.value = null
    }

    fun onSaveNewEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            saveDiaryEntryUseCase(entry)
            _mediaItemPendingLog.value = null
        }
    }
}

