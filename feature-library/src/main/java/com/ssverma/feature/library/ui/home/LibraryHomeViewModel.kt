package com.ssverma.feature.library.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.feature.library.ui.home.component.MediaTypeFilter
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.community.CloneCommunityListParams
import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.model.community.CommunityListCategories
import com.ssverma.shared.domain.model.community.PublishCustomListParams
import com.ssverma.shared.domain.model.community.ToggleListUpvoteParams
import com.ssverma.shared.domain.model.community.UnpublishCustomListParams
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.model.library.SavedMediaItem
import com.ssverma.shared.domain.repository.LibraryRepository
import com.ssverma.shared.domain.usecase.community.CloneCommunityListUseCase
import com.ssverma.shared.domain.usecase.community.GetCommunityListDetailsUseCase
import com.ssverma.shared.domain.usecase.community.GetCommunityListsUseCase
import com.ssverma.shared.domain.usecase.community.PublishCustomListUseCase
import com.ssverma.shared.domain.usecase.community.ToggleCommunityListUpvoteUseCase
import com.ssverma.shared.domain.usecase.community.UnpublishCustomListUseCase
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
    private val libraryRepository: LibraryRepository,
    private val getCommunityListsUseCase: GetCommunityListsUseCase,
    private val getCommunityListDetailsUseCase: GetCommunityListDetailsUseCase,
    private val publishCustomListUseCase: PublishCustomListUseCase,
    private val unpublishCustomListUseCase: UnpublishCustomListUseCase,
    private val toggleCommunityListUpvoteUseCase: ToggleCommunityListUpvoteUseCase,
    private val cloneCommunityListUseCase: CloneCommunityListUseCase
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

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    fun setSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

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

    fun deleteCustomList(listId: String, isPublic: Boolean = false) {
        viewModelScope.launch {
            if (_selectedCustomListId.value == listId) {
                _selectedCustomListId.value = null
            }
            if (isPublic) {
                unpublishCustomListUseCase(UnpublishCustomListParams(listId = listId))
            }
            libraryRepository.deleteCustomList(listId)
        }
    }

    fun removeItemFromCustomList(listId: String, mediaId: Int) {
        viewModelScope.launch {
            libraryRepository.removeMediaFromCustomList(listId, mediaId)
        }
    }

    // Community Curated Lists
    private val _selectedCommunityCategory = MutableStateFlow(CommunityListCategories.ALL)
    val selectedCommunityCategory: StateFlow<String> = _selectedCommunityCategory.asStateFlow()

    val communityLists: StateFlow<List<CommunityCuratedList>> = _selectedCommunityCategory
        .flatMapLatest { category ->
            getCommunityListsUseCase(if (category == CommunityListCategories.ALL) null else category)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _selectedCommunityListId = MutableStateFlow<String?>(null)
    val selectedCommunityListId: StateFlow<String?> = _selectedCommunityListId.asStateFlow()

    val selectedCommunityList: StateFlow<CommunityCuratedList?> = _selectedCommunityListId
        .flatMapLatest { listId ->
            if (listId == null) {
                flowOf(null)
            } else {
                getCommunityListDetailsUseCase(listId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun setCommunityCategory(category: String) {
        _selectedCommunityCategory.value = category
    }

    fun selectCommunityList(list: CommunityCuratedList?) {
        _selectedCommunityListId.value = list?.listId
    }

    fun publishCustomList(
        localList: CustomList,
        categoryTag: String,
        onPublished: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = publishCustomListUseCase(
                PublishCustomListParams(
                    localList = localList,
                    categoryTag = categoryTag
                )
            )
            if (result is Result.Success) {
                onPublished?.invoke()
            } else if (result is Result.Error) {
                onError?.invoke("Unable to publish collection. Please try again.")
            }
        }
    }

    fun unpublishCustomList(
        listId: String,
        onUnpublished: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = unpublishCustomListUseCase(
                UnpublishCustomListParams(listId = listId)
            )
            if (result is Result.Success) {
                onUnpublished?.invoke()
            } else if (result is Result.Error) {
                onError?.invoke("Unable to make collection private. Please try again.")
            }
        }
    }

    fun toggleCommunityListUpvote(listId: String) {
        viewModelScope.launch {
            toggleCommunityListUpvoteUseCase(
                ToggleListUpvoteParams(listId = listId)
            )
        }
    }

    fun cloneCommunityList(
        communityList: CommunityCuratedList,
        onCloned: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = cloneCommunityListUseCase(
                CloneCommunityListParams(communityList = communityList)
            )
            if (result is Result.Success) {
                onCloned?.invoke(result.data)
            } else if (result is Result.Error) {
                onError?.invoke("Unable to clone collection. Please try again.")
            }
        }
    }
}