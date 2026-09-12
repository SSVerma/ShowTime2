package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.shared.domain.model.game.CinemaGameStats

fun LazyListScope.cinephileQuickAccessHub(
    gameStats: CinemaGameStats,
    isTodayGameCompleted: Boolean,
    isPollVoted: Boolean,
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
    item(key = "cinephile_quick_access_hub") {
        CinephileQuickAccessHub(
            gameStats = gameStats,
            isTodayGameCompleted = isTodayGameCompleted,
            isPollVoted = isPollVoted,
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
fun CinephileQuickAccessHub(
    gameStats: CinemaGameStats,
    isTodayGameCompleted: Boolean,
    isPollVoted: Boolean,
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
    Column(modifier = modifier) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Cinephile Hub",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Two-row synchronous horizontal scrollable action rail
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: Core Navigation & Daily Habits
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1. My Lists
                    QuickAccessTile(
                        title = "My Lists",
                        subtitle = "Personal",
                        icon = Icons.AutoMirrored.Rounded.List,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = onOpenMyLists
                    )

                    // 2. Community Lists
                    QuickAccessTile(
                        title = "Community",
                        subtitle = "Curated Lists",
                        icon = Icons.Rounded.Diversity3,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        onClick = onOpenCommunityLists
                    )

                    // 3. Cinema Diary
                    QuickAccessTile(
                        title = "Cinema Diary",
                        subtitle = "Logs & Reviews",
                        icon = Icons.Rounded.Star,
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        onClick = onOpenCinemaDiary
                    )

                    // 4. Daily Game (with live streak or completion badge)
                    val gameBadgeText = when {
                        gameStats.currentStreak > 0 -> "🔥 ${gameStats.currentStreak}"
                        isTodayGameCompleted -> "✓ Done"
                        else -> "Play"
                    }
                    val gameBadgeColor = if (isTodayGameCompleted) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                    QuickAccessTile(
                        title = "Daily Game",
                        subtitle = "Challenge",
                        icon = Icons.Rounded.SportsEsports,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        badge = gameBadgeText,
                        badgeContainerColor = gameBadgeColor,
                        badgeTextColor = MaterialTheme.colorScheme.primary,
                        onClick = onOpenCinemaGame
                    )

                    // 5. Daily Poll
                    val pollBadgeText = if (isPollVoted) "✓ Voted" else "Vote"
                    QuickAccessTile(
                        title = "Daily Poll",
                        subtitle = "Cinephile Voice",
                        icon = Icons.Rounded.Poll,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                        iconTint = MaterialTheme.colorScheme.secondary,
                        badge = pollBadgeText,
                        badgeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        badgeTextColor = MaterialTheme.colorScheme.secondary,
                        onClick = onOpenDailyPoll
                    )

                    // 5b. Movie Match (Swipe Night)
                    QuickAccessTile(
                        title = "Movie Match",
                        subtitle = "Swipe Night",
                        icon = Icons.Rounded.Favorite,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        badge = "New",
                        badgeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        badgeTextColor = MaterialTheme.colorScheme.primary,
                        onClick = onOpenMovieMatch
                    )
                }

                // Row 2: Deep Exploration & Stats
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 6. Discover & Browse
                    QuickAccessTile(
                        title = "Discover",
                        subtitle = "Vibes & Mood",
                        icon = Icons.Rounded.AutoAwesome,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = onOpenDiscovery
                    )

                    // 7. Taste Profile
                    QuickAccessTile(
                        title = "Taste Profile",
                        subtitle = "Persona & Picks",
                        icon = Icons.Rounded.Bookmarks,
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        onClick = onOpenTasteProfile
                    )

                    // 8. Blindspot Challenges
                    QuickAccessTile(
                        title = "Challenges",
                        subtitle = "Blindspots",
                        icon = Icons.Rounded.EmojiEvents,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        onClick = onOpenBacklogChallenges
                    )

                    // 9. Cinema Receipt & Wrapped
                    QuickAccessTile(
                        title = "Receipt",
                        subtitle = "Wrapped Stats",
                        icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onOpenReceipt
                    )

                    // 10. Stars & People
                    QuickAccessTile(
                        title = "People",
                        subtitle = "Actors & Crew",
                        icon = Icons.Rounded.People,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onOpenPeople
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAccessTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    badgeContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    badgeTextColor: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .height(52.dp)
            .defaultMinSize(minWidth = 135.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // Icon Pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconContainerColor,
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(9.dp))

            // Text Labels & Optional Badge
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )

                    if (!badge.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = badgeContainerColor
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    maxLines = 1
                )
            }
        }
    }
}
