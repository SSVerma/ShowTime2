package com.ssverma.feature.library.ui.share

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.convertToTmdbBackdropUrl
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.SecretSharedList
import com.ssverma.shared.domain.model.library.SecretSharedListItem
import com.ssverma.shared.domain.repository.LibraryRepository
import com.ssverma.shared.domain.repository.SecretSharedListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecretSharedListUiState(
    val secretSharedList: SecretSharedList? = null,
    val isLoading: Boolean = true,
    val isRevoked: Boolean = false,
    val isOwner: Boolean = false,
    val isCollaborative: Boolean = false,
    val isAddingToWatchlist: Boolean = false,
    val isCloning: Boolean = false,
    val isActionInProgress: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<SecretSharedListItem> = emptyList(),
    val isSearching: Boolean = false,
    val showAddDialog: Boolean = false,
    val feedbackMessage: String? = null
)

@HiltViewModel
class SecretSharedListViewModel @Inject constructor(
    private val secretSharedListRepository: SecretSharedListRepository,
    private val libraryRepository: LibraryRepository,
    private val tmdbApiService: TmdbApiService,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecretSharedListUiState())
    val uiState: StateFlow<SecretSharedListUiState> = _uiState.asStateFlow()

    private var currentShareCode: String? = null
    private var observeJob: Job? = null

    private val persistentUserId: String by lazy {
        context.getSharedPreferences("showtime_device_prefs", Context.MODE_PRIVATE)
            .getString("persistent_user_uuid", "").orEmpty()
    }

    fun init(shareCode: String) {
        val normalizedCode = shareCode.trim().uppercase()
        if (currentShareCode == normalizedCode && observeJob != null) return
        currentShareCode = normalizedCode

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            secretSharedListRepository.observeSecretSharedList(normalizedCode)
                .collect { list ->
                    _uiState.update { current ->
                        current.copy(
                            secretSharedList = list,
                            isLoading = false,
                            isRevoked = list?.isRevoked == true,
                            isOwner = list != null && list.ownerUserId == persistentUserId,
                            isCollaborative = list?.isCollaborative == true
                        )
                    }
                }
        }
    }

    fun addAllToWatchlist() {
        val list = _uiState.value.secretSharedList ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingToWatchlist = true) }
            list.items.forEach { item ->
                val alreadyInWatchlist = libraryRepository.isInWatchlistFlow(item.mediaId).first()
                if (!alreadyInWatchlist) {
                    libraryRepository.toggleWatchlist(
                        mediaId = item.mediaId,
                        mediaType = item.mediaType,
                        title = item.title,
                        posterImageUrl = item.posterImageUrl,
                        backdropImageUrl = item.backdropImageUrl,
                        voteAvg = item.voteAvg,
                        releaseDate = item.releaseYear.orEmpty()
                    )
                }
            }
            _uiState.update {
                it.copy(
                    isAddingToWatchlist = false,
                    feedbackMessage = "All titles added to your Watchlist!"
                )
            }
        }
    }

    fun cloneToMyLists() {
        val list = _uiState.value.secretSharedList ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCloning = true) }
            val listId = libraryRepository.createCustomList(
                title = list.title,
                description = list.description,
                coverImageUrl = list.previewPosters.firstOrNull()
            )
            list.items.forEach { item ->
                libraryRepository.addMediaToCustomList(
                    listId = listId,
                    mediaId = item.mediaId,
                    mediaType = item.mediaType,
                    title = item.title,
                    posterImageUrl = item.posterImageUrl,
                    backdropImageUrl = item.backdropImageUrl,
                    voteAvg = item.voteAvg
                )
            }
            _uiState.update {
                it.copy(
                    isCloning = false,
                    feedbackMessage = "List cloned to your Custom Lists!"
                )
            }
        }
    }

    fun setShowAddDialog(show: Boolean) {
        _uiState.update {
            it.copy(
                showAddDialog = show,
                searchQuery = "",
                searchResults = emptyList()
            )
        }
    }

    fun searchMedia(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            when (val response = tmdbApiService.multiSearch(query)) {
                is ApiResponse.Success -> {
                    val suggestions = response.body.results.orEmpty()
                        .filter { it.mediaType == "movie" || it.mediaType == "tv" }
                        .map { remote ->
                            val isTv = remote.mediaType == "tv"
                            SecretSharedListItem(
                                mediaId = remote.id,
                                mediaType = if (isTv) MediaType.Tv else MediaType.Movie,
                                title = remote.name.orEmpty(),
                                posterImageUrl = remote.posterPath?.convertToTmdbPosterUrl()
                                    .orEmpty(),
                                backdropImageUrl = remote.backdropPath?.convertToTmdbBackdropUrl()
                                    .orEmpty(),
                                voteAvg = remote.voteAvg,
                                releaseYear = remote.releaseDate?.take(4)
                                    ?: remote.firstAirDate?.take(4),
                                addedByName = "Collaborator",
                                addedAtEpochMs = System.currentTimeMillis()
                            )
                        }
                    _uiState.update { it.copy(searchResults = suggestions, isSearching = false) }
                }

                else -> {
                    _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                }
            }
        }
    }

    fun addMediaToSharedList(item: SecretSharedListItem) {
        val code = currentShareCode ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true) }
            secretSharedListRepository.addMediaToSharedList(code, item)
            _uiState.update {
                it.copy(
                    isActionInProgress = false,
                    showAddDialog = false,
                    feedbackMessage = "\"${item.title}\" added to list!"
                )
            }
        }
    }

    fun removeMediaFromSharedList(mediaId: Int) {
        val code = currentShareCode ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true) }
            secretSharedListRepository.removeMediaFromSharedList(code, mediaId)
            _uiState.update { it.copy(isActionInProgress = false) }
        }
    }

    fun revokeSecretShare(onRevoked: () -> Unit) {
        val code = currentShareCode ?: return
        viewModelScope.launch {
            secretSharedListRepository.revokeSecretShare(code)
            _uiState.update { it.copy(isRevoked = true) }
            onRevoked()
        }
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
