package com.ssverma.showtime.ui.dashboard.shelves

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

val CuratedStudioPortals = listOf(
    StudioPortalItem(
        id = 41077,
        nameRes = R.string.studio_a24,
        isNetwork = false,
        icon = Icons.Rounded.MovieFilter
    ),
    StudioPortalItem(
        id = 49,
        nameRes = R.string.studio_hbo,
        isNetwork = true,
        icon = Icons.Rounded.Tv
    ),
    StudioPortalItem(
        id = 10342,
        nameRes = R.string.studio_ghibli,
        isNetwork = false,
        icon = Icons.Rounded.AutoAwesome
    ),
    StudioPortalItem(
        id = 3,
        nameRes = R.string.studio_pixar,
        isNetwork = false,
        icon = Icons.Rounded.Stars
    ),
    StudioPortalItem(
        id = 420,
        nameRes = R.string.studio_marvel,
        isNetwork = false,
        icon = Icons.Rounded.Bolt
    ),
    StudioPortalItem(
        id = 2552,
        nameRes = R.string.studio_apple_tv,
        isNetwork = true,
        icon = Icons.Rounded.PlayCircle
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
        id = 4330,
        nameRes = R.string.studio_paramount,
        isNetwork = true,
        icon = Icons.Rounded.Tv
    ),
    StudioPortalItem(
        id = 2,
        nameRes = R.string.studio_disney,
        isNetwork = false,
        icon = Icons.Rounded.AutoAwesome
    ),
    StudioPortalItem(
        id = 34,
        nameRes = R.string.studio_sony,
        isNetwork = false,
        icon = Icons.Rounded.Theaters
    ),
    StudioPortalItem(
        id = 213,
        nameRes = R.string.studio_netflix,
        isNetwork = true,
        icon = Icons.Rounded.Tv
    )
)

fun LazyListScope.studioPortalsShelf(
    onPortalClick: (StudioPortalItem) -> Unit
) {
    item(key = "studio_portals_shelf") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            // Standard Section Header
            SectionHeader(
                title = stringResource(id = R.string.studio_portals),
                leadingIcon = Icons.Rounded.Theaters,
                leadingIconContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                leadingIconTint = MaterialTheme.colorScheme.secondary,
                titleTextStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                hideTrailingAction = true
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
                items(CuratedStudioPortals, key = { it.id }) { portal ->
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
