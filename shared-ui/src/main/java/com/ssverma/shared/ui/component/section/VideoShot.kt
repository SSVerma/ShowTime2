package com.ssverma.shared.ui.component.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.Video
import com.ssverma.shared.domain.model.youtubeThumbnailUrl
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.TmdbBackdropAspectRatio

@Composable
fun VideoShotsSection(
    videos: List<Video>,
    onVideoClick: (video: Video) -> Unit,
    modifier: Modifier = Modifier
) {
    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.videos),
                subtitle = stringResource(id = R.string.video_header_subtitle),
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                hideTrailingAction = true
            )
        },
        hideIf = videos.isEmpty(),
        headerContentSpacing = SectionDefaults.SectionContentHeaderSpacing,
        modifier = modifier
    ) {
        HorizontalLazyList(
            items = videos,
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
        ) { video ->
            VideoItem(
                video = video,
                onVideoClick = { onVideoClick(video) },
                modifier = Modifier.width(VideoCardWidth)
            )
        }
    }
}

@Composable
fun VideoItem(
    video: Video,
    onVideoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        onClick = onVideoClick,
        modifier = modifier
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(TmdbBackdropAspectRatio)
            ) {
                NetworkImage(
                    url = video.youtubeThumbnailUrl(),
                    contentDescription = video.name.ifEmpty { null },
                    modifier = Modifier.fillMaxSize()
                )

                // Scrim overlay to ensure play button and badges pop
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.20f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.50f)
                                )
                            )
                        )
                )

                // Centered frosted play button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(id = R.string.play_video_cd),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                    )
                }

                // Video Type Badge in bottom start (e.g. Trailer, Teaser)
                if (video.type.isNotBlank()) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.85f),
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(MaterialTheme.spacing.small)
                    ) {
                        Text(
                            text = video.type,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(
                                horizontal = MaterialTheme.spacing.extraSmall,
                                vertical = 2.dp
                            )
                        )
                    }
                }
            }

            // Fixed height text container for perfectly uniform cards across the carousel
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(VideoTitleContainerHeight)
                    .padding(horizontal = MaterialTheme.spacing.smallMedium)
            ) {
                if (video.name.isNotBlank()) {
                    Text(
                        text = video.name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private val VideoCardWidth = 200.dp
private val VideoTitleContainerHeight = 44.dp
