package com.ssverma.feature.library.ui.backlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.challenge.ChallengeProgress
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import com.ssverma.shared.domain.usecase.challenge.GetBacklogChallengesUseCase
import com.ssverma.shared.domain.usecase.challenge.ManageChallengeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val manageChallengeUseCase: ManageChallengeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BacklogChallengeUiState())
    val uiState: StateFlow<BacklogChallengeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val curated =
                getBacklogChallengesUseCase.getCuratedChallengesWithProgress().map { it.challenge }
            _uiState.update { it.copy(curatedChallenges = curated) }
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
        loadData()
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

    fun openCreateCustomGoalSheet() {
        _uiState.update { it.copy(isCreatingCustomGoal = true) }
    }

    fun closeCreateCustomGoalSheet() {
        _uiState.update { it.copy(isCreatingCustomGoal = false) }
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
        targetCount: Int
    ) {
        viewModelScope.launch {
            val custom = manageChallengeUseCase.createCustomChallenge(
                title = title,
                description = description,
                mediaTypeFilter = mediaTypeFilter,
                targetCount = targetCount
            )
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
