package com.ssverma.shared.ui.component.section

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchBrowserIntent
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.WatchProvider
import com.ssverma.shared.ui.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WatchProvidersSection(
    watchProvider: WatchProvider?,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    adSlotIndex: Int? = 2,
    adContent: (@Composable () -> Unit)? = null,
    onWatchProviderClick: (ProviderInfo) -> Unit = {},
    onJustWatchClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val hasProviders = watchProvider?.hasProviders == true

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .background(gradientBrush)
                .clickable(enabled = hasProviders && watchProvider?.link?.isNotEmpty() == true) {
                    watchProvider?.link?.let { link ->
                        if (link.isNotEmpty()) {
                            context.dispatchBrowserIntent(link)
                        }
                    }
                }
                .padding(20.dp)
        ) {
            Column {
                if (showTitle) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.where_to_watch),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(if (hasProviders) 20.dp else 12.dp))
                }

                if (watchProvider != null && hasProviders) {
                    val primaryAdCategory = when {
                        watchProvider.flatrate.isNotEmpty() -> stringResource(R.string.stream)
                        watchProvider.free.isNotEmpty() -> stringResource(R.string.free)
                        watchProvider.rent.isNotEmpty() -> stringResource(R.string.rent)
                        watchProvider.buy.isNotEmpty() -> stringResource(R.string.buy)
                        watchProvider.ads.isNotEmpty() -> stringResource(R.string.ads)
                        else -> null
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (watchProvider.flatrate.isNotEmpty()) {
                            val isAdTarget = primaryAdCategory == stringResource(R.string.stream)
                            ProviderCategoryRow(
                                categoryName = stringResource(R.string.stream),
                                providers = watchProvider.flatrate,
                                adSlotIndex = if (isAdTarget) adSlotIndex else null,
                                adContent = if (isAdTarget) adContent else null,
                                onProviderClick = onWatchProviderClick
                            )
                        }

                        if (watchProvider.free.isNotEmpty()) {
                            val isAdTarget = primaryAdCategory == stringResource(R.string.free)
                            ProviderCategoryRow(
                                categoryName = stringResource(R.string.free),
                                providers = watchProvider.free,
                                adSlotIndex = if (isAdTarget) adSlotIndex else null,
                                adContent = if (isAdTarget) adContent else null,
                                onProviderClick = onWatchProviderClick
                            )
                        }

                        if (watchProvider.rent.isNotEmpty()) {
                            val isAdTarget = primaryAdCategory == stringResource(R.string.rent)
                            ProviderCategoryRow(
                                categoryName = stringResource(R.string.rent),
                                providers = watchProvider.rent,
                                adSlotIndex = if (isAdTarget) adSlotIndex else null,
                                adContent = if (isAdTarget) adContent else null,
                                onProviderClick = onWatchProviderClick
                            )
                        }

                        if (watchProvider.buy.isNotEmpty()) {
                            val isAdTarget = primaryAdCategory == stringResource(R.string.buy)
                            ProviderCategoryRow(
                                categoryName = stringResource(R.string.buy),
                                providers = watchProvider.buy,
                                adSlotIndex = if (isAdTarget) adSlotIndex else null,
                                adContent = if (isAdTarget) adContent else null,
                                onProviderClick = onWatchProviderClick
                            )
                        }

                        if (watchProvider.ads.isNotEmpty()) {
                            val isAdTarget = primaryAdCategory == stringResource(R.string.ads)
                            ProviderCategoryRow(
                                categoryName = stringResource(R.string.ads),
                                providers = watchProvider.ads,
                                adSlotIndex = if (isAdTarget) adSlotIndex else null,
                                adContent = if (isAdTarget) adContent else null,
                                onProviderClick = onWatchProviderClick
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onJustWatchClick()
                                context.dispatchBrowserIntent("https://www.justwatch.com")
                            },
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.provided_by),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Image(
                            painter = painterResource(id = R.drawable.justwatch),
                            contentDescription = "JustWatch",
                            modifier = Modifier
                                .height(14.dp)
                                .width(70.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.not_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProviderCategoryRow(
    categoryName: String,
    providers: List<ProviderInfo>,
    modifier: Modifier = Modifier,
    adSlotIndex: Int? = null,
    adContent: (@Composable () -> Unit)? = null,
    onProviderClick: (ProviderInfo) -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.width(72.dp)
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            providers.forEachIndexed { index, provider ->
                if (adSlotIndex != null && index == adSlotIndex && adContent != null) {
                    adContent()
                }

                com.ssverma.shared.ui.component.WatchProviderLogo(
                    provider = provider,
                    onClick = { onProviderClick(provider) },
                    size = 44.dp,
                    modifier = Modifier
                        .graphicsLayer {
                            shadowElevation = 2.dp.toPx()
                            shape = androidx.compose.foundation.shape.CircleShape
                            clip = true
                        }
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
            }

            if (adSlotIndex != null && providers.size <= adSlotIndex && adContent != null && providers.isNotEmpty()) {
                adContent()
            }
        }
    }
}

