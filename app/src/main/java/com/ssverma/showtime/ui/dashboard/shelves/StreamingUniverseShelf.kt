package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.UiState
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.ui.component.WatchProviderHubSection
import com.ssverma.showtime.R

fun LazyListScope.streamingUniverseShelf(
    movieProviders: UiState<List<ProviderInfo>, Failure.CoreFailure>,
    tvProviders: UiState<List<ProviderInfo>, Failure.CoreFailure>,
    isMovieSelected: Boolean,
    onToggleStreamingType: (Boolean) -> Unit,
    onProviderClick: (ProviderInfo, Boolean) -> Unit,
    onRetry: () -> Unit
) {
    item(key = "streaming_universe_shelf") {
        val activeProviders = if (isMovieSelected) movieProviders else tvProviders
        WatchProviderHubSection(
            providersUiState = activeProviders,
            onProviderClick = { provider ->
                onProviderClick(provider, isMovieSelected)
            },
            onRetry = onRetry,
            isMovie = isMovieSelected,
            source = "dashboard",
            headerTrailingContent = {
                StreamingSegmentedSwitcher(
                    isMovieSelected = isMovieSelected,
                    onToggle = onToggleStreamingType
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
        )
    }
}

@Composable
private fun StreamingSegmentedSwitcher(
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
            SegmentItem(
                title = stringResource(id = R.string.movie_streaming),
                selected = isMovieSelected,
                onClick = { onToggle(true) }
            )

            SegmentItem(
                title = stringResource(id = R.string.tv_streaming),
                selected = !isMovieSelected,
                onClick = { onToggle(false) }
            )
        }
    }
}

@Composable
private fun SegmentItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "segment_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "segment_text"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
