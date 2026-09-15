package com.ssverma.feature.tv.ui.details.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.domain.model.Video
import com.ssverma.shared.ui.component.section.ImageShotsSection
import com.ssverma.shared.ui.component.section.VideoShotsSection

fun LazyListScope.tvImageShotsSection(
    imageShots: List<ImageShot>,
    openImageShotsList: () -> Unit,
    openImageShot: (Int) -> Unit,
    maxImageShots: Int = 3,
    modifier: Modifier = Modifier
) {
    if (imageShots.isNotEmpty()) {
        item(key = "tv_image_shots", contentType = "image_shots") {
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

fun LazyListScope.tvVideoShotsSection(
    videos: List<Video>,
    onVideoClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    if (videos.isNotEmpty()) {
        item(key = "tv_video_shots", contentType = "video_shots") {
            VideoShotsSection(
                videos = videos,
                onVideoClick = onVideoClick,
                modifier = modifier
            )
        }
    }
}
