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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.image.NetworkImage
import com.ssverma.feature.library.R
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
                                    text = "SECRET COLLECTION",
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
                                    "ShowTime Cinephile"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.8.sp,
                                color = themeConfig.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PosterMosaicGrid(
    posters: List<String>,
    themeConfig: StoryCardThemeConfig,
    format: ListShareCardFormat
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
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                posters.forEach { url ->
                    PosterItem(
                        url = url,
                        themeConfig = themeConfig,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(2f / 3f)
                    )
                }
            }
        }

        else -> {
            // 4 posters: 2x2 grid
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    PosterItem(
                        url = posters[0],
                        themeConfig = themeConfig,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(2f / 3f)
                    )
                    PosterItem(
                        url = posters[1],
                        themeConfig = themeConfig,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(2f / 3f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    PosterItem(
                        url = posters[2],
                        themeConfig = themeConfig,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(2f / 3f)
                    )
                    PosterItem(
                        url = posters[3],
                        themeConfig = themeConfig,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(2f / 3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PosterItem(
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
private fun VintageSprocketHoles(modifier: Modifier = Modifier) {
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

private data class StoryCardThemeConfig(
    val brush: Brush,
    val border: Color,
    val accent: Color,
    val secondaryAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val cardBg: Color,
    val isVintage: Boolean = false
)

private fun resolveCardTheme(theme: ListShareTheme): StoryCardThemeConfig {
    return when (theme) {
        ListShareTheme.CLASSIC_SHOWTIME -> StoryCardThemeConfig(
            brush = Brush.verticalGradient(
                listOf(ListShareColor.ClassicBackgroundStart, ListShareColor.ClassicBackgroundEnd)
            ),
            border = ListShareColor.ClassicAccent.copy(alpha = 0.35f),
            accent = ListShareColor.ClassicAccent,
            secondaryAccent = ListShareColor.ClassicAccent,
            textPrimary = ListShareColor.ClassicTextPrimary,
            textSecondary = ListShareColor.ClassicTextSecondary,
            cardBg = ListShareColor.ClassicCard
        )

        ListShareTheme.VINTAGE_35MM -> StoryCardThemeConfig(
            brush = Brush.verticalGradient(
                listOf(ListShareColor.VintageBackgroundStart, ListShareColor.VintageBackgroundEnd)
            ),
            border = ListShareColor.VintageAccent.copy(alpha = 0.4f),
            accent = ListShareColor.VintageAccent,
            secondaryAccent = ListShareColor.VintageAccent,
            textPrimary = ListShareColor.VintageTextPrimary,
            textSecondary = ListShareColor.VintageTextSecondary,
            cardBg = ListShareColor.VintageCard,
            isVintage = true
        )

        ListShareTheme.OLED_MIDNIGHT -> StoryCardThemeConfig(
            brush = Brush.verticalGradient(
                listOf(ListShareColor.OledBackgroundStart, ListShareColor.OledBackgroundEnd)
            ),
            border = ListShareColor.OledBorder,
            accent = ListShareColor.OledAccent,
            secondaryAccent = ListShareColor.OledAccent,
            textPrimary = ListShareColor.OledTextPrimary,
            textSecondary = ListShareColor.OledTextSecondary,
            cardBg = ListShareColor.OledCard
        )

        ListShareTheme.NEON_CYBERPUNK -> StoryCardThemeConfig(
            brush = Brush.verticalGradient(
                listOf(
                    ListShareColor.CyberpunkBackgroundStart,
                    ListShareColor.CyberpunkBackgroundEnd
                )
            ),
            border = ListShareColor.CyberpunkBorder,
            accent = ListShareColor.CyberpunkAccentPink,
            secondaryAccent = ListShareColor.CyberpunkAccentCyan,
            textPrimary = ListShareColor.CyberpunkTextPrimary,
            textSecondary = ListShareColor.CyberpunkTextSecondary,
            cardBg = ListShareColor.CyberpunkCard
        )
    }
}
