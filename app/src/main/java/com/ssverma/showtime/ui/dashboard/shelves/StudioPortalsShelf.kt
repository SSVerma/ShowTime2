package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.Theaters
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.showtime.R

data class StudioPortalItem(
    val id: Int,
    val name: String,
    val isNetwork: Boolean = false,
    val icon: ImageVector
)

val CuratedStudioPortals = listOf(
    StudioPortalItem(
        id = 41077,
        name = "A24",
        isNetwork = false,
        icon = Icons.Rounded.MovieFilter
    ),
    StudioPortalItem(
        id = 49,
        name = "HBO",
        isNetwork = true,
        icon = Icons.Rounded.Tv
    ),
    StudioPortalItem(
        id = 10342,
        name = "Ghibli",
        isNetwork = false,
        icon = Icons.Rounded.AutoAwesome
    ),
    StudioPortalItem(
        id = 3,
        name = "Pixar",
        isNetwork = false,
        icon = Icons.Rounded.Stars
    ),
    StudioPortalItem(
        id = 420,
        name = "Marvel",
        isNetwork = false,
        icon = Icons.Rounded.Bolt
    ),
    StudioPortalItem(
        id = 2552,
        name = "Apple TV+",
        isNetwork = true,
        icon = Icons.Rounded.PlayCircle
    ),
    StudioPortalItem(
        id = 174,
        name = "WB",
        isNetwork = false,
        icon = Icons.Rounded.Shield
    ),
    StudioPortalItem(
        id = 33,
        name = "Universal",
        isNetwork = false,
        icon = Icons.Rounded.Theaters
    ),
    StudioPortalItem(
        id = 4330,
        name = "Paramount",
        isNetwork = true,
        icon = Icons.Rounded.Tv
    ),
    StudioPortalItem(
        id = 2,
        name = "Disney",
        isNetwork = false,
        icon = Icons.Rounded.AutoAwesome
    ),
    StudioPortalItem(
        id = 34,
        name = "Sony",
        isNetwork = false,
        icon = Icons.Rounded.Theaters
    ),
    StudioPortalItem(
        id = 213,
        name = "Netflix",
        isNetwork = true,
        icon = Icons.Rounded.Tv
    )
)

@OptIn(ExperimentalLayoutApi::class)
fun LazyListScope.studioPortalsShelf(
    onPortalClick: (StudioPortalItem) -> Unit
) {
    item(key = "studio_portals_shelf") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // Standard Section Header
            SectionHeader(
                title = stringResource(id = R.string.studio_portals),
                titleTextStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                hideTrailingAction = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                maxItemsInEachRow = 3,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                CuratedStudioPortals.forEach { portal ->
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
    ElevatedAssistChip(
        onClick = onClick,
        label = {
            Text(
                text = portal.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = portal.icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        shape = RoundedCornerShape(8.dp),
        colors = AssistChipDefaults.elevatedAssistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            labelColor = MaterialTheme.colorScheme.onSurface,
            leadingIconContentColor = MaterialTheme.colorScheme.primary
        ),
        elevation = AssistChipDefaults.elevatedAssistChipElevation(
            elevation = 1.dp,
            pressedElevation = 2.dp
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            borderWidth = 1.dp
        ),
        modifier = modifier
    )
}
