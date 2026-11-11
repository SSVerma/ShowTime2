package com.ssverma.common.ui.a2ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ai.a2ui.model.A2UiAction
import com.ssverma.shared.ai.a2ui.model.A2UiComponent

/**
 * Renders an action button emitted by the AI Agent (e.g. "Add to Watchlist").
 */
@Composable
fun A2UiActionButtonRenderer(
    actionButton: A2UiComponent.ActionButton,
    onAction: (A2UiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { onAction(actionButton.action) },
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.small,
            ),
    ) {
        Text(
            text = actionButton.label,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
