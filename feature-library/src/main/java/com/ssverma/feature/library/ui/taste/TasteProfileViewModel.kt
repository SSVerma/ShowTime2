package com.ssverma.feature.library.ui.taste

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.domain.model.stats.RecommendationShelf
import com.ssverma.shared.domain.model.stats.TasteProfileStats
import com.ssverma.shared.domain.usecase.recommendation.GetSmartRecommendationsUseCase
import com.ssverma.shared.domain.usecase.stats.GetTasteProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TasteProfileViewModel @Inject constructor(
    private val getTasteProfileUseCase: GetTasteProfileUseCase,
    private val getSmartRecommendationsUseCase: GetSmartRecommendationsUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(DiaryFilterType.ALL)
    private val _recommendationShelves =
        MutableStateFlow<List<RecommendationShelf>>(emptyList())
    private val _isRefreshing = MutableStateFlow(false)

    init {
        loadRecommendations(DiaryFilterType.ALL)
    }

    val uiState: StateFlow<TasteProfileUiState> = combine(
        _selectedFilter.flatMapLatest { filter ->
            getTasteProfileUseCase(filter)
        },
        _recommendationShelves,
        _selectedFilter,
        _isRefreshing
    ) { stats, shelves, filter, isRefreshing ->
        TasteProfileUiState(
            isLoading = false,
            selectedFilter = filter,
            stats = stats,
            recommendationShelves = shelves,
            isRefreshingRecommendations = isRefreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasteProfileUiState(isLoading = true)
    )

    private var recommendationPage = 1

    fun setFilter(filter: DiaryFilterType) {
        if (_selectedFilter.value == filter) return
        _selectedFilter.value = filter
        recommendationPage = 1
        loadRecommendations(filter, page = 1)
    }

    fun refreshRecommendations() {
        recommendationPage = if (recommendationPage >= 3) 1 else recommendationPage + 1
        loadRecommendations(_selectedFilter.value, page = recommendationPage)
    }

    private fun loadRecommendations(filter: DiaryFilterType, page: Int = 1) {
        viewModelScope.launch {
            _isRefreshing.value = true
            when (val result = getSmartRecommendationsUseCase(filterType = filter, page = page)) {
                is Result.Success -> {
                    _recommendationShelves.value = result.data
                }

                is Result.Error -> {
                    // Retain existing or set empty
                }
            }
            _isRefreshing.value = false
        }
    }

    fun getShareTasteText(stats: TasteProfileStats): String {
        val builder = StringBuilder()
        builder.appendLine("🎬 My ShowTime Cinephile Taste Profile:")
        builder.appendLine("✨ Persona: ${stats.persona.emoji} ${stats.persona.title}")
        builder.appendLine("⏱️ Watch Time: ${stats.totalWatchedHours} Hours (~${"%.1f".format(stats.totalWatchedDays)} Days)")
        builder.appendLine("📊 Total Logged: ${stats.totalItemsLogged} (${stats.totalMoviesLogged} Movies, ${stats.totalTvLogged} TV Shows)")
        builder.appendLine("★ Average Rating: ${stats.averageRating} / 5.0")
        if (stats.rewatchCount > 0) {
            builder.appendLine("🔁 Rewatches: ${stats.rewatchCount} (${stats.rewatchPercentage}%)")
        }
        if (stats.topRatedSeedTitles.isNotEmpty()) {
            builder.appendLine("🌟 Top Rated: ${stats.topRatedSeedTitles.joinToString(", ")}")
        }
        builder.appendLine("\nTrack your personal cinema journey with ShowTime! 🍿")
        return builder.toString()
    }
}
