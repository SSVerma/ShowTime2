package com.ssverma.feature.library.ui.share.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.feature.library.ui.share.ListShareColor
import com.ssverma.shared.domain.model.library.ListShareCardFormat

@Composable
fun PosterMosaicGrid(
    posters: List<String>,
    themeConfig: StoryCardThemeConfig,
    format: ListShareCardFormat,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            posters.isEmpty() -> {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = themeConfig.cardBg,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .aspectRatio(2f / 3f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Movie,
                            contentDescription = null,
                            tint = themeConfig.textSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            posters.size == 1 -> {
                PosterItem(
                    url = posters[0],
                    themeConfig = themeConfig,
                    modifier = Modifier
                        .fillMaxHeight(0.9f)
                        .aspectRatio(2f / 3f)
                )
            }

            posters.size in 2..3 -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    posters.forEach { url ->
                        PosterItem(
                            url = url,
                            themeConfig = themeConfig,
                            modifier = Modifier
                                .fillMaxHeight(if (format == ListShareCardFormat.SQUARE_1_1) 0.85f else 0.75f)
                                .aspectRatio(2f / 3f)
                        )
                    }
                }
            }

            else -> {
                // 4 posters: 2x2 grid bounded strictly by available row height to prevent downward overflow
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PosterItem(
                            url = posters[0],
                            themeConfig = themeConfig,
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(2f / 3f)
                        )
                        PosterItem(
                            url = posters[1],
                            themeConfig = themeConfig,
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(2f / 3f)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PosterItem(
                            url = posters[2],
                            themeConfig = themeConfig,
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(2f / 3f)
                        )
                        PosterItem(
                            url = posters[3],
                            themeConfig = themeConfig,
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(2f / 3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PosterItem(
    url: String,
    themeConfig: StoryCardThemeConfig,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = themeConfig.cardBg,
        border = BorderStroke(1.dp, themeConfig.border.copy(alpha = 0.5f)),
        modifier = modifier.clip(RoundedCornerShape(12.dp))
    ) {
        NetworkImage(
            url = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun VintageSprocketHoles(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(12) {
            Box(
                modifier = Modifier
                    .size(width = 8.dp, height = 12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ListShareColor.VintageSprocketHole)
            )
        }
    }
}
