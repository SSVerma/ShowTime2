package com.ssverma.feature.library.ui.wrapped.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.image.NetworkImage
import com.ssverma.feature.library.R
import com.ssverma.shared.domain.model.stats.WrappedYearSummary

@Composable
fun WrappedStoryCardView(
    summary: WrappedYearSummary,
    modifier: Modifier = Modifier,
    userName: String? = null,
    style: WrappedStoryStyle = WrappedStoryStyle.CLASSIC_VELVET,
    showWatermark: Boolean = true
) {
    val (backgroundBrush, borderColor, accentColor, textWhite, textMuted, cardBg) = when (style) {
        WrappedStoryStyle.CLASSIC_VELVET -> StoryTheme(
            brush = Brush.verticalGradient(
                listOf(WrappedStoryPalette.VelvetTop, WrappedStoryPalette.VelvetBottom)
            ),
            border = WrappedStoryPalette.VelvetAccent.copy(alpha = 0.4f),
            accent = WrappedStoryPalette.VelvetAccent,
            textWhite = WrappedStoryPalette.VelvetTextWhite,
            textMuted = WrappedStoryPalette.VelvetTextMuted,
            cardBg = WrappedStoryPalette.VelvetCardBg
        )

        WrappedStoryStyle.OLED_NOIR -> StoryTheme(
            brush = Brush.verticalGradient(
                listOf(WrappedStoryPalette.OledTop, WrappedStoryPalette.OledBottom)
            ),
            border = WrappedStoryPalette.OledBorder,
            accent = WrappedStoryPalette.OledAccent,
            textWhite = WrappedStoryPalette.OledTextWhite,
            textMuted = WrappedStoryPalette.OledTextMuted,
            cardBg = WrappedStoryPalette.OledCardBg
        )

        WrappedStoryStyle.NEON_CYBERPUNK -> StoryTheme(
            brush = Brush.verticalGradient(
                listOf(WrappedStoryPalette.CyberpunkTop, WrappedStoryPalette.CyberpunkBottom)
            ),
            border = WrappedStoryPalette.CyberpunkCyan.copy(alpha = 0.5f),
            accent = WrappedStoryPalette.CyberpunkCyan,
            textWhite = WrappedStoryPalette.CyberpunkTextWhite,
            textMuted = WrappedStoryPalette.CyberpunkTextMuted,
            cardBg = WrappedStoryPalette.CyberpunkCardBg
        )

        WrappedStoryStyle.GOLDEN_VIP -> StoryTheme(
            brush = Brush.verticalGradient(
                listOf(WrappedStoryPalette.GoldTop, WrappedStoryPalette.GoldBottom)
            ),
            border = WrappedStoryPalette.GoldAccent.copy(alpha = 0.5f),
            accent = WrappedStoryPalette.GoldAccent,
            textWhite = WrappedStoryPalette.GoldTextWhite,
            textMuted = WrappedStoryPalette.GoldTextMuted,
            cardBg = WrappedStoryPalette.GoldCardBg
        )
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(28.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header Tag
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        accentColor.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val headerText = when {
                            userName != null && summary.year == 0 -> "${userName.uppercase()}'S ALL-TIME WRAPPED"
                            userName != null -> "${userName.uppercase()}'S ${summary.year} WRAPPED"
                            summary.year == 0 -> "ALL-TIME CINEPHILE WRAPPED"
                            else -> "${summary.year} CINEPHILE WRAPPED"
                        }
                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Hero Watch Time
                Text(
                    text = "${summary.totalWatchHours}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = textWhite
                )
                Text(
                    text = "HOURS WATCHED",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = accentColor
                )
                Text(
                    text = "~${"%.1f".format(summary.totalDaysEquivalent)} days of pure cinema",
                    style = MaterialTheme.typography.bodySmall,
                    color = textMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Key Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatBox(
                        title = "TOTAL LOGGED",
                        value = "${summary.totalLogged}",
                        subtitle = "${summary.totalMovies} Movies • ${summary.totalTvShows} TV",
                        bgColor = cardBg,
                        accentColor = accentColor,
                        textWhite = textWhite,
                        textMuted = textMuted,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "AVG RATING",
                        value = "${"%.1f".format(summary.averageUserRating)} ★",
                        subtitle = "${summary.rewatchCount} Rewatches",
                        bgColor = cardBg,
                        accentColor = accentColor,
                        textWhite = textWhite,
                        textMuted = textMuted,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Top Rated Media Row (if available)
                if (summary.topRatedMedia.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "TOP RATED MASTERPIECES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = accentColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        summary.topRatedMedia.take(3).forEach { media ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(2f / 3f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                ) {
                                    NetworkImage(
                                        url = media.posterImageUrl,
                                        contentDescription = media.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.matchParentSize()
                                    )
                                    Surface(
                                        color = cardBg.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(topStart = 8.dp),
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(
                                                horizontal = 5.dp,
                                                vertical = 2.dp
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Star,
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "${media.userRating.toInt()}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = textWhite
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = media.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = textWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Peak Month Highlight
                summary.mostActiveMonth?.let { activeMonth ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = cardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            borderColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Most Active Month",
                                style = MaterialTheme.typography.bodySmall,
                                color = textMuted
                            )
                            Text(
                                text = "${activeMonth.monthName} (${activeMonth.count} watches)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = textWhite
                            )
                        }
                    }
                }

                // Watermark Branding Footer
                if (showWatermark) {
                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(
                        color = textMuted.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Movie,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.wrapped_watermark_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    subtitle: String,
    bgColor: androidx.compose.ui.graphics.Color,
    accentColor: androidx.compose.ui.graphics.Color,
    textWhite: androidx.compose.ui.graphics.Color,
    textMuted: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = textWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textMuted,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class StoryTheme(
    val brush: Brush,
    val border: androidx.compose.ui.graphics.Color,
    val accent: androidx.compose.ui.graphics.Color,
    val textWhite: androidx.compose.ui.graphics.Color,
    val textMuted: androidx.compose.ui.graphics.Color,
    val cardBg: androidx.compose.ui.graphics.Color
)
