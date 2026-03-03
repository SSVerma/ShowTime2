package com.ssverma.shared.ui.component.media

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.shared.ui.TmdbPosterAspectRatio

@Composable
fun MediaItem(
    title: String,
    posterImageUrl: String,
    modifier: Modifier = Modifier,
    posterModifier: Modifier = Modifier,
    indicator: (@Composable () -> Unit)? = null,
    onOverflowIconClick: (() -> Unit)? = null,
    titleMaxLines: Int = 1,
    titleTextStyle: TextStyle = MaterialTheme.typography.titleMedium,
    onClick: () -> Unit = {},
) {
    Column(modifier = modifier) {
        MediaPoster(
            posterImageUrl = posterImageUrl,
            indicator = indicator,
            onOverflowIconClick = onOverflowIconClick,
            onClick = onClick,
            modifier = posterModifier
                .width(MediaItemDefaults.PosterWidth)
                .aspectRatio(TmdbPosterAspectRatio)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            maxLines = titleMaxLines,
            overflow = TextOverflow.Ellipsis,
            style = titleTextStyle.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.widthIn(max = MediaItemDefaults.PosterWidth)
        )
    }
}
