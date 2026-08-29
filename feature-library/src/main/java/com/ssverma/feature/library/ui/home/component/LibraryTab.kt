package com.ssverma.feature.library.ui.home.component

import androidx.compose.ui.graphics.vector.ImageVector
import com.ssverma.core.ui.UiText
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.model.library.SavedMediaItem

enum class MediaTypeFilter {
    ALL,
    MOVIE,
    TV
}

data class LibraryTab(
    val title: UiText,
    val icon: ImageVector,
    val tabType: LibraryTabType
) {
    val itemCount: Int
        get() = when (tabType) {
            is LibraryTabType.Watchlist -> tabType.items.size
            is LibraryTabType.Favorites -> tabType.items.size
            is LibraryTabType.History -> tabType.items.size
            is LibraryTabType.CustomLists -> tabType.lists.size
            is LibraryTabType.Community -> tabType.count
        }
}

sealed interface LibraryTabType {
    data class Watchlist(
        val items: List<SavedMediaItem>
    ) : LibraryTabType

    data class Favorites(
        val items: List<SavedMediaItem>
    ) : LibraryTabType

    data class History(
        val items: List<SavedMediaItem>
    ) : LibraryTabType

    data class CustomLists(
        val lists: List<CustomList>
    ) : LibraryTabType

    data class Community(
        val count: Int = 0
    ) : LibraryTabType
}