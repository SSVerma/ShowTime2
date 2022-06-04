package com.ssverma.shared.ui.component.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.layout.VerticalGrid
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.component.ImageShotItem

@Composable
fun ImageShotsSection(
    imageShots: List<ImageShot>,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    maxImageShots: Int = MaxImageShots,
) {
    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.shots),
                modifier = Modifier.padding(horizontal = 16.dp),
                onTrailingActionClicked = openImageShotsList,
                hideTrailingAction = imageShots.size <= maxImageShots
            )
        },
        headerContentSpacing = SectionDefaults.SectionContentHeaderSpacing,
        hideIf = imageShots.isEmpty(),
        modifier = modifier
    ) {
        VerticalGrid(
            items = imageShots,
            columnCount = 3,
            max = maxImageShots,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) { index, item ->
            ImageShotItem(
                imageShot = item,
                onClick = { openImageShot(index) },
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(imageShots.first().aspectRatio)
            )
        }
    }
}

private const val MaxImageShots = 9