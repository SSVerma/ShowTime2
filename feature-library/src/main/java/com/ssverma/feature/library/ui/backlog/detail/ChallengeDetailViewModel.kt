package com.ssverma.feature.library.ui.backlog.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.convertToTmdbBackdropUrl
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.challenge.ChallengeProgress
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.usecase.challenge.GetBacklogChallengesUseCase
import com.ssverma.shared.domain.usecase.challenge.ManageChallengeUseCase
import com.ssverma.shared.domain.usecase.diary.SaveDiaryEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeDetailViewModel @Inject constructor(
    private val getBacklogChallengesUseCase: GetBacklogChallengesUseCase,
    private val manageChallengeUseCase: ManageChallengeUseCase,
    private val saveDiaryEntryUseCase: SaveDiaryEntryUseCase,
    private val tmdbApiService: TmdbApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeDetailUiState())
    val uiState: StateFlow<ChallengeDetailUiState> = _uiState.asStateFlow()

    private var currentChallengeId: String? = null
    private var observeJob: Job? = null
    private var mediaSearchJob: Job? = null

    fun initChallenge(challengeId: String) {
        if (currentChallengeId == challengeId && observeJob != null) return
        currentChallengeId = challengeId

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getBacklogChallengesUseCase.getChallengeDetailFlow(challengeId)
                .collect { (progress, isJoined) ->
                    _uiState.update { current ->
                        current.copy(
                            progress = progress,
                            isJoined = isJoined,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun selectFilter(index: Int) {
        _uiState.update { it.copy(selectedFilterIndex = index) }
    }

    fun requestJoinConfirmation() {
        _uiState.update { it.copy(showJoinConfirmation = true) }
    }

    fun dismissJoinConfirmation() {
        _uiState.update { it.copy(showJoinConfirmation = false) }
    }

    fun confirmJoin() {
        val challenge = _uiState.value.progress?.challenge ?: return
        viewModelScope.launch {
            manageChallengeUseCase.joinChallenge(challenge)
            _uiState.update { it.copy(showJoinConfirmation = false) }
        }
    }

    fun requestLeaveConfirmation() {
        _uiState.update { it.copy(showLeaveConfirmation = true) }
    }

    fun dismissLeaveConfirmation() {
        _uiState.update { it.copy(showLeaveConfirmation = false) }
    }

    fun confirmLeave(onLeft: () -> Unit) {
        val challengeId = currentChallengeId ?: return
        viewModelScope.launch {
            manageChallengeUseCase.leaveChallenge(challengeId)
            _uiState.update { it.copy(showLeaveConfirmation = false) }
            onLeft()
        }
    }

    fun confirmDeleteGoal(onDeleted: () -> Unit) {
        val challengeId = currentChallengeId ?: return
        viewModelScope.launch {
            manageChallengeUseCase.deleteCustomChallenge(challengeId)
            _uiState.update { it.copy(showLeaveConfirmation = false) }
            onDeleted()
        }
    }

    fun openEditMetadataDialog() {
        _uiState.update { it.copy(isEditingMetadata = true) }
    }

    fun dismissEditMetadataDialog() {
        _uiState.update { it.copy(isEditingMetadata = false) }
    }

    fun saveMetadata(title: String, description: String) {
        val challengeId = currentChallengeId ?: return
        viewModelScope.launch {
            manageChallengeUseCase.editCustomChallengeMetadata(
                challengeId = challengeId,
                title = title,
                description = description
            )
            _uiState.update { it.copy(isEditingMetadata = false) }
        }
    }

    fun requestRemoveItem(item: ChallengeMediaItem) {
        val targetItems = _uiState.value.progress?.challenge?.targetMediaItems.orEmpty()
        if (targetItems.size <= 1) {
            _uiState.update { it.copy(cannotRemoveLastTitleWarning = true) }
        } else {
            _uiState.update { it.copy(itemPendingRemoval = item) }
        }
    }

    fun dismissRemoveItem() {
        _uiState.update { it.copy(itemPendingRemoval = null) }
    }

    fun dismissCannotRemoveWarning() {
        _uiState.update { it.copy(cannotRemoveLastTitleWarning = false) }
    }

    fun confirmRemoveItem() {
        val item = _uiState.value.itemPendingRemoval ?: return
        val challengeId = currentChallengeId ?: return
        viewModelScope.launch {
            manageChallengeUseCase.removeTitleFromCustomChallenge(
                challengeId = challengeId,
                mediaId = item.id,
                mediaType = item.mediaType
            )
            _uiState.update { it.copy(itemPendingRemoval = null) }
        }
    }

    fun openAddTitlesSearch() {
        _uiState.update { it.copy(isSearchingTitlesToAdd = true) }
    }

    fun closeAddTitlesSearch() {
        clearMediaSearch()
        _uiState.update { it.copy(isSearchingTitlesToAdd = false) }
    }

    fun onMediaSearchFilterChange(filter: ChallengeMediaTypeFilter) {
        _uiState.update { it.copy(mediaSearchFilter = filter) }
        val currentQuery = _uiState.value.mediaSearchQuery
        if (currentQuery.isNotBlank()) {
            onMediaSearchQueryChange(currentQuery, filter)
        }
    }

    fun onMediaSearchQueryChange(
        query: String,
        filter: ChallengeMediaTypeFilter = ChallengeMediaTypeFilter.ALL
    ) {
        _uiState.update { it.copy(mediaSearchQuery = query) }
        mediaSearchJob?.cancel()

        val trimmed = query.trim()
        if (trimmed.length < 2) {
            _uiState.update {
                it.copy(
                    mediaSearchSuggestions = emptyList(),
                    isSearchingMedia = false
                )
            }
            return
        }

        mediaSearchJob = viewModelScope.launch {
            delay(250)
            _uiState.update { it.copy(isSearchingMedia = true) }

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
                        .take(8)
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

                    _uiState.update {
                        it.copy(
                            mediaSearchSuggestions = filteredSuggestions,
                            isSearchingMedia = false
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            mediaSearchSuggestions = emptyList(),
                            isSearchingMedia = false
                        )
                    }
                }
            }
        }
    }

    fun clearMediaSearch() {
        mediaSearchJob?.cancel()
        _uiState.update {
            it.copy(
                mediaSearchQuery = "",
                mediaSearchSuggestions = emptyList(),
                isSearchingMedia = false
            )
        }
    }

    fun toggleMediaInCustomChallenge(item: ChallengeMediaItem) {
        val challengeId = currentChallengeId ?: return
        val currentItems = _uiState.value.progress?.challenge?.targetMediaItems.orEmpty()
        val isAlreadyAdded = currentItems.any { it.id == item.id && it.mediaType == item.mediaType }

        viewModelScope.launch {
            if (isAlreadyAdded) {
                if (currentItems.size <= 1) {
                    _uiState.update { it.copy(cannotRemoveLastTitleWarning = true) }
                } else {
                    manageChallengeUseCase.removeTitleFromCustomChallenge(
                        challengeId = challengeId,
                        mediaId = item.id,
                        mediaType = item.mediaType
                    )
                }
            } else {
                manageChallengeUseCase.addTitlesToCustomChallenge(
                    challengeId = challengeId,
                    newItems = listOf(item)
                )
            }
        }
    }

    fun openLogDialog(item: ChallengeMediaItem) {
        _uiState.update { it.copy(mediaItemToLog = item) }
    }

    fun dismissLogDialog() {
        _uiState.update { it.copy(mediaItemToLog = null) }
    }

    fun saveDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            saveDiaryEntryUseCase(entry)
            _uiState.update { it.copy(mediaItemToLog = null) }
        }
    }

    fun generateShareableChallengeText(progress: ChallengeProgress): String {
        return buildString {
            append("🏆 Cinema Challenge: ${progress.challenge.title}\n")
            append("📊 Progress: ${progress.watchedCount}/${progress.totalCount} titles (${progress.progressPercentage}%)\n")
            append("🎖 Milestone: ${progress.milestoneTitle}\n")
            if (progress.isCompleted) {
                append("🎉 Challenge Completed on ShowTime!\n")
            } else if (progress.remainingItems.isNotEmpty()) {
                val nextUp = progress.remainingItems.firstOrNull()?.title
                if (!nextUp.isNullOrBlank()) {
                    append("🍿 Next Up: $nextUp\n")
                }
            }
            append("\nTracked on ShowTime App")
        }
    }
}
