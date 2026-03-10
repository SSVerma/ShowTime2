package com.ssverma.shared.ui.component.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.shared.ui.R

@Composable
fun MediaPoster(
    posterImageUrl: String,
    modifier: Modifier = Modifier,
    indicator: (@Composable () -> Unit)? = null,
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

            // Screen-level dynamic indicator (Top Left Overlay)
            indicator?.let { customIndicator ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    customIndicator()
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            ) {
                overlayContent()
            }

            onOverflowIconClick?.let {
                IconButton(
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.TopEnd)
                        .size(32.dp),
                    onClick = it
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(id = R.string.more_options_cd),
                        tint = Color.White,
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}
