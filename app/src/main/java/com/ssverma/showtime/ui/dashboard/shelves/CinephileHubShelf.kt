package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Poll
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.feature.CinephileFeature
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.showtime.R

fun LazyListScope.cinephileHubShelf(
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
    onOpenDiscovery: () -> Unit,
    modifier: Modifier = Modifier
) {
    item(key = "cinephile_hub_shelf", contentType = "cinephile_hub_shelf") {
        CinephileHubShelf(
            gameStats = gameStats,
            isTodayGameCompleted = isTodayGameCompleted,
            isPollVoted = isPollVoted,
            acknowledgedFeatures = acknowledgedFeatures,
            onFeatureTapped = onFeatureTapped,
            onOpenMyLists = onOpenMyLists,
            onOpenCommunityLists = onOpenCommunityLists,
            onOpenCinemaDiary = onOpenCinemaDiary,
            onOpenCinemaGame = onOpenCinemaGame,
            onOpenDailyPoll = onOpenDailyPoll,
            onOpenMovieMatch = onOpenMovieMatch,
            onOpenTasteProfile = onOpenTasteProfile,
            onOpenBacklogChallenges = onOpenBacklogChallenges,
            onOpenReceipt = onOpenReceipt,
            onOpenPeople = onOpenPeople,
            onOpenDiscovery = onOpenDiscovery,
            modifier = modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CinephileHubShelf(
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
    onOpenDiscovery: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.cinephile_hub_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // 4 Primary Habit Tiles (2x2 Grid) - Unified styling preserving vertical space
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            // Row 1: My Lists & Community Lists
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                HubTile(
                    title = stringResource(R.string.hub_my_lists_title),
                    subtitle = stringResource(R.string.hub_my_lists_desc),
                    icon = Icons.AutoMirrored.Rounded.List,
                    iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = {
                        onFeatureTapped(CinephileFeature.MY_LISTS)
                        onOpenMyLists()
                    },
                    modifier = Modifier.weight(1f)
                )

                HubTile(
                    title = stringResource(R.string.hub_community_title),
                    subtitle = stringResource(R.string.hub_community_desc),
                    icon = Icons.Rounded.Diversity3,
                    iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    badge = if (CinephileFeature.COMMUNITY_LISTS.isNew(acknowledgedFeatures)) {
                        { NewBadge() }
                    } else null,
                    onClick = {
                        onFeatureTapped(CinephileFeature.COMMUNITY_LISTS)
                        onOpenCommunityLists()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Cinema Diary & Daily Game
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                HubTile(
                    title = stringResource(R.string.hub_diary_title),
                    subtitle = stringResource(R.string.hub_diary_desc),
                    icon = Icons.Rounded.Star,
                    iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    badge = if (CinephileFeature.CINEMA_DIARY.isNew(acknowledgedFeatures)) {
                        { NewBadge() }
                    } else null,
                    onClick = {
                        onFeatureTapped(CinephileFeature.CINEMA_DIARY)
                        onOpenCinemaDiary()
                    },
                    modifier = Modifier.weight(1f)
                )

                val isGameNew = CinephileFeature.DAILY_GAME.isNew(acknowledgedFeatures)
                HubTile(
                    title = stringResource(R.string.hub_game_title),
                    subtitle = stringResource(R.string.hub_game_desc),
                    icon = Icons.Rounded.SportsEsports,
                    iconContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    badge = {
                        when {
                            gameStats.currentStreak > 0 -> StreakBadge(streak = gameStats.currentStreak)
                            isTodayGameCompleted -> CompletedBadge(label = stringResource(R.string.badge_done))
                            isGameNew -> NewBadge()
                            else -> ActionBadge(label = stringResource(R.string.badge_play))
                        }
                    },
                    onClick = {
                        onFeatureTapped(CinephileFeature.DAILY_GAME)
                        onOpenCinemaGame()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // In-Feed Animated Expansion for Remaining 7 Tools
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(250)) + expandVertically(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(200))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .padding(top = MaterialTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                // Expanded Row 1: Daily Poll & Movie Match
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    val isPollNew = CinephileFeature.DAILY_POLL.isNew(acknowledgedFeatures)
                    HubTile(
                        title = stringResource(R.string.hub_poll_title),
                        subtitle = stringResource(R.string.hub_poll_desc),
                        icon = Icons.Rounded.Poll,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                        iconTint = MaterialTheme.colorScheme.secondary,
                        badge = {
                            when {
                                isPollVoted -> CompletedBadge(label = stringResource(R.string.badge_voted))
                                isPollNew -> NewBadge()
                                else -> ActionBadge(label = stringResource(R.string.badge_vote))
                            }
                        },
                        onClick = {
                            onFeatureTapped(CinephileFeature.DAILY_POLL)
                            onOpenDailyPoll()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    HubTile(
                        title = stringResource(R.string.hub_match_title),
                        subtitle = stringResource(R.string.hub_match_desc),
                        icon = Icons.Rounded.Favorite,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        badge = if (CinephileFeature.MOVIE_MATCH.isNew(acknowledgedFeatures)) {
                            { NewBadge() }
                        } else null,
                        onClick = {
                            onFeatureTapped(CinephileFeature.MOVIE_MATCH)
                            onOpenMovieMatch()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Expanded Row 2: Taste Profile & Challenges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    HubTile(
                        title = stringResource(R.string.hub_taste_title),
                        subtitle = stringResource(R.string.hub_taste_desc),
                        icon = Icons.Rounded.Bookmarks,
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        badge = if (CinephileFeature.TASTE_PROFILE.isNew(acknowledgedFeatures)) {
                            { NewBadge() }
                        } else null,
                        onClick = {
                            onFeatureTapped(CinephileFeature.TASTE_PROFILE)
                            onOpenTasteProfile()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    HubTile(
                        title = stringResource(R.string.hub_challenges_title),
                        subtitle = stringResource(R.string.hub_challenges_desc),
                        icon = Icons.Rounded.EmojiEvents,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        badge = if (CinephileFeature.BACKLOG_CHALLENGES.isNew(acknowledgedFeatures)) {
                            { NewBadge() }
                        } else null,
                        onClick = {
                            onFeatureTapped(CinephileFeature.BACKLOG_CHALLENGES)
                            onOpenBacklogChallenges()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Expanded Row 3: Cinema Receipt & Discover
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    HubTile(
                        title = stringResource(R.string.hub_receipt_title),
                        subtitle = stringResource(R.string.hub_receipt_desc),
                        icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        badge = if (CinephileFeature.CINEMA_RECEIPT.isNew(acknowledgedFeatures)) {
                            { NewBadge() }
                        } else null,
                        onClick = {
                            onFeatureTapped(CinephileFeature.CINEMA_RECEIPT)
                            onOpenReceipt()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    HubTile(
                        title = stringResource(R.string.hub_discover_title),
                        subtitle = stringResource(R.string.hub_discover_desc),
                        icon = Icons.Rounded.AutoAwesome,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        badge = if (CinephileFeature.DISCOVERY.isNew(acknowledgedFeatures)) {
                            { NewBadge() }
                        } else null,
                        onClick = {
                            onFeatureTapped(CinephileFeature.DISCOVERY)
                            onOpenDiscovery()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Expanded Row 4: People (Actors & Crew) - balanced with Spacer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    HubTile(
                        title = stringResource(R.string.hub_people_title),
                        subtitle = stringResource(R.string.hub_people_desc),
                        icon = Icons.Rounded.People,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = {
                            onFeatureTapped(CinephileFeature.PEOPLE)
                            onOpenPeople()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Toggle Expansion Button
        Surface(
            onClick = { isExpanded = !isExpanded },
            shape = RoundedCornerShape(MaterialTheme.spacing.smallMedium),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
            ) {
                Text(
                    text = if (isExpanded) {
                        stringResource(R.string.hub_show_less_tools)
                    } else {
                        stringResource(R.string.hub_show_all_tools, 7)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Rounded.KeyboardArrowUp
                    } else {
                        Icons.Rounded.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun HubTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier.height(52.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // Icon Container
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconContainerColor,
                modifier = Modifier.size(34.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

            // Text Labels & Optional Badge
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (badge != null) {
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        badge()
                    }
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NewBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.badge_new),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun StreakBadge(streak: Int, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = stringResource(R.string.cd_streak_flame),
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = stringResource(R.string.streak_badge, streak),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun CompletedBadge(label: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(R.string.cd_completed_check),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun ActionBadge(label: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}
