package com.ssverma.showtime.ui.whatsnew

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.library.navigation.LibraryTabDestination
import com.ssverma.feature.library.navigation.TasteProfileNavKey
import com.ssverma.feature.match.navigation.MatchRoomNavKey
import com.ssverma.feature.movie.navigation.CinemaGameNavKey
import com.ssverma.shared.domain.model.feature.CinephileFeature
import com.ssverma.showtime.R
import com.ssverma.showtime.feature.filter.navigation.UniversalDiscoveryNavKey

@Immutable
data class WhatsNewFeature(
    val feature: CinephileFeature,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val icon: ImageVector,
    val destinationNavKey: NavKey
)

object WhatsNewCatalog {
    val allFeatures: List<WhatsNewFeature> = listOf(
        WhatsNewFeature(
            feature = CinephileFeature.MY_LISTS,
            titleRes = R.string.whats_new_my_lists_title,
            descriptionRes = R.string.whats_new_my_lists_desc,
            icon = Icons.AutoMirrored.Rounded.List,
            destinationNavKey = LibraryHomeNavKey(initialTab = LibraryTabDestination.CustomLists)
        ),
        WhatsNewFeature(
            feature = CinephileFeature.DISCOVERY,
            titleRes = R.string.whats_new_discover_title,
            descriptionRes = R.string.whats_new_discover_desc,
            icon = Icons.Rounded.Explore,
            destinationNavKey = UniversalDiscoveryNavKey()
        ),
        WhatsNewFeature(
            feature = CinephileFeature.COMMUNITY_LISTS,
            titleRes = R.string.whats_new_community_title,
            descriptionRes = R.string.whats_new_community_desc,
            icon = Icons.Rounded.Diversity3,
            destinationNavKey = LibraryHomeNavKey(initialTab = LibraryTabDestination.Community)
        ),
        WhatsNewFeature(
            feature = CinephileFeature.MOVIE_MATCH,
            titleRes = R.string.whats_new_match_title,
            descriptionRes = R.string.whats_new_match_desc,
            icon = Icons.Rounded.Favorite,
            destinationNavKey = MatchRoomNavKey()
        ),
        WhatsNewFeature(
            feature = CinephileFeature.TASTE_PROFILE,
            titleRes = R.string.whats_new_taste_title,
            descriptionRes = R.string.whats_new_taste_desc,
            icon = Icons.Rounded.AutoAwesome,
            destinationNavKey = TasteProfileNavKey
        ),
        WhatsNewFeature(
            feature = CinephileFeature.DAILY_GAME,
            titleRes = R.string.whats_new_game_title,
            descriptionRes = R.string.whats_new_game_desc,
            icon = Icons.Rounded.SportsEsports,
            destinationNavKey = CinemaGameNavKey
        )
    )

    fun filterActiveFeatures(filterString: String): List<WhatsNewFeature> {
        if (filterString.isBlank()) return allFeatures
        val allowedIds = filterString.split(",").map { it.trim().lowercase() }.toSet()
        val filtered = allFeatures.filter { it.feature.id.lowercase() in allowedIds }
        return filtered.ifEmpty { allFeatures }
    }
}
