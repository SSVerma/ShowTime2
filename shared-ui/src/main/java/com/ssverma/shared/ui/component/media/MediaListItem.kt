package com.ssverma.shared.ui.component.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun MediaListItem(
    title: String,
    posterImageUrl: String,
    overview: String,
    modifier: Modifier = Modifier,
    indicator: (@Composable () -> Unit)? = null,
    metadata: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MediaPoster(
                posterImageUrl = posterImageUrl,
                indicator = indicator,
                onClick = onClick,
                modifier = Modifier
                    .width(MediaItemDefaults.ListItemPosterWidth)
                    .fillMaxSize()
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                metadata?.let { it() }

                Text(
                    text = overview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
