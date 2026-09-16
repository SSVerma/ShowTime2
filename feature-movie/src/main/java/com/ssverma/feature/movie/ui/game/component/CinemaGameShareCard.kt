package com.ssverma.feature.movie.ui.game.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.ui.game.GameStatus
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.shared.domain.model.game.DailyCinemaPuzzle
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.core.ui.R as CoreUiR

@Composable
fun CinemaGameShareCard(
    stats: CinemaGameStats,
    puzzle: DailyCinemaPuzzle?,
    gameStatus: GameStatus,
    guessCount: Int,
    isBonusReel: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: App Logo & Branding
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = CoreUiR.drawable.ic_showtime_logo),
                        contentDescription = stringResource(id = R.string.cinema_stats_showtime_cinema_brand),
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(id = R.string.cinema_stats_showtime_cinema_brand),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                            if (isBonusReel) {
                                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.cinema_challenge_bonus_reel_badge),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontSize = 8.sp,
                                        modifier = Modifier.padding(
                                            horizontal = 4.dp,
                                            vertical = 1.dp
                                        )
                                    )
                                }
                            }
                        }
                        Text(
                            text = stringResource(id = R.string.cinema_stats_daily_challenge_sub),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (puzzle != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = "#${puzzle.puzzleNumber}${if (isBonusReel) "B" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Movie Reveal Details Row
            if (gameStatus != GameStatus.IN_PROGRESS && puzzle != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                        .padding(MaterialTheme.spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NetworkImage(
                        url = puzzle.posterImageUrl,
                        contentDescription = puzzle.targetMovieTitle,
                        modifier = Modifier
                            .width(48.dp)
                            .aspectRatio(TmdbPosterAspectRatio)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                    Column(modifier = Modifier.weight(1f)) {
                        val isWon = gameStatus == GameStatus.WON
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isWon) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                                Text(
                                    text = if (isBonusReel) {
                                        stringResource(
                                            id = R.string.cinema_stats_solved_in_guesses_bonus,
                                            guessCount
                                        )
                                    } else {
                                        stringResource(
                                            id = R.string.cinema_stats_solved_in_guesses,
                                            guessCount
                                        )
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                                Text(
                                    text = stringResource(id = R.string.cinema_stats_answer_revealed),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Text(
                            text = puzzle.targetMovieTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(
                                id = R.string.cinema_stats_movie_details,
                                puzzle.releaseYear,
                                puzzle.director
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            }

            // 4 Stats Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShareStatBox(
                    label = stringResource(id = R.string.cinema_stats_played),
                    value = "${stats.gamesPlayed}"
                )
                ShareStatBox(
                    label = stringResource(id = R.string.cinema_stats_win_rate),
                    value = "${stats.winPercentage}%"
                )
                ShareStatBox(
                    label = stringResource(id = R.string.cinema_stats_streak),
                    value = "${stats.currentStreak}",
                    icon = Icons.Rounded.LocalFireDepartment,
                    iconTint = MaterialTheme.colorScheme.tertiary
                )
                ShareStatBox(
                    label = stringResource(id = R.string.cinema_stats_max_streak),
                    value = "${stats.maxStreak}",
                    icon = Icons.Rounded.EmojiEvents,
                    iconTint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Footer branding
            Text(
                text = stringResource(id = R.string.cinema_stats_footer_brand),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ShareStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )
            if (icon != null) {
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = iconTint
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false
        )
    }
}
