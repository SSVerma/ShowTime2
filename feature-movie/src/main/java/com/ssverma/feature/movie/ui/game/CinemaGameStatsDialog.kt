package com.ssverma.feature.movie.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.ui.game.component.CinemaGameGuessDistributionCard
import com.ssverma.feature.movie.ui.game.component.CinemaGameHeroRevealCard
import com.ssverma.feature.movie.ui.game.component.CinemaGameSecondChanceBanner
import com.ssverma.feature.movie.ui.game.component.CinemaGameShareCard
import com.ssverma.feature.movie.ui.game.component.CinemaGameStatsBottomBar
import com.ssverma.feature.movie.ui.game.component.CinemaGameStatsSummaryCard
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.shared.domain.model.game.DailyCinemaPuzzle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaGameStatsDialog(
    stats: CinemaGameStats,
    puzzle: DailyCinemaPuzzle?,
    gameStatus: GameStatus,
    guessCount: Int,
    shareableText: String?,
    modifier: Modifier = Modifier,
    isBonusReel: Boolean = false,
    canUnlockSecondChance: Boolean = false,
    onUnlockSecondChance: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val graphicsLayer = rememberGraphicsLayer()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(id = R.string.cinema_stats_result_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isBonusReel) {
                                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.cinema_challenge_bonus_reel_badge),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(
                                                horizontal = MaterialTheme.spacing.extraSmall,
                                                vertical = 2.dp
                                            )
                                        )
                                    }
                                }
                            }
                            if (puzzle != null) {
                                Text(
                                    text = stringResource(
                                        id = R.string.cinema_challenge_puzzle_number,
                                        puzzle.puzzleNumber,
                                        if (isBonusReel) "B" else ""
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                CinemaGameStatsBottomBar(
                    graphicsLayer = graphicsLayer,
                    shareableText = shareableText
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                // Second Chance Banner if user failed Attempt #1
                if (canUnlockSecondChance) {
                    CinemaGameSecondChanceBanner(
                        onUnlockSecondChance = {
                            onDismiss()
                            onUnlockSecondChance()
                        }
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                }

                // Hero Movie Reveal Card
                if (puzzle != null) {
                    CinemaGameHeroRevealCard(
                        puzzle = puzzle,
                        gameStatus = gameStatus,
                        guessCount = guessCount,
                        isBonusReel = isBonusReel
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                }

                // Unified Stats Summary Card
                CinemaGameStatsSummaryCard(stats = stats)

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // Guess Distribution Chart
                CinemaGameGuessDistributionCard(
                    stats = stats,
                    gameStatus = gameStatus,
                    guessCount = guessCount
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // Capturable Graphical Share Card Preview Section
                Text(
                    text = stringResource(id = R.string.cinema_stats_preview_title),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        }
                ) {
                    CinemaGameShareCard(
                        stats = stats,
                        puzzle = puzzle,
                        gameStatus = gameStatus,
                        guessCount = guessCount,
                        isBonusReel = isBonusReel
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            }
        }
    }
}
