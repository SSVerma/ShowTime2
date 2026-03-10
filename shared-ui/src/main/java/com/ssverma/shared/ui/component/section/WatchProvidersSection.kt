package com.ssverma.shared.ui.component.section

import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchBrowserIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.ssverma.core.image.NetworkImage
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.WatchProvider
import com.ssverma.shared.ui.R

@Composable
fun WatchProvidersSection(
    watchProvider: WatchProvider?,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true
) {
    val context = LocalContext.current
    val hasProviders = watchProvider?.hasProviders == true

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .clickable(enabled = hasProviders && watchProvider?.link?.isNotEmpty() == true) {
                watchProvider?.link?.let { link ->
                    if (link.isNotEmpty()) {
                        context.dispatchBrowserIntent(link)
                    }
                }
            }
            .padding(16.dp)
    ) {
        if (showTitle) {
            Text(
                text = stringResource(R.string.where_to_watch),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(if (hasProviders) 16.dp else 8.dp))
        }

        if (watchProvider != null && hasProviders) {
            if (watchProvider.free.isNotEmpty()) {
                ProviderCategoryRow(
                    categoryName = stringResource(R.string.free),
                    providers = watchProvider.free
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (watchProvider.flatrate.isNotEmpty()) {
                ProviderCategoryRow(
                    categoryName = stringResource(R.string.stream),
                    providers = watchProvider.flatrate
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (watchProvider.rent.isNotEmpty()) {
                ProviderCategoryRow(
                    categoryName = stringResource(R.string.rent),
                    providers = watchProvider.rent
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (watchProvider.buy.isNotEmpty()) {
                ProviderCategoryRow(
                    categoryName = stringResource(R.string.buy),
                    providers = watchProvider.buy
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (watchProvider.ads.isNotEmpty()) {
                ProviderCategoryRow(
                    categoryName = stringResource(R.string.ads),
                    providers = watchProvider.ads
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.dispatchBrowserIntent("https://www.justwatch.com")
                    },
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.provided_by_justwatch),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun ProviderCategoryRow(
    categoryName: String,
    providers: List<ProviderInfo>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = categoryName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            providers.forEach { provider ->
                NetworkImage(
                    url = provider.logoPath,
                    contentDescription = provider.providerName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            }
        }
    }
}
