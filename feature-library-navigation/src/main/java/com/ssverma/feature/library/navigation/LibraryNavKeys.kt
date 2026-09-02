package com.ssverma.feature.library.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class LibraryHomeNavKey(
    val initialTab: LibraryTabDestination = LibraryTabDestination.Watchlist,
    val initialMediaType: String? = null,
    val targetCustomListId: String? = null
) : NavKey, Parcelable {
    companion object {
        val Default = LibraryHomeNavKey()
    }
}

@Serializable
enum class LibraryTabDestination {
    Watchlist,
    Favorites,
    History,
    CustomLists,
    Community
}

@Serializable
@Parcelize
data object CinemaReceiptNavKey : NavKey, Parcelable

@Serializable
@Parcelize
data object CinemaDiaryNavKey : NavKey, Parcelable

@Serializable
@Parcelize
data object TasteProfileNavKey : NavKey, Parcelable

@Serializable
@Parcelize
data object CinephileWrappedNavKey : NavKey, Parcelable

@Serializable
@Parcelize
data object BacklogChallengeNavKey : NavKey, Parcelable

