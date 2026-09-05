package com.ssverma.feature.library.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

interface LibraryNavArgs {
    val initialTab: LibraryTabDestination
    val initialMediaType: String?
    val targetCustomListId: String?
    val openCreateCustomList: Boolean
    val attachMediaId: Int?
    val attachMediaType: String?
    val attachMediaTitle: String?
    val attachMediaPosterUrl: String?
}

@Serializable
@Parcelize
data class LibraryHomeNavKey(
    override val initialTab: LibraryTabDestination = LibraryTabDestination.Watchlist,
    override val initialMediaType: String? = null,
    override val targetCustomListId: String? = null,
    override val openCreateCustomList: Boolean = false,
    override val attachMediaId: Int? = null,
    override val attachMediaType: String? = null,
    override val attachMediaTitle: String? = null,
    override val attachMediaPosterUrl: String? = null
) : NavKey, Parcelable, LibraryNavArgs {
    companion object {
        val Default = LibraryHomeNavKey()
    }
}

@Serializable
@Parcelize
data class StandaloneLibraryNavKey(
    override val initialTab: LibraryTabDestination = LibraryTabDestination.Watchlist,
    override val initialMediaType: String? = null,
    override val targetCustomListId: String? = null,
    override val openCreateCustomList: Boolean = false,
    override val attachMediaId: Int? = null,
    override val attachMediaType: String? = null,
    override val attachMediaTitle: String? = null,
    override val attachMediaPosterUrl: String? = null
) : NavKey, Parcelable, LibraryNavArgs {
    companion object {
        val Default = StandaloneLibraryNavKey()
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

@Serializable
@Parcelize
data class ChallengeDetailNavKey(val challengeId: String) : NavKey, Parcelable

