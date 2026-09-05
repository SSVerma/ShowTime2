package com.ssverma.feature.library.ui.backlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.convertToTmdbBackdropUrl
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.challenge.ChallengeProgress
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import com.ssverma.shared.domain.usecase.challenge.GetBacklogChallengesUseCase
import com.ssverma.shared.domain.usecase.challenge.ManageChallengeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BacklogChallengeViewModel @Inject constructor(
    private val getBacklogChallengesUseCase: GetBacklogChallengesUseCase,
    private val manageChallengeUseCase: ManageChallengeUseCase,
    private val tmdbApiService: TmdbApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BacklogChallengeUiState())
    val uiState: StateFlow<BacklogChallengeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            getBacklogChallengesUseCase.getCuratedChallengesFlow().collect { curatedList ->
                _uiState.update { it.copy(curatedChallenges = curatedList.map { p -> p.challenge }) }
            }
        }

        viewModelScope.launch {
            // Trigger initial sync in background
            getBacklogChallengesUseCase.getCuratedChallengesWithProgress(forceRefresh = false)
        }

        viewModelScope.launch {
            combine(
                getBacklogChallengesUseCase(),
                manageChallengeUseCase.blindspotsFlow
            ) { activeProgressList, blindspots ->
                activeProgressList to blindspots
            }.collect { (activeProgressList, blindspots) ->
                _uiState.update { current ->
                    // Also update selectedChallengeDetail if it's currently open
                    val updatedDetail = current.selectedChallengeDetail?.let { detail ->
                        activeProgressList.firstOrNull { it.challenge.id == detail.challenge.id }
                            ?: detail
                    }
                    current.copy(
                        activeChallenges = activeProgressList,
                        blindspots = blindspots,
                        selectedChallengeDetail = updatedDetail,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            getBacklogChallengesUseCase.getCuratedChallengesWithProgress(forceRefresh = true)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun selectCategoryFilter(category: ChallengeCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun openChallengeDetail(progress: ChallengeProgress) {
        _uiState.update { it.copy(selectedChallengeDetail = progress) }
    }

    fun openChallengeDetail(challenge: CinephileChallenge) {
        val activeProgress =
            _uiState.value.activeChallenges.firstOrNull { it.challenge.id == challenge.id }
        if (activeProgress != null) {
            openChallengeDetail(activeProgress)
        }
    }

    fun closeChallengeDetail() {
        _uiState.update { it.copy(selectedChallengeDetail = null) }
    }

    private var mediaSearchJob: Job? = null

    fun openCreateCustomGoalSheet() {
        _uiState.update { it.copy(isCreatingCustomGoal = true) }
    }

    fun closeCreateCustomGoalSheet() {
        clearMediaSearch()
        _uiState.update { it.copy(isCreatingCustomGoal = false) }
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
            delay(250) // Debounce typing
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

    fun joinCuratedChallenge(challenge: CinephileChallenge) {
        viewModelScope.launch {
            manageChallengeUseCase.joinChallenge(challenge)
        }
    }

    fun leaveChallenge(challengeId: String) {
        viewModelScope.launch {
            manageChallengeUseCase.leaveChallenge(challengeId)
            if (_uiState.value.selectedChallengeDetail?.challenge?.id == challengeId) {
                closeChallengeDetail()
            }
        }
    }

    fun createCustomGoal(
        title: String,
        description: String,
        mediaTypeFilter: ChallengeMediaTypeFilter,
        targetCount: Int,
        targetItems: List<ChallengeMediaItem> = emptyList()
    ) {
        viewModelScope.launch {
            manageChallengeUseCase.createCustomChallenge(
                title = title,
                description = description,
                mediaTypeFilter = mediaTypeFilter,
                targetCount = targetCount,
                targetItems = targetItems
            )
            clearMediaSearch()
            closeCreateCustomGoalSheet()
        }
    }

    fun addBlindspot(item: BlindspotPriorityItem) {
        viewModelScope.launch {
            manageChallengeUseCase.addBlindspot(item)
        }
    }

    fun removeBlindspot(mediaId: Int, mediaType: MediaType) {
        viewModelScope.launch {
            manageChallengeUseCase.removeBlindspot(mediaId, mediaType)
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
