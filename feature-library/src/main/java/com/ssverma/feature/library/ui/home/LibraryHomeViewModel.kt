package com.ssverma.feature.library.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.feature.library.ui.home.component.MediaTypeFilter
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.model.library.SavedMediaItem
import com.ssverma.shared.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryHomeViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    val watchlistItems: StateFlow<List<SavedMediaItem>> = libraryRepository.getAllWatchlist()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val favoriteItems: StateFlow<List<SavedMediaItem>> = libraryRepository.getAllFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val historyItems: StateFlow<List<SavedMediaItem>> = libraryRepository.getAllWatchHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val customLists: StateFlow<List<CustomList>> = libraryRepository.getCustomListsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _watchlistFilter = MutableStateFlow(MediaTypeFilter.ALL)
    val watchlistFilter: StateFlow<MediaTypeFilter> = _watchlistFilter.asStateFlow()

    private val _favoritesFilter = MutableStateFlow(MediaTypeFilter.ALL)
    val favoritesFilter: StateFlow<MediaTypeFilter> = _favoritesFilter.asStateFlow()

    private val _historyFilter = MutableStateFlow(MediaTypeFilter.ALL)
    val historyFilter: StateFlow<MediaTypeFilter> = _historyFilter.asStateFlow()

    private val _selectedCustomListId = MutableStateFlow<String?>(null)
    val selectedCustomListId: StateFlow<String?> = _selectedCustomListId.asStateFlow()

    val selectedCustomList: StateFlow<CustomList?> = _selectedCustomListId
        .flatMapLatest { listId ->
            if (listId != null) {
                libraryRepository.getCustomListWithItemsFlow(listId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun setWatchlistFilter(filter: MediaTypeFilter) {
        _watchlistFilter.value = filter
    }

    fun setFavoritesFilter(filter: MediaTypeFilter) {
        _favoritesFilter.value = filter
    }

    fun setHistoryFilter(filter: MediaTypeFilter) {
        _historyFilter.value = filter
    }

    fun selectCustomList(listId: String?) {
        _selectedCustomListId.value = listId
    }

    fun removeFromWatchlist(mediaId: Int) {
        viewModelScope.launch {
            libraryRepository.deleteWatchlist(mediaId)
        }
    }

    fun removeFromFavorites(mediaId: Int) {
        viewModelScope.launch {
            libraryRepository.deleteFavorite(mediaId)
        }
    }

    fun removeFromHistory(mediaId: Int) {
        viewModelScope.launch {
            libraryRepository.deleteWatchHistory(mediaId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            libraryRepository.clearWatchHistory()
        }
    }

    fun createCustomList(
        title: String,
        description: String? = null,
        onCreated: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val id = libraryRepository.createCustomList(title = title, description = description)
            onCreated?.invoke(id)
        }
    }

    fun updateCustomList(listId: String, title: String, description: String?) {
        viewModelScope.launch {
            libraryRepository.updateCustomList(listId, title, description)
        }
    }

    fun deleteCustomList(listId: String) {
        viewModelScope.launch {
            if (_selectedCustomListId.value == listId) {
                _selectedCustomListId.value = null
            }
            libraryRepository.deleteCustomList(listId)
        }
    }

    fun removeItemFromCustomList(listId: String, mediaId: Int) {
        viewModelScope.launch {
            libraryRepository.removeMediaFromCustomList(listId, mediaId)
        }
    }
}