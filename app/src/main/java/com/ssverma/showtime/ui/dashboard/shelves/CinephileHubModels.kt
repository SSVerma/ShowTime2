package com.ssverma.showtime.ui.dashboard.shelves

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Poll
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ssverma.shared.domain.model.feature.CinephileFeature
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.showtime.R

@Immutable
sealed interface HubBadge {
    @Immutable
    data class Streak(val streak: Int) : HubBadge

    @Immutable
    data class Completed(@param:StringRes val labelRes: Int) : HubBadge

    @Immutable
    data class Action(@param:StringRes val labelRes: Int) : HubBadge

    @Immutable
    data object New : HubBadge
}

@Immutable
data class CinephileHubItem(
    val feature: CinephileFeature,
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
    val icon: ImageVector,
    val containerColor: Color,
    val iconTint: Color,
    val badge: HubBadge? = null,
    val onClick: () -> Unit,
)

enum class HubCategoryType(
    @param:StringRes val titleRes: Int,
) {
    DAILY_HABITS(R.string.hub_category_daily_habits),
    MY_CINEMA(R.string.hub_category_my_cinema),
    COMMUNITY_EXPLORE(R.string.hub_category_community_explore)
}

@Immutable
data class CinephileHubCategory(
    val type: HubCategoryType,
    val items: List<CinephileHubItem>,
)

@Immutable
data class CinephileHubData(
    val carouselItems: List<CinephileHubItem>,
    val categories: List<CinephileHubCategory>,
)

