package com.ssverma.showtime.ui.dashboard.shelves

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.Theaters
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.showtime.R

object StudioPortalDefaults {
    val GridHeight: Dp = 88.dp
    val IconSize: Dp = 18.dp
    val ChipBorderWidth: Dp = 1.dp
    const val BorderAlpha: Float = 0.5f
}

data class StudioPortalItem(
    val id: Int,
    @param:StringRes val nameRes: Int,
    val isNetwork: Boolean = false,
    val icon: ImageVector
)

val CuratedMovieStudios = listOf(
    StudioPortalItem(
        id = 41077,
        nameRes = R.string.studio_a24,
        isNetwork = false,
        icon = Icons.Rounded.MovieFilter
    ),
    StudioPortalItem(
        id = 420,
        nameRes = R.string.studio_marvel,
        isNetwork = false,
        icon = Icons.Rounded.Bolt
    ),
    StudioPortalItem(
        id = 3,
        nameRes = R.string.studio_pixar,
        isNetwork = false,
        icon = Icons.Rounded.Stars
    ),
    StudioPortalItem(
        id = 10342,
        nameRes = R.string.studio_ghibli,
        isNetwork = false,
        icon = Icons.Rounded.AutoAwesome
    ),
    StudioPortalItem(
        id = 174,
        nameRes = R.string.studio_wb,
        isNetwork = false,
        icon = Icons.Rounded.Shield
    ),
    StudioPortalItem(
        id = 33,
        nameRes = R.string.studio_universal,
        isNetwork = false,
        icon = Icons.Rounded.Public
    ),
    StudioPortalItem(
        id = 2,
        nameRes = R.string.studio_disney,
        isNetwork = false,
        icon = Icons.Rounded.AutoAwesome
    ),
    StudioPortalItem(
        id = 5,
        nameRes = R.string.studio_sony,
        isNetwork = false,
        icon = Icons.Rounded.Theaters
    ),
    StudioPortalItem(
        id = 4,
        nameRes = R.string.studio_paramount,
        isNetwork = false,
        icon = Icons.Rounded.Theaters
    ),
    StudioPortalItem(
        id = 3172,
        nameRes = R.string.studio_blumhouse,
        isNetwork = false,
        icon = Icons.Rounded.MovieFilter
    ),
    StudioPortalItem(
        id = 93243,
        nameRes = R.string.studio_neon,
        isNetwork = false,
        icon = Icons.Rounded.AutoAwesome
    ),
    StudioPortalItem(
        id = 1,
        nameRes = R.string.studio_lucasfilm,
        isNetwork = false,
        icon = Icons.Rounded.Bolt
    )
)

val CuratedTvNetworks = listOf(
    StudioPortalItem(
        id = 49,
        nameRes = R.string.studio_hbo,
        isNetwork = true,
        icon = Icons.Rounded.Tv
    ),
    StudioPortalItem(
        id = 213,
        nameRes = R.string.studio_netflix,
        isNetwork = true,
        icon = Icons.Rounded.Tv
    ),
    StudioPortalItem(
        id = 2552,
        nameRes = R.string.studio_apple_tv,
        isNetwork = true,
        icon = Icons.Rounded.PlayCircle
    ),
    StudioPortalItem(
        id = 88,
        nameRes = R.string.network_fx,
        isNetwork = true,
        icon = Icons.Rounded.Tv
    ),
    StudioPortalItem(
        id = 174,
        nameRes = R.string.network_amc,
        isNetwork = true,
        icon = Icons.Rounded.Tv
    ),
    StudioPortalItem(
        id = 4,
        nameRes = R.string.network_bbc,
        isNetwork = true,
        icon = Icons.Rounded.Public
    ),
    StudioPortalItem(
        id = 67,
        nameRes = R.string.network_showtime,
        isNetwork = true,
        icon = Icons.Rounded.Tv
    ),
    StudioPortalItem(
        id = 453,
        nameRes = R.string.network_hulu,
        isNetwork = true,
        icon = Icons.Rounded.PlayCircle
    ),
    StudioPortalItem(
        id = 2739,
        nameRes = R.string.network_disney_plus,
        isNetwork = true,
        icon = Icons.Rounded.AutoAwesome
    ),
    StudioPortalItem(
        id = 4330,
        nameRes = R.string.studio_paramount,
        isNetwork = true,
        icon = Icons.Rounded.Tv
    ),
    StudioPortalItem(
        id = 1024,
        nameRes = R.string.network_amazon,
        isNetwork = true,
        icon = Icons.Rounded.PlayCircle
    ),
    StudioPortalItem(
        id = 71,
        nameRes = R.string.network_cw,
        isNetwork = true,
        icon = Icons.Rounded.Tv
    )
)

fun LazyListScope.studioPortalsShelf(
    isMovieSelected: Boolean,
    onToggleStudioType: (Boolean) -> Unit,
    onPortalClick: (StudioPortalItem) -> Unit
) {
    item(key = "studio_portals_shelf") {
        val portals = if (isMovieSelected) CuratedMovieStudios else CuratedTvNetworks
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            // Standard Section Header with Segmented Switcher
            SectionHeader(
                title = stringResource(
                    id = if (isMovieSelected) R.string.movie_studios else R.string.tv_networks
                ),
                leadingIcon = if (isMovieSelected) Icons.Rounded.Theaters else Icons.Rounded.Tv,
                leadingIconContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                leadingIconTint = MaterialTheme.colorScheme.secondary,
                titleTextStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                trailingContent = {
                    StudioSegmentedSwitcher(
                        isMovieSelected = isMovieSelected,
                        onToggle = onToggleStudioType
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyHorizontalStaggeredGrid(
                rows = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
                horizontalItemSpacing = MaterialTheme.spacing.small,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(StudioPortalDefaults.GridHeight)
            ) {
                items(portals, key = { "${it.isNetwork}_${it.id}" }) { portal ->
                    StudioPortalChip(
                        portal = portal,
                        onClick = { onPortalClick(portal) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioSegmentedSwitcher(
    isMovieSelected: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = CircleShape
            )
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Movies Option
            val movieBg by animateColorAsState(
                targetValue = if (isMovieSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = tween(200),
                label = "studio_movie_bg"
            )
            val movieText by animateColorAsState(
                targetValue = if (isMovieSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200),
                label = "studio_movie_text"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(movieBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onToggle(true) }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.movies),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = if (isMovieSelected) FontWeight.Bold else FontWeight.Medium,
                    color = movieText
                )
            }

            // TV Shows Option
            val tvBg by animateColorAsState(
                targetValue = if (!isMovieSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = tween(200),
                label = "studio_tv_bg"
            )
            val tvText by animateColorAsState(
                targetValue = if (!isMovieSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200),
                label = "studio_tv_text"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(tvBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onToggle(false) }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.tv_streaming),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = if (!isMovieSelected) FontWeight.Bold else FontWeight.Medium,
                    color = tvText
                )
            }
        }
    }
}

@Composable
private fun StudioPortalChip(
    portal: StudioPortalItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = stringResource(id = portal.nameRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = portal.icon,
                contentDescription = null,
                modifier = Modifier.size(StudioPortalDefaults.IconSize),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        shape = MaterialTheme.shapes.small,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            labelColor = MaterialTheme.colorScheme.onSurface,
            leadingIconContentColor = MaterialTheme.colorScheme.primary
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = StudioPortalDefaults.BorderAlpha),
            borderWidth = StudioPortalDefaults.ChipBorderWidth
        ),
        modifier = modifier
    )
}
