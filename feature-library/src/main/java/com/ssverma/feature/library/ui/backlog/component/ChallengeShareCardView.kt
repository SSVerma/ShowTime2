package com.ssverma.feature.library.ui.backlog.component

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.core.image.NetworkImage
import com.ssverma.feature.library.R
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeProgress

@Composable
fun ChallengeShareCardView(
    progress: ChallengeProgress,
    modifier: Modifier = Modifier,
    userName: String? = null
) {
    val isCompleted = progress.isCompleted

    val backgroundBrush = if (isCompleted) {
        Brush.verticalGradient(
            colors = listOf(
                ChallengeShareColor.GoldCardBackgroundTop,
                ChallengeShareColor.GoldCardBackgroundBottom
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                ChallengeShareColor.CardBackgroundTop,
                ChallengeShareColor.CardBackgroundBottom
            )
        )
    }

    val borderColor = if (isCompleted) {
        ChallengeShareColor.GoldCardBorder
    } else {
        ChallengeShareColor.CardBorder
    }

    val accentColor = if (isCompleted) {
        ChallengeShareColor.GoldAccent
    } else {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category & Branding Pill
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = accentColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Rounded.EmojiEvents else Icons.Rounded.Movie,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (progress.challenge.category) {
                                    ChallengeCategory.Curated -> stringResource(R.string.challenges_category_essential)
                                    ChallengeCategory.DirectorSpotlight -> stringResource(R.string.challenges_category_director)
                                    ChallengeCategory.DecadeClassics -> stringResource(R.string.challenges_category_decade)
                                    ChallengeCategory.GenreSprint -> stringResource(R.string.challenges_category_genre)
                                    ChallengeCategory.PersonalGoal -> stringResource(R.string.challenges_category_goal)
                                }.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Milestone Level Badge
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = ChallengeShareColor.ItemBackground
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WorkspacePremium,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = progress.milestoneTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ChallengeShareColor.TextWhite
                            )
                        }
                    }
                }

                // Center Content: Title, Stats & Poster Strip
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isCompleted) {
                        Surface(
                            shape = CircleShape,
                            color = ChallengeShareColor.GoldAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.5.dp, ChallengeShareColor.GoldAccent),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = ChallengeShareColor.GoldAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.challenges_share_certificate_badge),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChallengeShareColor.GoldAccent,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = progress.challenge.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ChallengeShareColor.TextWhite,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    if (progress.challenge.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = progress.challenge.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCompleted) ChallengeShareColor.TextGoldMuted else ChallengeShareColor.TextMuted,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Metric Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${progress.progressPercentage}%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(
                                    R.string.challenges_progress_completed,
                                    progress.watchedCount,
                                    progress.totalCount
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ChallengeShareColor.TextWhite
                            )
                            Text(
                                text = if (isCompleted) {
                                    stringResource(R.string.challenges_completed_label)
                                } else {
                                    stringResource(R.string.challenges_active_status)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCompleted) ChallengeShareColor.GoldAccent else ChallengeShareColor.TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (progress.progressPercentage / 100f).coerceIn(0f, 1f) },
                        color = accentColor,
                        trackColor = ChallengeShareColor.ProgressTrack,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    // Poster Collage Preview (up to 4 films)
                    val displayPosters = (progress.watchedItems + progress.remainingItems)
                        .map { it.posterImageUrl }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .take(4)

                    if (displayPosters.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            displayPosters.forEachIndexed { index, posterUrl ->
                                val isThisWatched = index < progress.watchedItems.size
                                Box(
                                    modifier = Modifier
                                        .size(width = 54.dp, height = 78.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (isThisWatched) accentColor else ChallengeShareColor.ItemBackground,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    NetworkImage(
                                        url = posterUrl.convertToTmdbPosterUrl(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (isThisWatched) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    ChallengeShareColor.CardBackgroundBottom.copy(
                                                        alpha = 0.4f
                                                    )
                                                ),
                                            contentAlignment = Alignment.BottomEnd
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier
                                                    .padding(3.dp)
                                                    .size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer Watermark & Branding
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.challenges_share_watermark),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ChallengeShareColor.TextMuted
                        )
                    }

                    if (!userName.isNullOrBlank()) {
                        Text(
                            text = userName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChallengeShareColor.TextWhite
                        )
                    }
                }
            }
        }
    }
}
