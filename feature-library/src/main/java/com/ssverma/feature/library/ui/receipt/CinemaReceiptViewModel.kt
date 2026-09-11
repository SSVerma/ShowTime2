package com.ssverma.feature.library.ui.receipt

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.backup.BackupRepository
import com.ssverma.core.billing.BillingRepository
import com.ssverma.feature.library.domain.ReceiptGeneratorHelper
import com.ssverma.feature.library.domain.model.ReceiptItem
import com.ssverma.feature.library.domain.model.ReceiptSnapshot
import com.ssverma.feature.library.domain.model.ReceiptSource
import com.ssverma.feature.library.domain.model.ReceiptStyle
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.ads.quota.RewardPassType
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.model.library.SavedMediaItem
import com.ssverma.shared.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CinemaReceiptUiState(
    val selectedStyle: ReceiptStyle = ReceiptStyle.THERMAL,
    val selectedSource: ReceiptSource = ReceiptSource.HISTORY,
    val selectedCustomList: CustomList? = null,
    val customLists: List<CustomList> = emptyList(),
    val snapshot: ReceiptSnapshot? = null,
    val isExporting: Boolean = false,
    val isProActive: Boolean = false,
    val isPassActive: Boolean = false,
    val isProPaymentEnabled: Boolean = true,
    val isGateOpen: Boolean = false,
    val pendingStyle: ReceiptStyle? = null,
    val theaterName: String = "SHOWTIME CINEMA",
    val collectorName: String = "SHOWTIME CINEPHILE",
    val isEditPersonalizationOpen: Boolean = false
)

