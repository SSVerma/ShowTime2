package com.ssverma.shared.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.model.Genre

@Composable
fun GenreItem(genre: Genre, onGenreClicked: () -> Unit) {
    SuggestionChip(
        shape = RoundedCornerShape(50),
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.24f)
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),
            borderWidth = 1.dp
        ),
        onClick = onGenreClicked,
        label = {
            Text(
                text = genre.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    )
}
