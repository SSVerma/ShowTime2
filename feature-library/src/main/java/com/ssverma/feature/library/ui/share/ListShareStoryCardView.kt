package com.ssverma.feature.library.ui.share

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.feature.library.R
import com.ssverma.feature.library.ui.share.component.PosterMosaicGrid
import com.ssverma.feature.library.ui.share.component.VintageSprocketHoles
import com.ssverma.feature.library.ui.share.component.resolveCardTheme
import com.ssverma.shared.domain.model.library.ListShareCardFormat
import com.ssverma.shared.domain.model.library.ListShareTheme
import java.util.Locale

@Composable
fun ListShareStoryCardView(
    title: String,
    ownerName: String,
    itemCount: Int,
    averageRating: Float,
    posters: List<String>,
    modifier: Modifier = Modifier,
    theme: ListShareTheme = ListShareTheme.CLASSIC_SHOWTIME,
    format: ListShareCardFormat = ListShareCardFormat.STORY_9_16,
    showWatermark: Boolean = true
) {
    val themeConfig = resolveCardTheme(theme)
    val cardAspectRatio = when (format) {
        ListShareCardFormat.STORY_9_16 -> 9f / 16f
        ListShareCardFormat.SQUARE_1_1 -> 1f
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Black,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(cardAspectRatio)
            .border(width = 1.dp, color = themeConfig.border, shape = RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(themeConfig.brush)
        ) {
            if (themeConfig.isVintage) {
                VintageSprocketHoles(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp)
                )
                VintageSprocketHoles(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = if (themeConfig.isVintage) 28.dp else 20.dp,
                        vertical = 20.dp
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = themeConfig.accent.copy(alpha = 0.15f),
                            border = BorderStroke(
                                1.dp,
                                themeConfig.accent.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Movie,
                                    contentDescription = null,
                                    tint = themeConfig.accent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.secret_share_story_badge),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = themeConfig.accent
                                )
                            }
                        }

                        if (averageRating > 0f) {
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = ListShareColor.RatingGold.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = ListShareColor.RatingGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = String.format(Locale.US, "%.1f", averageRating),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ListShareColor.RatingGold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = title,
                        fontSize = if (format == ListShareCardFormat.STORY_9_16) 24.sp else 20.sp,
                        fontWeight = FontWeight.Black,
                        color = themeConfig.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = themeConfig.textSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.secret_share_curated_by, ownerName),
                            fontSize = 12.sp,
                            color = themeConfig.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = themeConfig.textSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.items_count, itemCount),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeConfig.secondaryAccent
                        )
                    }
                }

                // Middle Poster Mosaic
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PosterMosaicGrid(
                        posters = posters.take(4),
                        themeConfig = themeConfig,
                        format = format
                    )
                }

                // Bottom Footer / Watermark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = ListShareColor.WatermarkPillBackground,
                        border = BorderStroke(
                            1.dp,
                            themeConfig.border.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = themeConfig.accent,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (showWatermark) {
                                    stringResource(R.string.secret_share_watermark)
                                } else {
                                    stringResource(R.string.secret_share_story_curator_default)
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.8.sp,
                                color = themeConfig.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
