package com.ssverma.shared.ui.component.section

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.domain.model.Video
import com.ssverma.core.domain.model.youtubeThumbnailUrl
import com.ssverma.shared.ui.TmdbBackdropAspectRatio
import com.ssverma.core.ui.component.scrim
import com.ssverma.core.ui.icon.AppIcons
import com.ssverma.core.ui.image.NetworkImage
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.shared.ui.R

@OptIn(ExperimentalAnimationApi::class)
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
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        hideIf = videos.isEmpty(),
        headerContentSpacing = SectionDefaults.SectionContentHeaderSpacing,
        modifier = modifier
    ) {
        HorizontalLazyList(items = videos) {
            VideoItem(
                video = it,
                onVideoClick = {
                    onVideoClick(it)
                },
                modifier = androidx.compose.ui.Modifier
                    .width(VideoIteWidth)
                    .height(VideoIteHeight)
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VideoItem(
    video: Video,
    onVideoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.medium.copy(CornerSize(8.dp)),
        elevation = 0.dp,
        onClick = onVideoClick,
        modifier = modifier.aspectRatio(TmdbBackdropAspectRatio)
    ) {
        Box {
            NetworkImage(
                url = video.youtubeThumbnailUrl(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .scrim(
                        colors = listOf(
                            MaterialTheme.colors.surface.copy(alpha = 0.30f),
                            MaterialTheme.colors.surface.copy(alpha = 0.10f),
                        )
                    )
            )

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.54f),
                onClick = onVideoClick,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = AppIcons.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp),
                    tint = MaterialTheme.colors.surface
                )
            }
        }
    }
}

private val VideoIteWidth = 200.dp
private val VideoIteHeight = 112.5.dp