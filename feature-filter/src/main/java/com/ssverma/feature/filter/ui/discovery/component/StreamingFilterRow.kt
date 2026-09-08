package com.ssverma.feature.filter.ui.discovery.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.feature.filter.R
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.ui.R as SharedUiR

@Composable
fun StreamingFilterRow(
    watchRegion: String,
    availableProviders: List<ProviderInfo>,
    selectedProviderIds: Set<Int>,
    userSubscriptions: Set<Int> = emptySet(),
    onToggleProvider: (Int) -> Unit,
    onToggleMyServices: () -> Unit = {},
    onOpenSubscriptionsSheet: () -> Unit = {},
    onOpenRegionSheet: () -> Unit,
    onOpenFilterSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        // Region Selector Chip
        item(key = "region_chip") {
            Surface(
                onClick = onOpenRegionSheet,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = watchRegion.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Advanced Filter & Sort Button
        item(key = "filter_chip") {
            Surface(
                onClick = onOpenFilterSheet,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = stringResource(R.string.filter),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.filter),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // My Services Quick Filter Chip
        item(key = "my_services_chip") {
            val isMyServicesActive = userSubscriptions.isNotEmpty() &&
                    (selectedProviderIds == userSubscriptions ||
                            (userSubscriptions.size > 1 && selectedProviderIds == userSubscriptions.take(
                                1
                            ).toSet()))

            val containerColor by animateColorAsState(
                targetValue = if (isMyServicesActive) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                label = "my_services_bg"
            )

            val borderColor by animateColorAsState(
                targetValue = if (isMyServicesActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                label = "my_services_border"
            )

            if (userSubscriptions.isEmpty()) {
                Surface(
                    onClick = onOpenSubscriptionsSheet,
                    shape = RoundedCornerShape(16.dp),
                    color = containerColor,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LiveTv,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(SharedUiR.string.streaming_subscriptions_my_services_chip),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = containerColor,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(32.dp)
                    ) {
                        // Clickable filter toggle area
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                                .clickable(onClick = onToggleMyServices)
                                .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LiveTv,
                                contentDescription = null,
                                tint = if (isMyServicesActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(
                                    SharedUiR.string.streaming_subscriptions_my_services_chip_count,
                                    userSubscriptions.size
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isMyServicesActive) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isMyServicesActive) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                            if (isMyServicesActive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Subtle vertical separator
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(14.dp)
                                .background(
                                    if (isMyServicesActive) MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.35f
                                    )
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                )
                        )

                        // Dedicated in-browse edit button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                                .clickable(onClick = onOpenSubscriptionsSheet)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(SharedUiR.string.streaming_subscriptions_edit_action),
                                tint = if (isMyServicesActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }

        // Streaming Providers Chips
        items(
            items = availableProviders,
            key = { it.providerId }
        ) { provider ->
            val isSelected = selectedProviderIds.contains(provider.providerId)

            val containerColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                label = "provider_bg"
            )

            val borderColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                label = "provider_border"
            )

            Surface(
                onClick = { onToggleProvider(provider.providerId) },
                shape = RoundedCornerShape(16.dp),
                color = containerColor,
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    if (provider.logoPath.isNotBlank()) {
                        NetworkImage(
                            url = provider.logoPath,
                            contentDescription = provider.providerName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = provider.providerName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )

                    if (isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
