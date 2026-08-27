package com.ssverma.feature.tv.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.DriveCompose
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.analytics.TvAnalyticsEvent
import com.ssverma.feature.tv.analytics.TvAnalyticsScreenName
import com.ssverma.feature.tv.navigation.args.TvEpisodeArgs
import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.domain.model.tv.TvEpisode
import com.ssverma.shared.domain.model.tv.TvSeason
import com.ssverma.shared.ui.TmdbBackdropAspectRatio
import com.ssverma.shared.ui.bottomsheet.ImageShotBottomSheet
import com.ssverma.shared.ui.bottomsheet.SheetContentType
import com.ssverma.shared.ui.bottomsheet.rememberImageShotBottomSheetState
import com.ssverma.shared.ui.component.BackdropNavigationAction
import com.ssverma.shared.ui.component.Highlight
import com.ssverma.shared.ui.component.Highlights
import com.ssverma.shared.ui.component.media.DateBadge
import com.ssverma.shared.ui.component.media.ScoreBadge
import com.ssverma.shared.ui.component.section.CreditSection
import com.ssverma.shared.ui.component.section.ImageShotsSection
import com.ssverma.shared.ui.component.section.OverviewSection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvSeasonDetailsScreen(
    onBackPress: () -> Unit,
    openEpisodeDetails: (episodeArgs: TvEpisodeArgs) -> Unit,
    openPersonDetails: (Cast) -> Unit,
    viewModel: TvSeasonDetailsViewModel
) {
    val analytics = LocalAnalytics.current
    val imageSheetState = rememberImageShotBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val watchedEpisodes by viewModel.watchedEpisodes.collectAsStateWithLifecycle()

    TrackScreenView(screenName = TvAnalyticsScreenName.TV_SEASON)

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        DriveCompose(
            uiState = uiState,
            onRetry = { viewModel.fetchTvSeason() }
        ) { tvSeason ->
            ImageShotBottomSheet(
                imageShots = tvSeason.posters,
                sheetState = imageSheetState
            ) {
                TvSeasonContent(
                    tvSeason = tvSeason,
                    watchedEpisodes = watchedEpisodes,
                    onBackPress = onBackPress,
                    onEpisodeClick = { episode ->
                        analytics.logEvent(
                            TvAnalyticsEvent.EpisodeClicked(
                                episode = episode,
                                tvShowId = viewModel.tvShowId,
                                sourceScreen = TvAnalyticsScreenName.TV_SEASON
                            )
                        )
                        openEpisodeDetails(
                            TvEpisodeArgs(
                                tvShowId = viewModel.tvShowId,
                                seasonNumber = episode.seasonNumber,
                                episodeNumber = episode.episodeNumber,
                                tvShowTitle = viewModel.tvShowTitle,
                                tvShowPosterPath = viewModel.tvShowPosterPath
                            )
                        )
                    },
                    onToggleEpisodeWatched = { episodeNumber ->
                        viewModel.toggleEpisodeWatched(episodeNumber)
                    },
                    onMarkSeasonWatched = { episodeNumbers ->
                        viewModel.markSeasonWatched(episodeNumbers)
                    },
                    openPersonDetails = { cast ->
                        analytics.logEvent(
                            TvAnalyticsEvent.CastClicked(
                                cast = cast,
                                sourceScreen = TvAnalyticsScreenName.TV_SEASON
                            )
                        )
                        openPersonDetails(cast)
                    },
                    openImageShotsList = {
                        coroutineScope.launch {
                            imageSheetState.show(SheetContentType.ImageList)
                        }
                    },
                    openImageShot = { pageIndex ->
                        coroutineScope.launch {
                            imageSheetState.show(SheetContentType.ImagePager(pageIndex))
                        }
                    },
                    modifier = Modifier
                        .padding(it)
                )
            }
        }
    }
}