@HiltViewModel
class CinemaReceiptViewModel @Inject constructor(
    libraryRepository: LibraryRepository,
    private val billingRepository: BillingRepository,
    private val rewardManager: RewardManager,
    private val rewardedAdManager: RewardedAdManager,
    backupRepository: BackupRepository
) : ViewModel() {

    private val _selectedStyle = MutableStateFlow(ReceiptStyle.THERMAL)
    private val _selectedSource = MutableStateFlow(ReceiptSource.HISTORY)
    private val _selectedCustomList = MutableStateFlow<CustomList?>(null)
    private val _isExporting = MutableStateFlow(false)
    private val _isProActive = MutableStateFlow(false)
    private val _isPassActive = MutableStateFlow(false)
    private val _isProPaymentEnabled = MutableStateFlow(true)
    private val _isGateOpen = MutableStateFlow(false)
    private val _pendingStyle = MutableStateFlow<ReceiptStyle?>(null)
    private val _theaterName = MutableStateFlow("SHOWTIME CINEMA")
    private val _collectorName = MutableStateFlow("SHOWTIME CINEPHILE")
    private val _isEditPersonalizationOpen = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            backupRepository.googleUser.collectLatest { googleUser ->
                val name = googleUser?.displayName?.ifBlank { null }
                if (name != null && _collectorName.value == "SHOWTIME CINEPHILE") {
                    _collectorName.value = name
                }
            }
        }

        viewModelScope.launch {
            combine(
                billingRepository.isProActive,
                rewardManager.passStatus
            ) { isPro, passStatus ->
                isPro to passStatus.isReceiptWatermarkFreeUnlocked
            }.collectLatest { (isPro, isPass) ->
                _isProActive.value = isPro
                _isPassActive.value = isPass
            }
        }

        viewModelScope.launch {
            billingRepository.isBillingEnabled.collectLatest { isEnabled ->
                _isProPaymentEnabled.value = isEnabled
            }
        }

        rewardedAdManager.loadAd()
    }

    val historyItems: StateFlow<List<SavedMediaItem>> = libraryRepository.getAllWatchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteItems: StateFlow<List<SavedMediaItem>> = libraryRepository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlistItems: StateFlow<List<SavedMediaItem>> = libraryRepository.getAllWatchlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customLists: StateFlow<List<CustomList>> = libraryRepository.getCustomListsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<CinemaReceiptUiState> = combine(
        combine(
            _selectedStyle,
            _selectedSource,
            _selectedCustomList,
            _isExporting,
            _isProActive
        ) { style, source, customList, isExporting, isPro ->
            ReceiptControlState1(style, source, customList, isExporting, isPro)
        },
        combine(
            _isPassActive,
            _isProPaymentEnabled,
            _isGateOpen,
            _pendingStyle
        ) { isPass, isProPaymentEnabled, isGateOpen, pendingStyle ->
            ReceiptControlState2(
                isPass,
                isProPaymentEnabled,
                isGateOpen,
                pendingStyle
            )
        },
        combine(
            _theaterName,
            _collectorName,
            _isEditPersonalizationOpen
        ) { theaterName, collectorName, isEditOpen ->
            ReceiptPersonalizationState(theaterName, collectorName, isEditOpen)
        },
        combine(
            historyItems,
            favoriteItems,
            watchlistItems,
            customLists
        ) { history, favorites, watchlist, lists ->
            ReceiptMediaState(history, favorites, watchlist, lists)
        }
    ) { ctrl1, ctrl2, personal, media ->
        val snapshot = if (ctrl1.customList != null) {
            val mappedItems = ctrl1.customList.items.map { item ->
                ReceiptItem(
                    id = item.mediaId,
                    title = item.title,
                    year = "",
                    runtimeMinutes = if (item.mediaType == MediaType.Tv) 45 else 115,
                    rating = item.voteAvg
                )
            }
            ReceiptGeneratorHelper.generateSnapshot(
                title = ctrl1.customList.title,
                collectorName = personal.collectorName,
                theaterName = personal.theaterName,
                items = mappedItems
            )
        } else {
            val itemsToMap = when (ctrl1.source) {
                ReceiptSource.HISTORY -> media.history
                ReceiptSource.FAVORITES -> media.favorites
                ReceiptSource.WATCHLIST -> media.watchlist
                ReceiptSource.THIS_MONTH -> {
                    val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                    media.history.filter { it.addedAt >= oneMonthAgo }
                }

                ReceiptSource.THIS_YEAR -> {
                    val oneYearAgo = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
                    media.history.filter { it.addedAt >= oneYearAgo }
                }

                ReceiptSource.LAST_90_DAYS -> {
                    val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
                    media.history.filter { it.addedAt >= ninetyDaysAgo }
                }
            }
            val title = when (ctrl1.source) {
                ReceiptSource.HISTORY -> "Watch History"
                ReceiptSource.FAVORITES -> "Favorites"
                ReceiptSource.WATCHLIST -> "Watchlist"
                ReceiptSource.THIS_MONTH -> "This Month"
                ReceiptSource.THIS_YEAR -> "This Year"
                ReceiptSource.LAST_90_DAYS -> "Last 90 Days"
            }
            val mappedItems = itemsToMap.map { item ->
                ReceiptItem(
                    id = item.mediaId,
                    title = item.title,
                    year = item.releaseDate.take(4),
                    runtimeMinutes = if (item.mediaType == MediaType.Tv) 45 else 115,
                    rating = item.voteAvg
                )
            }
            ReceiptGeneratorHelper.generateSnapshot(
                title = title,
                collectorName = personal.collectorName,
                theaterName = personal.theaterName,
                items = mappedItems
            )
        }

        CinemaReceiptUiState(
            selectedStyle = ctrl1.style,
            selectedSource = ctrl1.source,
            selectedCustomList = ctrl1.customList,
            customLists = media.lists,
            snapshot = snapshot,
            isExporting = ctrl1.isExporting,
            isProActive = ctrl1.isPro,
            isPassActive = ctrl2.isPass,
            isProPaymentEnabled = ctrl2.isProPaymentEnabled,
            isGateOpen = ctrl2.isGateOpen,
            pendingStyle = ctrl2.pendingStyle,
            theaterName = personal.theaterName,
            collectorName = personal.collectorName,
            isEditPersonalizationOpen = personal.isEditOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CinemaReceiptUiState()
    )

    fun selectStyle(style: ReceiptStyle) {
        _selectedStyle.update { style }
    }

    fun dismissGate() {
        _isGateOpen.value = false
        _pendingStyle.value = null
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

    fun watchAdForWatermarkFreePass(activity: Activity) {
        rewardedAdManager.showRewardedAdIfReady(activity) {
            viewModelScope.launch {
                rewardManager.grantRewardPass(RewardPassType.WATERMARK_FREE_RECEIPT)
                _isPassActive.value = true
                val pending = _pendingStyle.value
                if (pending != null) {
                    _selectedStyle.update { pending }
                    _pendingStyle.value = null
                }
                _isGateOpen.value = false
            }
        }
    }

    fun updatePersonalization(theaterName: String, collectorName: String) {
        _theaterName.update { theaterName.ifBlank { "SHOWTIME CINEMA" } }
        _collectorName.update { collectorName.ifBlank { "SHOWTIME CINEPHILE" } }
        _isEditPersonalizationOpen.update { false }
    }

    fun setEditPersonalizationOpen(isOpen: Boolean) {
        _isEditPersonalizationOpen.update { isOpen }
    }

    fun selectSource(source: ReceiptSource) {
        _selectedCustomList.update { null }
        _selectedSource.update { source }
    }

    fun selectCustomList(customList: CustomList?) {
        _selectedCustomList.update { customList }
    }

    fun setExporting(exporting: Boolean) {
        _isExporting.update { exporting }
    }
}

private data class ReceiptControlState1(
    val style: ReceiptStyle,
    val source: ReceiptSource,
    val customList: CustomList?,
    val isExporting: Boolean,
    val isPro: Boolean
)

private data class ReceiptControlState2(
    val isPass: Boolean,
    val isProPaymentEnabled: Boolean,
    val isGateOpen: Boolean,
    val pendingStyle: ReceiptStyle?
)

private data class ReceiptPersonalizationState(
    val theaterName: String,
    val collectorName: String,
    val isEditOpen: Boolean
)

private data class ReceiptMediaState(
    val history: List<SavedMediaItem>,
    val favorites: List<SavedMediaItem>,
    val watchlist: List<SavedMediaItem>,
    val lists: List<CustomList>
)
