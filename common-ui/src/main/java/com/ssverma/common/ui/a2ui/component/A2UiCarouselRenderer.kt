package com.ssverma.common.ui.a2ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ai.a2ui.model.A2UiAction
import com.ssverma.shared.ai.a2ui.model.A2UiComponent

/**
 * Renders a horizontal carousel of media cards emitted by the AI Agent.
 */
@Composable
fun A2UiCarouselRenderer(
    carousel: A2UiComponent.MediaCarousel,
    onAction: (A2UiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = MaterialTheme.spacing.small),
    ) {
        if (carousel.title.isNotBlank()) {
            Text(
                text = carousel.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            items(
                items = carousel.items,
                key = { item -> item.id },
                contentType = { "A2UiMediaCard" },
            ) { mediaCard ->
                A2UiMediaCardRenderer(
                    mediaCard = mediaCard,
                    onAction = onAction,
                    modifier = Modifier.width(260.dp),
                )
            }
        }
    }
}
