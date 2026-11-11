package com.ssverma.common.ui.a2ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.common.ui.a2ui.component.A2UiActionButtonRenderer
import com.ssverma.common.ui.a2ui.component.A2UiCarouselRenderer
import com.ssverma.common.ui.a2ui.component.A2UiChipGroupRenderer
import com.ssverma.common.ui.a2ui.component.A2UiMediaCardRenderer
import com.ssverma.common.ui.a2ui.component.A2UiTextRenderer
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ai.a2ui.model.A2UiAction
import com.ssverma.shared.ai.a2ui.model.A2UiComponent
import com.ssverma.shared.ai.a2ui.model.A2UiDocument

/**
 * Top-level Agent-to-UI (A2UI) Compose renderer for ShowTime.
 *
 * Dynamically interprets the on-device AI Agent's structured UI document
 * and renders high-performance native Jetpack Compose components.
 */
@Composable
fun A2UiComposeRenderer(
    document: A2UiDocument,
    onAction: (A2UiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        val hasMatchingCarouselTitle = document.components.any {
            it is A2UiComponent.MediaCarousel && it.title.equals(document.title, ignoreCase = true)
        }
        if (!document.title.isNullOrBlank() && !hasMatchingCarouselTitle) {
            Text(
                text = document.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.small,
                ),
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        }

        document.components.forEach { component ->
            when (component) {
                is A2UiComponent.Text -> {
                    A2UiTextRenderer(
                        textComponent = component,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                    )
                }

                is A2UiComponent.MediaCard -> {
                    A2UiMediaCardRenderer(
                        mediaCard = component,
                        onAction = onAction,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.small,
                        ),
                    )
                }

                is A2UiComponent.MediaCarousel -> {
                    A2UiCarouselRenderer(
                        carousel = component,
                        onAction = onAction,
                    )
                }

                is A2UiComponent.ChipGroup -> {
                    A2UiChipGroupRenderer(
                        chipGroup = component,
                        onAction = onAction,
                    )
                }

                is A2UiComponent.ActionButton -> {
                    A2UiActionButtonRenderer(
                        actionButton = component,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}
