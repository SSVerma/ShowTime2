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
