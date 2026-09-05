package com.ssverma.shared.ui.component.media.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaOmniMenuViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    val customLists: Flow<List<CustomList>> = libraryRepository.getCustomListsFlow()

    fun getCustomListIdsForMedia(mediaId: Int): Flow<List<String>> =
        libraryRepository.getCustomListIdsForMediaFlow(mediaId)

    fun isInWatchlist(mediaId: Int): Flow<Boolean> =
        libraryRepository.isInWatchlistFlow(mediaId)

    fun isFavorite(mediaId: Int): Flow<Boolean> =
        libraryRepository.isFavoriteFlow(mediaId)

    fun isWatched(mediaId: Int): Flow<Boolean> =
        libraryRepository.isWatchedFlow(mediaId)

    fun isMediaActionActive(mediaId: Int): Flow<Boolean> =
        libraryRepository.isMediaActionActiveFlow(mediaId)

    fun toggleWatchlist(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        backdropImageUrl: String,
        voteAvg: Float,
        releaseDate: String,
        onResult: ((added: Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val added = libraryRepository.toggleWatchlist(
                mediaId = mediaId,
                mediaType = mediaType,
                title = title,
                posterImageUrl = posterImageUrl,
                backdropImageUrl = backdropImageUrl,
                voteAvg = voteAvg,
                releaseDate = releaseDate
            )
            onResult?.invoke(added)
        }
    }

    fun toggleFavorite(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        backdropImageUrl: String,
        voteAvg: Float,
        releaseDate: String,
        onResult: ((added: Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val added = libraryRepository.toggleFavorite(
                mediaId = mediaId,
                mediaType = mediaType,
                title = title,
                posterImageUrl = posterImageUrl,
                backdropImageUrl = backdropImageUrl,
                voteAvg = voteAvg,
                releaseDate = releaseDate
            )
            onResult?.invoke(added)
        }
    }

    fun toggleWatched(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        voteAvg: Float,
        onResult: ((added: Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val added = libraryRepository.toggleWatchHistory(
                mediaId = mediaId,
                mediaType = mediaType,
                title = title,
                posterImageUrl = posterImageUrl,
                voteAvg = voteAvg
            )
            onResult?.invoke(added)
        }
    }

    fun toggleMediaCustomList(
        listId: String,
        mediaId: Int,
        mediaType: MediaType,
        title: String = "",
        posterImageUrl: String = "",
        backdropImageUrl: String = "",
        voteAvg: Float = 0f,
        isCurrentlyInList: Boolean,
        onResult: ((added: Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (isCurrentlyInList) {
                libraryRepository.removeMediaFromCustomList(listId, mediaId)
                onResult?.invoke(false)
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
                onResult?.invoke(true)
            }
        }
    }
}
