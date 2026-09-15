package com.ssverma.feature.movie.ui.details.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.domain.model.Video
import com.ssverma.shared.ui.component.section.ImageShotsSection
import com.ssverma.shared.ui.component.section.VideoShotsSection

fun LazyListScope.movieImageShotsSection(
    imageShots: List<ImageShot>,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    maxImageShots: Int = 3,
    modifier: Modifier = Modifier
) {
    if (imageShots.isNotEmpty()) {
        item(key = "movie_image_shots", contentType = "image_shots") {
            ImageShotsSection(
                imageShots = imageShots,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                maxImageShots = maxImageShots,
                modifier = modifier
            )
        }
    }
}

fun LazyListScope.movieVideoShotsSection(
    videos: List<Video>,
    onVideoClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    if (videos.isNotEmpty()) {
        item(key = "movie_video_shots", contentType = "video_shots") {
            VideoShotsSection(
                videos = videos,
                onVideoClick = onVideoClick,
                modifier = modifier
            )
        }
    }
}
