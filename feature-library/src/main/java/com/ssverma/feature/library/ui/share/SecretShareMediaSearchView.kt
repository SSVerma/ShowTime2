package com.ssverma.feature.library.ui.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.feature.library.ui.common.ShowTimeMediaSearchPicker
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.library.SecretSharedListItem

@Composable
fun SecretShareMediaSearchView(
    searchQuery: String,
    selectedFilter: ChallengeMediaTypeFilter,
    suggestions: List<SecretSharedListItem>,
    existingMediaIds: Set<Int>,
    isSearching: Boolean,
    onSearchQueryChange: (String, ChallengeMediaTypeFilter) -> Unit,
    onClearSearch: () -> Unit,
    onMediaSelected: (SecretSharedListItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    currentUserName: String = "Friend",
    isGoogleUser: Boolean = false,
    onUpdateUserName: (String) -> Unit = {}
) {
    ShowTimeMediaSearchPicker(
        searchQuery = searchQuery,
        selectedFilter = selectedFilter,
        suggestions = suggestions,
        isSearching = isSearching,
        onSearchQueryChange = onSearchQueryChange,
        onClearSearch = onClearSearch,
        onMediaSelected = onMediaSelected,
        onDismiss = onDismiss,
        modifier = modifier,
        isMediaSelectedAlready = { existingMediaIds.contains(it.mediaId) },
        contributorDisplayName = currentUserName,
        isGoogleUser = isGoogleUser,
        itemKey = { index, item -> "${item.mediaType}_${item.mediaId}_$index" },
        itemTitle = { it.title },
        itemPosterUrl = { it.posterImageUrl },
        itemMediaType = { it.mediaType },
        itemVoteAvg = { it.voteAvg },
        itemReleaseYear = { it.releaseYear },
        itemOverview = { it.overview }
    )
}
