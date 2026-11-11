package com.ssverma.common.ui.a2ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ai.a2ui.model.A2UiAction
import com.ssverma.shared.ai.a2ui.model.A2UiComponent

/**
 * Renders a row of interactive vibe/genre suggestion chips emitted by the AI Agent.
 */
@Composable
fun A2UiChipGroupRenderer(
    chipGroup: A2UiComponent.ChipGroup,
    onAction: (A2UiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = MaterialTheme.spacing.small),
    ) {
        if (!chipGroup.title.isNullOrBlank()) {
            Text(
                text = chipGroup.title.orEmpty(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            items(
                items = chipGroup.chips,
                key = { chip -> chip.id },
                contentType = { "A2UiChip" },
            ) { chip ->
                SuggestionChip(
                    onClick = {
                        chip.action?.let { action ->
                            onAction(action)
                        }
                    },
                    label = {
                        Text(
                            text = chip.label,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                )
            }
        }
    }
}
