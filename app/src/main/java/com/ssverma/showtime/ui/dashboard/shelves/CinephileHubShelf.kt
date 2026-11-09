package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFireDepartment
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.feature.CinephileFeature
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.showtime.R
import com.ssverma.core.ui.R as CoreUiR

private object CinephileHubDefaults {
    val GridHeight = 126.dp
    val TileWidth = 188.dp
    val TileCornerRadius = 14.dp
    val IconContainerRadius = 10.dp
    val IconContainerSize = 34.dp
    val IconSize = 18.dp
}

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
    modifier: Modifier = Modifier,
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
            modifier = modifier.fillMaxWidth(),
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
    modifier: Modifier = Modifier,
) {
    var showHubSheet by rememberSaveable { mutableStateOf(value = false) }

    val hubData = rememberCinephileHubData(
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
    )

    Column(modifier = modifier) {
        // Section Header matching TV & Movie home page style
        SectionHeader(
            title = stringResource(R.string.cinephile_hub_title),
            onTrailingActionClicked = { showHubSheet = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium),
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Fixed-height 2-row horizontal staggered grid (zero vertical layout shift)
        LazyHorizontalStaggeredGrid(
            rows = StaggeredGridCells.Fixed(2),
            horizontalItemSpacing = MaterialTheme.spacing.small,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
            modifier = Modifier
                .fillMaxWidth()
                .height(CinephileHubDefaults.GridHeight),
        ) {
            items(
                items = hubData.carouselItems,
                key = { it.feature.id },
                contentType = { "cinephile_hub_tile" }
            ) { item ->
                HubTile(
                    item = item,
                    modifier = Modifier.width(CinephileHubDefaults.TileWidth)
                )
            }

            item(
                key = "cinephile_hub_see_all_tile",
                contentType = "see_all_tile"
            ) {
                SeeAllHubTile(
                    onClick = { showHubSheet = true },
                    modifier = Modifier.width(CinephileHubDefaults.TileWidth)
                )
            }
        }
    }

    if (showHubSheet) {
        CinephileHubBottomSheet(
            hubCategories = hubData.categories,
            onDismiss = { showHubSheet = false }
        )
    }
}

@Composable
fun SeeAllHubTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(CinephileHubDefaults.TileCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        ),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(CinephileHubDefaults.IconContainerSize),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = stringResource(CoreUiR.string.see_all),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(CinephileHubDefaults.IconSize),
                    )
                }
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(CoreUiR.string.see_all),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = stringResource(R.string.hub_sheet_subtitle),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun HubTile(
    item: CinephileHubItem,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = item.onClick,
        shape = RoundedCornerShape(CinephileHubDefaults.TileCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // Icon Container
            Surface(
                shape = RoundedCornerShape(CinephileHubDefaults.IconContainerRadius),
                color = item.containerColor,
                modifier = Modifier.size(CinephileHubDefaults.IconContainerSize)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.iconTint,
                        modifier = Modifier.size(CinephileHubDefaults.IconSize)
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
                        text = stringResource(item.titleRes),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (item.badge != null) {
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        HubBadgeView(badge = item.badge)
                    }
                }

                Text(
                    text = stringResource(item.subtitleRes),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun HubBadgeView(
    badge: HubBadge,
    modifier: Modifier = Modifier
) {
    when (badge) {
        is HubBadge.Streak -> StreakBadge(streak = badge.streak, modifier = modifier)
        is HubBadge.Completed -> CompletedBadge(
            label = stringResource(badge.labelRes),
            modifier = modifier
        )

        is HubBadge.Action -> ActionBadge(
            label = stringResource(badge.labelRes),
            modifier = modifier
        )

        is HubBadge.New -> NewBadge(modifier = modifier)
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
            color = MaterialTheme.colorScheme.onPrimaryContainer,
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
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}
