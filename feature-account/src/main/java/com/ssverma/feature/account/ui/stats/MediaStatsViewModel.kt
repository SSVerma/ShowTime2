package com.ssverma.feature.account.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.feature.account.domain.model.MediaStats
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaStatsViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _mediaStats: MutableStateFlow<MediaStatsUiState> =
        MutableStateFlow(MediaStatsUiState.Loading)

    val mediaStats = _mediaStats.asStateFlow()

    fun isMediaActionActiveFlow(mediaId: Int): Flow<Boolean> =
        libraryRepository.isMediaActionActiveFlow(mediaId)

    fun fetchMediaStats(mediaType: MediaType, mediaId: Int) {
        viewModelScope.launch {
            val isFavorite = libraryRepository.isFavorite(mediaId)
            val isInWatchlist = libraryRepository.isInWatchlist(mediaId)
            val isWatched = libraryRepository.isWatched(mediaId)

            _mediaStats.value = MediaStatsUiState.Success(
                MediaStats(
                    mediaId = mediaId,
                    favorite = isFavorite,
                    inWatchlist = isInWatchlist,
                    isWatched = isWatched,
                    rating = null
                )
            )
        }
    }

    fun toggleMediaFavoriteStatus(
        mediaType: MediaType,
        mediaId: Int,
        title: String = "",
        posterImageUrl: String = "",
        backdropImageUrl: String = "",
        voteAvg: Float = 0f,
        releaseDate: String = ""
    ) {
        viewModelScope.launch {
            val newStatus = libraryRepository.toggleFavorite(
                mediaId = mediaId,
                mediaType = mediaType,
                title = title,
                posterImageUrl = posterImageUrl,
                backdropImageUrl = backdropImageUrl,
                voteAvg = voteAvg,
                releaseDate = releaseDate
            )
            val currentStats = (_mediaStats.value as? MediaStatsUiState.Success)?.mediaStats
            _mediaStats.value = MediaStatsUiState.Success(
                currentStats?.copy(favorite = newStatus) ?: MediaStats(
                    mediaId = mediaId,
                    favorite = newStatus,
                    inWatchlist = false,
                    isWatched = false,
                    rating = null
                )
            )
        }
    }

    fun toggleMediaWatchlistStatus(
        mediaType: MediaType,
        mediaId: Int,
        title: String = "",
        posterImageUrl: String = "",
        backdropImageUrl: String = "",
        voteAvg: Float = 0f,
        releaseDate: String = ""
    ) {
        viewModelScope.launch {
            val newStatus = libraryRepository.toggleWatchlist(
                mediaId = mediaId,
                mediaType = mediaType,
                title = title,
                posterImageUrl = posterImageUrl,
                backdropImageUrl = backdropImageUrl,
                voteAvg = voteAvg,
                releaseDate = releaseDate
            )
            val currentStats = (_mediaStats.value as? MediaStatsUiState.Success)?.mediaStats
            _mediaStats.value = MediaStatsUiState.Success(
                currentStats?.copy(inWatchlist = newStatus) ?: MediaStats(
                    mediaId = mediaId,
                    favorite = false,
                    inWatchlist = newStatus,
                    isWatched = false,
                    rating = null
                )
            )
        }
    }

    fun toggleWatchHistoryStatus(
        mediaType: MediaType,
        mediaId: Int,
        title: String = "",
        posterImageUrl: String = "",
        voteAvg: Float = 0f
    ) {
        viewModelScope.launch {
            val newStatus = libraryRepository.toggleWatchHistory(
                mediaId = mediaId,
                mediaType = mediaType,
                title = title,
                posterImageUrl = posterImageUrl,
                voteAvg = voteAvg
            )
            val currentStats = (_mediaStats.value as? MediaStatsUiState.Success)?.mediaStats
            _mediaStats.value = MediaStatsUiState.Success(
                currentStats?.copy(isWatched = newStatus) ?: MediaStats(
                    mediaId = mediaId,
                    favorite = false,
                    inWatchlist = false,
                    isWatched = newStatus,
                    rating = null
                )
            )
        }
    }

    val customLists: Flow<List<CustomList>> = libraryRepository.getCustomListsFlow()

    fun getCustomListIdsForMedia(mediaId: Int): Flow<List<String>> =
        libraryRepository.getCustomListIdsForMediaFlow(mediaId)

    fun toggleMediaCustomList(
        listId: String,
        mediaId: Int,
        mediaType: MediaType,
        title: String = "",
        posterImageUrl: String = "",
        backdropImageUrl: String = "",
        voteAvg: Float = 0f,
        isCurrentlyInList: Boolean
    ) {
        viewModelScope.launch {
            if (isCurrentlyInList) {
                libraryRepository.removeMediaFromCustomList(listId, mediaId)
            } else {
                libraryRepository.addMediaToCustomList(
                    listId = listId,
                    mediaId = mediaId,
                    mediaType = mediaType,
                    title = title,
                    posterImageUrl = posterImageUrl,
                    backdropImageUrl = backdropImageUrl,
                    voteAvg = voteAvg
                )
            }
        }
    }
}