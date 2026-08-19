package com.ssverma.shared.ui.component.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage

@Composable
fun MediaPoster(
    posterImageUrl: String,
    modifier: Modifier = Modifier,
    indicator: (@Composable () -> Unit)? = null,
    actionIcon: (@Composable () -> Unit)? = null,
    onOverflowIconClick: (() -> Unit)? = null,
    overlayContent: @Composable () -> Unit = {},
    onClick: () -> Unit,
) {
    OutlinedCard(
        modifier = modifier,
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
    ) {
        Box {
            NetworkImage(
                url = posterImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Unified Top Overlays Row (Indicator on TopStart, Action on TopEnd)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(6.dp)
            ) {
                if (indicator != null) {
                    Box(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        indicator()
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (actionIcon != null) {
                    Box(modifier = Modifier.padding(start = 4.dp)) {
                        actionIcon()
                    }
                } else {
                    Box(modifier = Modifier.padding(start = 4.dp)) {
                        overlayContent()
                    }
                }
            }
        }
    }
}