@Composable
private fun TvSeasonContent(
    tvSeason: TvSeason,
    watchedEpisodes: Set<Int>,
    onBackPress: () -> Unit,
    onEpisodeClick: (TvEpisode) -> Unit,
    onToggleEpisodeWatched: (episodeNumber: Int) -> Unit,
    onMarkSeasonWatched: (List<Int>) -> Unit,
    openPersonDetails: (Cast) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            BackdropHeader(
                backdropImageUrl = tvSeason.posterImageUrl,
                onBackPress = onBackPress
            )
        }

        item {
            Text(
                text = tvSeason.title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        item {
            Highlights(
                highlights = remember { tvSeason.highlightedItems() },
                modifier = Modifier
                    .padding(top = SectionSpacing)
                    .padding(horizontal = 16.dp)
            )
        }

        item {
            OverviewSection(
                overview = tvSeason.overview,
                modifier = Modifier
                    .padding(top = SectionSpacing)
                    .padding(horizontal = 16.dp)
            )
        }

        item {
            CreditSection(
                casts = tvSeason.casts,
                onPersonClick = openPersonDetails,
                source = "tv_season_credit",
                modifier = Modifier.padding(top = SectionSpacing)
            )
        }

        item {
            ImageShotsSection(
                imageShots = tvSeason.posters,
                maxImageShots = MaxImageShots,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                modifier = Modifier.padding(top = SectionSpacing)
            )
        }

        item {
            val allEpisodesCount = tvSeason.episodes.size
            val watchedCount = tvSeason.episodes.count { it.episodeNumber in watchedEpisodes }
            val isAllWatched = allEpisodesCount > 0 && watchedCount == allEpisodesCount

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SectionSpacing, bottom = 8.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.episodes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (watchedCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$watchedCount / $allEpisodesCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                FilledTonalButton(
                    onClick = {
                        val allNumbers = tvSeason.episodes.map { it.episodeNumber }
                        onMarkSeasonWatched(if (isAllWatched) emptyList() else allNumbers)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isAllWatched) Color(0xFF4CAF50).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.6f
                        )
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = if (isAllWatched) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isAllWatched) "All Watched" else "Mark Season",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isAllWatched) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        items(items = tvSeason.episodes) { episode ->
            val isWatched = episode.episodeNumber in watchedEpisodes
            TvEpisodeItem(
                tvEpisode = episode,
                isWatched = isWatched,
                onClick = { onEpisodeClick(episode) },
                onToggleWatched = { onToggleEpisodeWatched(episode.episodeNumber) },
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(SectionSpacing))
        }
    }
}

@Composable
private fun BackdropHeader(
    backdropImageUrl: String,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        NetworkImage(
            url = backdropImageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(TmdbBackdropAspectRatio)
        )

        /*Navigation*/
        BackdropNavigationAction(onIconClick = onBackPress)

        /*Rounded surface*/
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SurfaceCornerRoundSize)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium.copy(
                        topStart = CornerSize(SurfaceCornerRoundSize),
                        topEnd = CornerSize(SurfaceCornerRoundSize),
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp)
                    ),
                )
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun TvEpisodeItem(
    tvEpisode: TvEpisode,
    isWatched: Boolean = false,
    onClick: () -> Unit,
    onToggleWatched: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isWatched) {
                Color(0xFF4CAF50).copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkImage(
                url = tvEpisode.posterImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(110.dp)
                    .aspectRatio(TmdbBackdropAspectRatio)
                    .clip(MaterialTheme.shapes.medium)
            )

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = "${tvEpisode.episodeNumber}. ${tvEpisode.title}",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tvEpisode.displayAirDate?.let {
                        DateBadge(dateText = it)
                    }
                    ScoreBadge(score = (tvEpisode.voteAvg * 10))
                }

                Text(
                    text = tvEpisode.overview,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    fontStyle = FontStyle.Normal,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Mark Watched Button
            IconButton(
                onClick = onToggleWatched,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(36.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isWatched) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.6f
                    ),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = if (isWatched) "Watched" else "Mark Watched",
                            tint = if (isWatched) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.6f
                            ),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun TvSeason.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.season_number,
            value = seasonNumber.toString()
        ),
        Highlight(
            labelRes = R.string.episodes,
            value = episodes.size.toString()
        ),
        Highlight(
            labelRes = R.string.air_date,
            value = displayAirDate.orEmpty()
        ),
    )
}

private val SectionSpacing = 20.dp
private val SurfaceCornerRoundSize = 12.dp
private const val MaxImageShots = 3
