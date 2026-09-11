package com.ssverma.feature.library.ui.taste

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.backup.BackupRepository
import com.ssverma.core.billing.BillingRepository
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.ads.quota.RewardPassType
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TasteProfileViewModel @Inject constructor(
    private val getTasteProfileUseCase: GetTasteProfileUseCase,
    private val getSmartRecommendationsUseCase: GetSmartRecommendationsUseCase,
    private val billingRepository: BillingRepository,
    private val backupRepository: BackupRepository,
    private val rewardManager: RewardManager,
    private val rewardedAdManager: RewardedAdManager
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(DiaryFilterType.ALL)
    private val _recommendationShelves =
        MutableStateFlow<List<RecommendationShelf>>(emptyList())
    private val _isRefreshing = MutableStateFlow(false)
    private val _isProActive = MutableStateFlow(false)
    private val _isPassActive = MutableStateFlow(false)
    private val _isProPaymentEnabled = MutableStateFlow(true)
    private val _isGateOpen = MutableStateFlow(false)
    private val _userName = MutableStateFlow<String?>(null)
    private val _isShareSheetOpen = MutableStateFlow(false)
    private val _isExporting = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            backupRepository.googleUser.collectLatest { user ->
                _userName.value = user?.displayName?.ifBlank { null }
            }
        }

        viewModelScope.launch {
            combine(
                billingRepository.isProActive,
                rewardManager.passStatus
            ) { isPro, passStatus ->
                isPro to passStatus.isTasteAnalyticsUnlocked
            }.collectLatest { (isPro, isPass) ->
                val wasUnlocked = _isProActive.value || _isPassActive.value
                val isNowUnlocked = isPro || isPass
                _isProActive.value = isPro
                _isPassActive.value = isPass
                if (isNowUnlocked && (!wasUnlocked || _recommendationShelves.value.isEmpty())) {
                    loadRecommendations(_selectedFilter.value, page = 1)
                }
            }
        }

        viewModelScope.launch {
            billingRepository.isBillingEnabled.collectLatest { isEnabled ->
                _isProPaymentEnabled.value = isEnabled
            }
        }

        rewardedAdManager.loadAd()
    }

    val uiState: StateFlow<TasteProfileUiState> = combine(
        combine(
            _selectedFilter.flatMapLatest { filter ->
                getTasteProfileUseCase(filter)
            },
            _recommendationShelves,
            _selectedFilter,
            _isRefreshing
        ) { stats, shelves, filter, isRefreshing ->
            TasteDataState(stats, shelves, filter, isRefreshing)
        },
        combine(
            _isProActive,
            _isPassActive,
            _isProPaymentEnabled,
            _isGateOpen
        ) { isPro, isPass, isProPaymentEnabled, isGateOpen ->
            TasteAuthState(isPro, isPass, isProPaymentEnabled, isGateOpen)
        },
        combine(
            _userName,
            _isShareSheetOpen,
            _isExporting
        ) { userName, isShareSheetOpen, isExporting ->
            TasteExportState(userName, isShareSheetOpen, isExporting)
        }
    ) { dataState, authState, exportState ->
        TasteProfileUiState(
            isLoading = false,
            selectedFilter = dataState.filter,
            stats = dataState.stats,
            recommendationShelves = dataState.shelves,
            isRefreshingRecommendations = dataState.isRefreshing,
            isProActive = authState.isPro,
            isPassActive = authState.isPass,
            isProPaymentEnabled = authState.isProPaymentEnabled,
            isGateOpen = authState.isGateOpen,
            userName = exportState.userName,
            isShareSheetOpen = exportState.isShareSheetOpen,
            isExporting = exportState.isExporting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasteProfileUiState(isLoading = true)
    )

    private var recommendationPage = 1

    fun openGate() {
        _isGateOpen.value = true
        rewardedAdManager.loadAd()
    }

    fun dismissGate() {
        _isGateOpen.value = false
    }

    fun openShareSheet() {
        _isShareSheetOpen.value = true
    }

    fun dismissShareSheet() {
        _isShareSheetOpen.value = false
    }

    fun setExporting(isExporting: Boolean) {
        _isExporting.value = isExporting
    }

    fun watchAdForTasteRadarPass(activity: Activity) {
        rewardedAdManager.showRewardedAdIfReady(activity) {
            viewModelScope.launch {
                rewardManager.grantRewardPass(RewardPassType.TASTE_ANALYTICS_RADAR)
                _isGateOpen.value = false
            }
        }
    }

    fun setFilter(filter: DiaryFilterType) {
        if (_selectedFilter.value == filter) return
        _selectedFilter.value = filter
        recommendationPage = 1
        if (_isProActive.value || _isPassActive.value) {
            loadRecommendations(filter, page = 1)
        }
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

private data class TasteDataState(
    val stats: TasteProfileStats,
    val shelves: List<RecommendationShelf>,
    val filter: DiaryFilterType,
    val isRefreshing: Boolean
)

private data class TasteAuthState(
    val isPro: Boolean,
    val isPass: Boolean,
    val isProPaymentEnabled: Boolean,
    val isGateOpen: Boolean
)

private data class TasteExportState(
    val userName: String?,
    val isShareSheetOpen: Boolean,
    val isExporting: Boolean
)
