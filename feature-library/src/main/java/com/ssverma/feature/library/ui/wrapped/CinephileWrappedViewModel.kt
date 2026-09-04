package com.ssverma.feature.library.ui.wrapped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.shared.domain.model.stats.CinephileMilestone
import com.ssverma.shared.domain.model.stats.WrappedYearSummary
import com.ssverma.shared.domain.usecase.stats.GetCinephileWrappedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CinephileWrappedViewModel @Inject constructor(
    private val getCinephileWrappedUseCase: GetCinephileWrappedUseCase
) : ViewModel() {

    private val _selectedYear = MutableStateFlow(0) // 0 = All-Time
    private val _selectedMilestone = MutableStateFlow<CinephileMilestone?>(null)

    val uiState: StateFlow<CinephileWrappedUiState> = combine(
        _selectedYear.flatMapLatest { year ->
            getCinephileWrappedUseCase(year)
        },
        _selectedYear,
        _selectedMilestone
    ) { summary, year, milestone ->
        CinephileWrappedUiState(
            summary = summary,
            selectedYear = year,
            availableYears = listOf(0) + summary.availableYears,
            selectedMilestone = milestone,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CinephileWrappedUiState(isLoading = true)
    )

    fun onSelectYear(year: Int) {
        _selectedYear.value = year
    }

    fun onSelectMilestone(milestone: CinephileMilestone?) {
        _selectedMilestone.value = milestone
    }

    fun generateWrappedShareText(summary: WrappedYearSummary): String {
        val title =
            if (summary.year == 0) "🎬 My All-Time Cinema Journey on ShowTime" else "🎬 My ${summary.year} Cinema Wrapped on ShowTime"
        val topPicksText = if (summary.topRatedMedia.isNotEmpty()) {
            "\n⭐ Top Rated:\n" + summary.topRatedMedia.take(3)
                .joinToString("\n") { "• ${it.title} (${it.userRating}★)" }
        } else ""

        val unlockedMilestonesCount = summary.milestones.count { it.isUnlocked }

        return """
            $title
            
            ⏱️ ${summary.totalWatchHours} Hours (~${summary.totalDaysEquivalent} Days)
            🍿 ${summary.totalLogged} Logged (${summary.totalMovies} Movies • ${summary.totalTvShows} TV Shows)
            ⭐ ${String.format("%.1f", summary.averageUserRating)} / 5.0 Average Rating
            🔁 ${summary.rewatchCount} Rewatches
            🏆 $unlockedMilestonesCount / ${summary.milestones.size} Milestones Unlocked
            $topPicksText
            
            Tracked with ShowTime 🍿
        """.trimIndent()
    }

    fun generateMilestoneShareText(milestone: CinephileMilestone): String {
        return """
            🏆 I just unlocked the "${milestone.title}" milestone on ShowTime!
            
            ${milestone.description}
            Tier: ${milestone.tier.name}
            
            Track your cinema journey with ShowTime 🍿
        """.trimIndent()
    }
}
