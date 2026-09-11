package com.ssverma.feature.library.ui.share

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.convertToTmdbBackdropUrl
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.feature.library.R
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.library.SecretSharedList
import com.ssverma.shared.domain.model.library.SecretSharedListItem
import com.ssverma.shared.domain.repository.LibraryRepository
import com.ssverma.shared.domain.repository.SecretSharedListRepository
import com.ssverma.shared.domain.utils.ShareMediaUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

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
    val searchFilter: ChallengeMediaTypeFilter = ChallengeMediaTypeFilter.ALL,
    val searchResults: List<SecretSharedListItem> = emptyList(),
    val isSearching: Boolean = false,
    val showAddDialog: Boolean = false,
    val currentUserId: String = "",
    val currentUserName: String = "Cinephile",
    val isGoogleUser: Boolean = false,
    val feedbackMessage: String? = null
)

@OptIn(kotlinx.coroutines.FlowPreview::class)
@HiltViewModel
class SecretSharedListViewModel @Inject constructor(
    private val secretSharedListRepository: SecretSharedListRepository,
    private val libraryRepository: LibraryRepository,
    private val tmdbApiService: TmdbApiService,
    private val googleAuthClient: GoogleAuthClient,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private data class SearchQueryState(
        val query: String,
        val filter: ChallengeMediaTypeFilter
    )

    private val searchInputFlow =
        MutableStateFlow(SearchQueryState("", ChallengeMediaTypeFilter.ALL))

    private val persistentUserId: String by lazy {
        val prefs = context.getSharedPreferences("showtime_device_prefs", Context.MODE_PRIVATE)
        var uuid = prefs.getString("persistent_user_uuid", null)
        if (uuid.isNullOrBlank()) {
            uuid = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("persistent_user_uuid", uuid).apply()
        }
        uuid
    }

    private val effectiveUserId: String
        get() = googleAuthClient.currentFirebaseAuthUid
            ?.takeIf { it.isNotBlank() }
            ?: persistentUserId

    private fun getStoredUserName(): String? {
        return context.getSharedPreferences("showtime_device_prefs", Context.MODE_PRIVATE)
            .getString("user_display_name", null)
            ?.takeIf { it.isNotBlank() && !it.equals("Me", ignoreCase = true) }
    }

    private fun resolveCurrentUserName(): String {
        val stored = getStoredUserName()
        if (stored != null) return stored
        val googleName = googleAuthClient.currentUser.value?.displayName?.trim()
        return googleName?.takeIf { it.isNotBlank() }
            ?: "Cinephile #${abs(effectiveUserId.hashCode() % 900) + 100}"
    }

    private val userName: String
        get() = _uiState.value.currentUserName.takeIf { it.isNotBlank() }
            ?: resolveCurrentUserName()

    private val _uiState = MutableStateFlow(
        SecretSharedListUiState(
            currentUserId = effectiveUserId,
            currentUserName = resolveCurrentUserName(),
            isGoogleUser = googleAuthClient.currentUser.value != null
        )
    )
    val uiState: StateFlow<SecretSharedListUiState> = _uiState.asStateFlow()

    private var currentShareCode: String? = null
    private var observeJob: Job? = null
    private var inFlightAddedItem: SecretSharedListItem? = null
    private var inFlightRemovedMediaId: Int? = null

    init {
        viewModelScope.launch {
            googleAuthClient.currentUser.collectLatest { googleUser ->
                val resolvedName = resolveCurrentUserName()
                _uiState.update {
                    it.copy(
                        currentUserName = resolvedName,
                        isGoogleUser = googleUser != null
                    )
                }
            }
        }

        viewModelScope.launch {
            searchInputFlow
                .debounce(300L)
                .distinctUntilChanged()
                .collectLatest { state ->
                    val trimmed = state.query.trim()
                    if (trimmed.length >= 2) {
                        executeSearch(trimmed, state.filter)
                    }
                }
        }
    }

    fun updateUserDisplayName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            context.getSharedPreferences("showtime_device_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("user_display_name", trimmed)
                .apply()
            _uiState.update { it.copy(currentUserName = trimmed) }
        }
    }

    fun init(shareCode: String) {
        val normalizedCode = ShareMediaUtils.normalizeSecretShareCode(shareCode)
        if (normalizedCode.isBlank()) return
        if (currentShareCode == normalizedCode && observeJob != null) return
        currentShareCode = normalizedCode

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            secretSharedListRepository.observeSecretSharedList(normalizedCode)
                .collect { list ->
                    val isRevoked = list?.isRevoked == true
                    val isOwner =
                        list != null && (list.ownerUserId == persistentUserId || list.ownerUserId == effectiveUserId)
                    if (list != null && !isRevoked && !isOwner) {
                        libraryRepository.saveJoinedSecretList(
                            shareCode = normalizedCode,
                            title = list.title,
                            description = list.description,
                            ownerName = list.ownerName,
                            coverImageUrl = list.previewPosters.firstOrNull(),
                            itemCount = list.items.size,
                            isCollaborative = list.isCollaborative
                        )
                    }
                    val effectiveList = list?.let { l ->
                        var items = l.items
                        inFlightAddedItem?.let { added ->
                            if (items.none { it.mediaId == added.mediaId }) {
                                items = listOf(added) + items
                            } else {
                                inFlightAddedItem = null
                            }
                        }
                        inFlightRemovedMediaId?.let { removedId ->
                            if (items.any { it.mediaId == removedId }) {
                                items = items.filterNot { it.mediaId == removedId }
                            } else {
                                inFlightRemovedMediaId = null
                            }
                        }
                        l.copy(items = items)
                    }
                    if (effectiveList != null) {
                        resolveMissingReleaseYears(effectiveList)
                    }
                    _uiState.update { current ->
                        current.copy(
                            secretSharedList = effectiveList,
                            isLoading = false,
                            isRevoked = isRevoked,
                            isOwner = isOwner,
                            isCollaborative = list?.isCollaborative == true,
                            currentUserId = effectiveUserId
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
                    feedbackMessage = context.getString(R.string.secret_share_all_added_success)
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
                coverImageUrl = list.previewPosters.firstOrNull(),
                isCloned = true,
                sourceAuthorName = list.ownerName
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
                    feedbackMessage = context.getString(R.string.secret_share_cloned_success)
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

    fun searchMedia(
        query: String,
        filter: ChallengeMediaTypeFilter = _uiState.value.searchFilter
    ) {
        _uiState.update { it.copy(searchQuery = query, searchFilter = filter) }
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            searchInputFlow.value = SearchQueryState("", filter)
        } else {
            _uiState.update { it.copy(isSearching = true) }
            searchInputFlow.value = SearchQueryState(trimmed, filter)
        }
    }

    private suspend fun executeSearch(query: String, filter: ChallengeMediaTypeFilter) {
        _uiState.update { it.copy(isSearching = true) }
        when (val response = tmdbApiService.multiSearch(query)) {
            is ApiResponse.Success -> {
                val suggestions = response.body.results.orEmpty()
                    .filter {
                        when (filter) {
                            ChallengeMediaTypeFilter.ALL -> it.mediaType == "movie" || it.mediaType == "tv"
                            ChallengeMediaTypeFilter.MOVIE -> it.mediaType == "movie"
                            ChallengeMediaTypeFilter.TV -> it.mediaType == "tv"
                        }
                    }
                    .distinctBy { it.id }
                    .take(15)
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
                            overview = remote.overview.orEmpty(),
                            addedByName = userName,
                            addedByUserId = effectiveUserId,
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

    fun clearSearch() {
        searchInputFlow.value = SearchQueryState("", _uiState.value.searchFilter)
        _uiState.update {
            it.copy(searchQuery = "", searchResults = emptyList(), isSearching = false)
        }
    }

    fun addMediaToSharedList(item: SecretSharedListItem) {
        val code = currentShareCode ?: return
        val currentList = _uiState.value.secretSharedList
        if (currentList != null && currentList.items.any { it.mediaId == item.mediaId }) return

        val effectiveItem = if (item.addedByName.isNullOrBlank() || item.addedByName.equals(
                "Friend",
                ignoreCase = true
            )
        ) {
            item.copy(addedByName = userName, addedByUserId = effectiveUserId)
        } else {
            item.copy(addedByUserId = item.addedByUserId ?: effectiveUserId)
        }

        val previousList = currentList
        inFlightAddedItem = effectiveItem
        if (currentList != null) {
            val updatedItems = listOf(effectiveItem) + currentList.items
            _uiState.update {
                it.copy(
                    secretSharedList = currentList.copy(items = updatedItems),
                    isActionInProgress = true
                )
            }
        } else {
            _uiState.update { it.copy(isActionInProgress = true) }
        }

        viewModelScope.launch {
            val result = secretSharedListRepository.addMediaToSharedList(code, effectiveItem)
            when (result) {
                is Result.Success -> {
                    inFlightAddedItem = null
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            feedbackMessage = context.getString(
                                R.string.secret_share_item_added_success,
                                effectiveItem.title
                            )
                        )
                    }
                }

                is Result.Error -> {
                    inFlightAddedItem = null
                    _uiState.update {
                        it.copy(
                            secretSharedList = previousList,
                            isActionInProgress = false,
                            feedbackMessage = context.getString(R.string.secret_share_sync_failed)
                        )
                    }
                }
            }
        }
    }

    fun removeMediaFromSharedList(mediaId: Int) {
        val code = currentShareCode ?: return
        val currentList = _uiState.value.secretSharedList
        if (currentList != null && currentList.items.none { it.mediaId == mediaId }) return

        val previousList = currentList
        inFlightRemovedMediaId = mediaId
        if (currentList != null) {
            val updatedItems = currentList.items.filterNot { it.mediaId == mediaId }
            _uiState.update {
                it.copy(
                    secretSharedList = currentList.copy(items = updatedItems),
                    isActionInProgress = true
                )
            }
        } else {
            _uiState.update { it.copy(isActionInProgress = true) }
        }

        viewModelScope.launch {
            val result = secretSharedListRepository.removeMediaFromSharedList(code, mediaId)
            when (result) {
                is Result.Success -> {
                    inFlightRemovedMediaId = null
                    _uiState.update { it.copy(isActionInProgress = false) }
                }

                is Result.Error -> {
                    inFlightRemovedMediaId = null
                    _uiState.update {
                        it.copy(
                            secretSharedList = previousList,
                            isActionInProgress = false,
                            feedbackMessage = context.getString(R.string.secret_share_sync_failed)
                        )
                    }
                }
            }
        }
    }

    private fun resolveMissingReleaseYears(list: SecretSharedList) {
        val missingYearItems = list.items.filter { it.releaseYear.isNullOrBlank() }
        if (missingYearItems.isEmpty()) return

        viewModelScope.launch {
            val resolvedItems = list.items.map { item ->
                if (item.releaseYear.isNullOrBlank()) {
                    val year = runCatching {
                        if (item.mediaType == MediaType.Tv) {
                            (tmdbApiService.getTvShowDetails(
                                item.mediaId,
                                emptyMap()
                            ) as? ApiResponse.Success)
                                ?.body?.firstAirDate?.take(4)
                        } else {
                            (tmdbApiService.getMovieDetails(
                                item.mediaId,
                                emptyMap()
                            ) as? ApiResponse.Success)
                                ?.body?.releaseDate?.take(4)
                        }
                    }.getOrNull()
                    if (!year.isNullOrBlank()) item.copy(releaseYear = year) else item
                } else {
                    item
                }
            }
            if (resolvedItems != list.items) {
                _uiState.update { current ->
                    val currentList = current.secretSharedList
                    if (currentList?.shareCode == list.shareCode) {
                        current.copy(
                            secretSharedList = currentList.copy(items = resolvedItems)
                        )
                    } else current
                }
            }
        }
    }

    fun toggleCollaborativeMode() {
        val code = currentShareCode ?: return
        val newCollab = !_uiState.value.isCollaborative
        viewModelScope.launch {
            secretSharedListRepository.updateCollaborativeStatus(code, newCollab)
            val msgRes = if (newCollab) {
                R.string.secret_share_collab_enabled_msg
            } else {
                R.string.secret_share_collab_disabled_msg
            }
            _uiState.update { it.copy(feedbackMessage = context.getString(msgRes)) }
        }
    }

    fun leaveSharedList(onRemoved: () -> Unit = {}) {
        val code = currentShareCode ?: return
        viewModelScope.launch {
            libraryRepository.removeJoinedSecretList(code)
            onRemoved()
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
