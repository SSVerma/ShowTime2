package com.ssverma.feature.library.ui.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.feature.library.domain.ReceiptGeneratorHelper
import com.ssverma.feature.library.domain.model.ReceiptItem
import com.ssverma.feature.library.domain.model.ReceiptSnapshot
import com.ssverma.feature.library.domain.model.ReceiptSource
import com.ssverma.feature.library.domain.model.ReceiptStyle
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.model.library.SavedMediaItem
import com.ssverma.shared.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CinemaReceiptUiState(
    val selectedStyle: ReceiptStyle = ReceiptStyle.THERMAL,
    val selectedSource: ReceiptSource = ReceiptSource.HISTORY,
    val selectedCustomList: CustomList? = null,
    val customLists: List<CustomList> = emptyList(),
    val snapshot: ReceiptSnapshot? = null,
    val isExporting: Boolean = false
)

@HiltViewModel
class CinemaReceiptViewModel @Inject constructor(
    libraryRepository: LibraryRepository
) : ViewModel() {

    private val _selectedStyle = MutableStateFlow(ReceiptStyle.THERMAL)
    private val _selectedSource = MutableStateFlow(ReceiptSource.HISTORY)
    private val _selectedCustomList = MutableStateFlow<CustomList?>(null)
    private val _isExporting = MutableStateFlow(false)

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
            _isExporting
        ) { style, source, customList, isExporting ->
            ReceiptControlState(style, source, customList, isExporting)
        },
        combine(
            historyItems,
            favoriteItems,
            watchlistItems,
            customLists
        ) { history, favorites, watchlist, lists ->
            ReceiptMediaState(history, favorites, watchlist, lists)
        }
    ) { (style, source, customList, isExporting), (history, favorites, watchlist, lists) ->
        val snapshot = if (customList != null) {
            val mappedItems = customList.items.map { item ->
                ReceiptItem(
                    id = item.mediaId,
                    title = item.title,
                    year = "",
                    runtimeMinutes = if (item.mediaType == MediaType.Tv) 45 else 115,
                    rating = item.voteAvg
                )
            }
            ReceiptGeneratorHelper.generateSnapshot(
                title = customList.title,
                collectorName = "ShowTime Cinephile",
                items = mappedItems
            )
        } else {
            val itemsToMap = when (source) {
                ReceiptSource.HISTORY -> history
                ReceiptSource.FAVORITES -> favorites
                ReceiptSource.WATCHLIST -> watchlist
                ReceiptSource.THIS_MONTH -> {
                    val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                    history.filter { it.addedAt >= oneMonthAgo }
                }
            }
            val title = when (source) {
                ReceiptSource.HISTORY -> "Watch History"
                ReceiptSource.FAVORITES -> "Favorites"
                ReceiptSource.WATCHLIST -> "Watchlist"
                ReceiptSource.THIS_MONTH -> "This Month"
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
                collectorName = "ShowTime Cinephile",
                items = mappedItems
            )
        }

        CinemaReceiptUiState(
            selectedStyle = style,
            selectedSource = source,
            selectedCustomList = customList,
            customLists = lists,
            snapshot = snapshot,
            isExporting = isExporting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CinemaReceiptUiState()
    )

    fun selectStyle(style: ReceiptStyle) {
        _selectedStyle.update { style }
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

private data class ReceiptControlState(
    val style: ReceiptStyle,
    val source: ReceiptSource,
    val customList: CustomList?,
    val isExporting: Boolean
)

private data class ReceiptMediaState(
    val history: List<SavedMediaItem>,
    val favorites: List<SavedMediaItem>,
    val watchlist: List<SavedMediaItem>,
    val lists: List<CustomList>
)