@Composable
fun rememberCinephileHubData(
    gameStats: CinemaGameStats,
    isTodayGameCompleted: Boolean,
    isPollVoted: Boolean,
    acknowledgedFeatures: Set<String>,
    onFeatureTapped: (CinephileFeature) -> Unit,
    onOpenMyLists: () -> Unit,
    onOpenCommunityLists: () -> Unit,
    onOpenCinemaDiary: () -> Unit,
    onOpenCinemaGame: () -> Unit,
    onOpenDailyPoll: () -> Unit,
    onOpenMovieMatch: () -> Unit,
    onOpenTasteProfile: () -> Unit,
    onOpenBacklogChallenges: () -> Unit,
    onOpenReceipt: () -> Unit,
    onOpenPeople: () -> Unit,
    onOpenDiscovery: () -> Unit
): CinephileHubData {
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val primary = MaterialTheme.colorScheme.primary
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surfaceContainerHighest = MaterialTheme.colorScheme.surfaceContainerHighest
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    return remember(
        gameStats,
        isTodayGameCompleted,
        isPollVoted,
        acknowledgedFeatures,
        primaryContainer,
        primary,
        secondaryContainer,
        secondary,
        tertiaryContainer,
        tertiary,
        surfaceContainerHighest,
        onSurfaceVariant
    ) {
        val isGameNew = CinephileFeature.DAILY_GAME.isNew(acknowledgedFeatures)
        val gameItem = CinephileHubItem(
            feature = CinephileFeature.DAILY_GAME,
            titleRes = R.string.hub_game_title,
            subtitleRes = R.string.hub_game_desc,
            icon = Icons.Rounded.SportsEsports,
            containerColor = primaryContainer,
            iconTint = primary,
            badge = when {
                gameStats.currentStreak > 0 -> HubBadge.Streak(streak = gameStats.currentStreak)
                isTodayGameCompleted -> HubBadge.Completed(labelRes = R.string.badge_done)
                isGameNew -> HubBadge.New
                else -> HubBadge.Action(labelRes = R.string.badge_play)
            },
            onClick = {
                onFeatureTapped(CinephileFeature.DAILY_GAME)
                onOpenCinemaGame()
            }
        )

        val isPollNew = CinephileFeature.DAILY_POLL.isNew(acknowledgedFeatures)
        val pollItem = CinephileHubItem(
            feature = CinephileFeature.DAILY_POLL,
            titleRes = R.string.hub_poll_title,
            subtitleRes = R.string.hub_poll_desc,
            icon = Icons.Rounded.Poll,
            containerColor = secondaryContainer,
            iconTint = secondary,
            badge = when {
                isPollVoted -> HubBadge.Completed(labelRes = R.string.badge_voted)
                isPollNew -> HubBadge.New
                else -> HubBadge.Action(labelRes = R.string.badge_vote)
            },
            onClick = {
                onFeatureTapped(CinephileFeature.DAILY_POLL)
                onOpenDailyPoll()
            }
        )

        val matchItem = CinephileHubItem(
            feature = CinephileFeature.MOVIE_MATCH,
            titleRes = R.string.hub_match_title,
            subtitleRes = R.string.hub_match_desc,
            icon = Icons.Rounded.Favorite,
            containerColor = primaryContainer,
            iconTint = primary,
            badge = if (CinephileFeature.MOVIE_MATCH.isNew(acknowledgedFeatures)) HubBadge.New else null,
            onClick = {
                onFeatureTapped(CinephileFeature.MOVIE_MATCH)
                onOpenMovieMatch()
            }
        )

        val diaryItem = CinephileHubItem(
            feature = CinephileFeature.CINEMA_DIARY,
            titleRes = R.string.hub_diary_title,
            subtitleRes = R.string.hub_diary_desc,
            icon = Icons.Rounded.Star,
            containerColor = tertiaryContainer,
            iconTint = tertiary,
            badge = if (CinephileFeature.CINEMA_DIARY.isNew(acknowledgedFeatures)) HubBadge.New else null,
            onClick = {
                onFeatureTapped(CinephileFeature.CINEMA_DIARY)
                onOpenCinemaDiary()
            }
        )

        val myListsItem = CinephileHubItem(
            feature = CinephileFeature.MY_LISTS,
            titleRes = R.string.hub_my_lists_title,
            subtitleRes = R.string.hub_my_lists_desc,
            icon = Icons.AutoMirrored.Rounded.List,
            containerColor = primaryContainer,
            iconTint = primary,
            badge = null,
            onClick = {
                onFeatureTapped(CinephileFeature.MY_LISTS)
                onOpenMyLists()
            }
        )

        val communityItem = CinephileHubItem(
            feature = CinephileFeature.COMMUNITY_LISTS,
            titleRes = R.string.hub_community_title,
            subtitleRes = R.string.hub_community_desc,
            icon = Icons.Rounded.Diversity3,
            containerColor = secondaryContainer,
            iconTint = secondary,
            badge = if (CinephileFeature.COMMUNITY_LISTS.isNew(acknowledgedFeatures)) HubBadge.New else null,
            onClick = {
                onFeatureTapped(CinephileFeature.COMMUNITY_LISTS)
                onOpenCommunityLists()
            }
        )

        val tasteItem = CinephileHubItem(
            feature = CinephileFeature.TASTE_PROFILE,
            titleRes = R.string.hub_taste_title,
            subtitleRes = R.string.hub_taste_desc,
            icon = Icons.Rounded.Bookmarks,
            containerColor = tertiaryContainer,
            iconTint = tertiary,
            badge = if (CinephileFeature.TASTE_PROFILE.isNew(acknowledgedFeatures)) HubBadge.New else null,
            onClick = {
                onFeatureTapped(CinephileFeature.TASTE_PROFILE)
                onOpenTasteProfile()
            }
        )

        val challengesItem = CinephileHubItem(
            feature = CinephileFeature.BACKLOG_CHALLENGES,
            titleRes = R.string.hub_challenges_title,
            subtitleRes = R.string.hub_challenges_desc,
            icon = Icons.Rounded.EmojiEvents,
            containerColor = secondaryContainer,
            iconTint = secondary,
            badge = if (CinephileFeature.BACKLOG_CHALLENGES.isNew(acknowledgedFeatures)) HubBadge.New else null,
            onClick = {
                onFeatureTapped(CinephileFeature.BACKLOG_CHALLENGES)
                onOpenBacklogChallenges()
            }
        )

        val receiptItem = CinephileHubItem(
            feature = CinephileFeature.CINEMA_RECEIPT,
            titleRes = R.string.hub_receipt_title,
            subtitleRes = R.string.hub_receipt_desc,
            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
            containerColor = surfaceContainerHighest,
            iconTint = onSurfaceVariant,
            badge = if (CinephileFeature.CINEMA_RECEIPT.isNew(acknowledgedFeatures)) HubBadge.New else null,
            onClick = {
                onFeatureTapped(CinephileFeature.CINEMA_RECEIPT)
                onOpenReceipt()
            }
        )

        val discoveryItem = CinephileHubItem(
            feature = CinephileFeature.DISCOVERY,
            titleRes = R.string.hub_discover_title,
            subtitleRes = R.string.hub_discover_desc,
            icon = Icons.Rounded.AutoAwesome,
            containerColor = primaryContainer,
            iconTint = primary,
            badge = if (CinephileFeature.DISCOVERY.isNew(acknowledgedFeatures)) HubBadge.New else null,
            onClick = {
                onFeatureTapped(CinephileFeature.DISCOVERY)
                onOpenDiscovery()
            }
        )

        val peopleItem = CinephileHubItem(
            feature = CinephileFeature.PEOPLE,
            titleRes = R.string.hub_people_title,
            subtitleRes = R.string.hub_people_desc,
            icon = Icons.Rounded.People,
            containerColor = surfaceContainerHighest,
            iconTint = onSurfaceVariant,
            badge = null,
            onClick = {
                onFeatureTapped(CinephileFeature.PEOPLE)
                onOpenPeople()
            }
        )

        // 2-row horizontal carousel ordering: pairs flow down each column
        val carouselItems = listOf(
            gameItem,
            pollItem,
            matchItem,
            diaryItem,
            myListsItem,
            communityItem,
            tasteItem,
            challengesItem,
            receiptItem,
            discoveryItem,
            peopleItem
        )

        // Categorized directory for full bottom sheet
        val categories = listOf(
            CinephileHubCategory(
                type = HubCategoryType.DAILY_HABITS,
                items = listOf(gameItem, pollItem, matchItem)
            ),
            CinephileHubCategory(
                type = HubCategoryType.MY_CINEMA,
                items = listOf(diaryItem, myListsItem, tasteItem, receiptItem, challengesItem)
            ),
            CinephileHubCategory(
                type = HubCategoryType.COMMUNITY_EXPLORE,
                items = listOf(communityItem, discoveryItem, peopleItem)
            )
        )

        CinephileHubData(
            carouselItems = carouselItems,
            categories = categories
        )
    }
}
