package com.ssverma.feature.library.ui.wrapped

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.backup.BackupRepository
import com.ssverma.core.billing.BillingRepository
import com.ssverma.feature.library.ui.wrapped.component.WrappedStoryStyle
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.ads.quota.RewardPassType
import com.ssverma.shared.domain.model.stats.CinephileMilestone
import com.ssverma.shared.domain.model.stats.WrappedYearSummary
import com.ssverma.shared.domain.usecase.stats.GetCinephileWrappedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CinephileWrappedViewModel @Inject constructor(
    private val getCinephileWrappedUseCase: GetCinephileWrappedUseCase,
    private val billingRepository: BillingRepository,
    private val rewardManager: RewardManager,
    private val rewardedAdManager: RewardedAdManager,
    backupRepository: BackupRepository
) : ViewModel() {

    private val _selectedYear = MutableStateFlow(0) // 0 = All-Time
    private val _selectedMilestone = MutableStateFlow<CinephileMilestone?>(null)
    private val _selectedStyle = MutableStateFlow(WrappedStoryStyle.CLASSIC_VELVET)
    private val _isWatermarkFree = MutableStateFlow(false)
    private val _isExporting = MutableStateFlow(false)
    private val _isProActive = MutableStateFlow(false)
    private val _isPassActive = MutableStateFlow(false)
    private val _isProPaymentEnabled = MutableStateFlow(true)
    private val _isGateOpen = MutableStateFlow(false)
    private val _isExportSheetOpen = MutableStateFlow(false)
    private val _pendingStyle = MutableStateFlow<WrappedStoryStyle?>(null)
    private val _userName = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            backupRepository.googleUser.collectLatest { googleUser ->
                _userName.value = googleUser?.displayName?.ifBlank { null }
            }
        }

        viewModelScope.launch {
            combine(
                billingRepository.isProActive,
                rewardManager.passStatus
            ) { isPro, passStatus ->
                isPro to passStatus.isWrappedStoryUnlocked
            }.collectLatest { (isPro, isPass) ->
                _isProActive.value = isPro
                _isPassActive.value = isPass
                if (isPro || isPass) {
                    _isWatermarkFree.value = true
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

    val uiState: StateFlow<CinephileWrappedUiState> = combine(
        combine(
            _selectedYear.flatMapLatest { year ->
                getCinephileWrappedUseCase(year)
            },
            _selectedYear,
            _selectedMilestone
        ) { summary, year, milestone ->
            WrappedBaseState(summary, year, milestone)
        },
        combine(
            _selectedStyle,
            _isWatermarkFree,
            _isProActive,
            _isPassActive,
            _isExporting
        ) { style, isWatermarkFree, isPro, isPass, isExporting ->
            WrappedStoryState1(style, isWatermarkFree, isPro, isPass, isExporting)
        },
        combine(
            _isProPaymentEnabled,
            _isGateOpen,
            _isExportSheetOpen,
            _pendingStyle,
            _userName
        ) { isProPaymentEnabled, isGateOpen, isExportSheetOpen, pendingStyle, userName ->
            WrappedStoryState2(
                isProPaymentEnabled,
                isGateOpen,
                isExportSheetOpen,
                pendingStyle,
                userName
            )
        }
    ) { base, story1, story2 ->
        CinephileWrappedUiState(
            summary = base.summary,
            selectedYear = base.year,
            availableYears = listOf(0) + base.summary.availableYears,
            selectedMilestone = base.milestone,
            isLoading = false,
            selectedStyle = story1.style,
            isWatermarkFree = story1.isWatermarkFree,
            isProActive = story1.isPro,
            isPassActive = story1.isPass,
            isExporting = story1.isExporting,
            isProPaymentEnabled = story2.isProPaymentEnabled,
            isGateOpen = story2.isGateOpen,
            isExportSheetOpen = story2.isExportSheetOpen,
            pendingStyle = story2.pendingStyle,
            userName = story2.userName
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

    fun openExportSheet() {
        _isExportSheetOpen.value = true
        rewardedAdManager.loadAd()
    }

    fun dismissExportSheet() {
        _isExportSheetOpen.value = false
    }

    fun selectStyle(style: WrappedStoryStyle) {
        _selectedStyle.update { style }
    }

    fun attemptExport(onAllowed: () -> Unit) {
        val currentStyle = _selectedStyle.value
        val isUnlocked = _isProActive.value || _isPassActive.value
        if (currentStyle.isProOnly && !isUnlocked) {
            _pendingStyle.value = currentStyle
            _isGateOpen.value = true
            rewardedAdManager.loadAd()
        } else {
            onAllowed()
        }
    }

    fun toggleWatermarkFree() {
        val isUnlocked = _isProActive.value || _isPassActive.value
        if (!isUnlocked) {
            _isGateOpen.value = true
            rewardedAdManager.loadAd()
        } else {
            _isWatermarkFree.update { !it }
        }
    }

    fun dismissGate() {
        _isGateOpen.value = false
        _pendingStyle.value = null
    }

    fun watchAdForWrappedPass(activity: Activity) {
        rewardedAdManager.showRewardedAdIfReady(activity) {
            viewModelScope.launch {
                rewardManager.grantRewardPass(RewardPassType.CINEMA_WRAPPED_STORY)
                _isPassActive.value = true
                _isWatermarkFree.value = true
                val pending = _pendingStyle.value
                if (pending != null) {
                    _selectedStyle.update { pending }
                    _pendingStyle.value = null
                }
                _isGateOpen.value = false
            }
        }
    }

    fun setExporting(exporting: Boolean) {
        _isExporting.value = exporting
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

private data class WrappedBaseState(
    val summary: WrappedYearSummary,
    val year: Int,
    val milestone: CinephileMilestone?
)

private data class WrappedStoryState1(
    val style: WrappedStoryStyle,
    val isWatermarkFree: Boolean,
    val isPro: Boolean,
    val isPass: Boolean,
    val isExporting: Boolean
)

private data class WrappedStoryState2(
    val isProPaymentEnabled: Boolean,
    val isGateOpen: Boolean,
    val isExportSheetOpen: Boolean,
    val pendingStyle: WrappedStoryStyle?,
    val userName: String?
)
