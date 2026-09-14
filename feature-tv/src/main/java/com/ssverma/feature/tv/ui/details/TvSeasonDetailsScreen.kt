package com.ssverma.feature.tv.ui.details

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchShareTextIntent
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.analytics.TvAnalyticsEvent
import com.ssverma.feature.tv.analytics.TvAnalyticsScreenName
import com.ssverma.feature.tv.navigation.args.TvEpisodeArgs
import com.ssverma.feature.tv.ui.details.component.TvEpisodeTimelineItem
import com.ssverma.feature.tv.ui.details.component.TvSeasonHeroHeader
import com.ssverma.feature.tv.ui.details.component.TvSeasonProgressSection
import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.domain.model.tv.TvEpisode
import com.ssverma.shared.domain.model.tv.TvSeason
import com.ssverma.shared.ui.bottomsheet.ImageShotBottomSheet
import com.ssverma.shared.ui.bottomsheet.SheetContentType
import com.ssverma.shared.ui.bottomsheet.rememberImageShotBottomSheetState
import com.ssverma.shared.ui.component.section.CreditSection
import com.ssverma.shared.ui.component.section.ImageShotsSection
import com.ssverma.shared.ui.component.section.OverviewSection
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionVerticalSpacing
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
    val context = LocalContext.current

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
                    tvShowTitle = viewModel.tvShowTitle,
                    tvShowBackdropPath = viewModel.tvShowBackdropPath,
                    watchedEpisodes = watchedEpisodes,
                    onBackPress = onBackPress,
                    onShareClick = {
                        analytics.logEvent(
                            TvAnalyticsEvent.ShareClicked(
                                tvShowId = viewModel.tvShowId,
                                sourceScreen = TvAnalyticsScreenName.TV_SEASON
                            )
                        )
                        val shareText = context.getString(
                            R.string.share_season_text,
                            tvSeason.title,
                            viewModel.tvShowTitle ?: tvSeason.title
                        )
                        context.dispatchShareTextIntent(text = shareText)
                    },
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
                                tvShowPosterPath = viewModel.tvShowPosterPath,
                                tvShowBackdropPath = viewModel.tvShowBackdropPath
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
                    modifier = Modifier.padding(it)
                )
            }
        }
    }
}

@Composable
private fun TvSeasonContent(
    tvSeason: TvSeason,
    tvShowTitle: String?,
    tvShowBackdropPath: String?,
    watchedEpisodes: Set<Int>,
    onBackPress: () -> Unit,
    onShareClick: () -> Unit,
    onEpisodeClick: (TvEpisode) -> Unit,
    onToggleEpisodeWatched: (episodeNumber: Int) -> Unit,
    onMarkSeasonWatched: (List<Int>) -> Unit,
    openPersonDetails: (Cast) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val allEpisodesCount = tvSeason.episodes.size
    val watchedCount = remember(tvSeason.episodes, watchedEpisodes) {
        tvSeason.episodes.count { it.episodeNumber in watchedEpisodes }
    }
    val isAllWatched = allEpisodesCount > 0 && watchedCount == allEpisodesCount
    val firstUnwatchedEpisodeNumber = remember(tvSeason.episodes, watchedEpisodes) {
        tvSeason.episodes.firstOrNull { it.episodeNumber !in watchedEpisodes }?.episodeNumber
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Full Width Hero Header (Series backdrop, poster, titles, highlights)
            item(key = "season_hero_header") {
                TvSeasonHeroHeader(
                    tvSeason = tvSeason,
                    tvShowTitle = tvShowTitle,
                    tvShowBackdropPath = tvShowBackdropPath,
                    onBackPress = onBackPress,
                    onShareClick = onShareClick
                )
            }

            // Overview Section
            if (tvSeason.overview.isNotBlank()) {
                item(key = "season_overview") {
                    OverviewSection(
                        overview = tvSeason.overview,
                        modifier = Modifier
                            .padding(top = SectionVerticalSpacing)
                            .padding(horizontal = MaterialTheme.spacing.medium)
                    )
                }
            }

            // Cast & Credits
            if (tvSeason.casts.isNotEmpty()) {
                item(key = "season_casts") {
                    CreditSection(
                        casts = tvSeason.casts,
                        onPersonClick = openPersonDetails,
                        source = "tv_season_credit",
                        modifier = Modifier.padding(top = SectionVerticalSpacing)
                    )
                }
            }

            // Image Shots
            if (tvSeason.posters.isNotEmpty()) {
                item(key = "season_shots") {
                    ImageShotsSection(
                        imageShots = tvSeason.posters,
                        maxImageShots = 3,
                        openImageShotsList = openImageShotsList,
                        openImageShot = openImageShot,
                        modifier = Modifier.padding(top = SectionVerticalSpacing)
                    )
                }
            }

            // Season Progress Section
            item(key = "season_progress") {
                TvSeasonProgressSection(
                    watchedCount = watchedCount,
                    totalEpisodesCount = allEpisodesCount,
                    isAllWatched = isAllWatched,
                    onMarkSeasonClick = {
                        val allNumbers = tvSeason.episodes.map { it.episodeNumber }
                        onMarkSeasonWatched(if (isAllWatched) emptyList() else allNumbers)
                    },
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                )
            }

            // Episode Timeline List
            itemsIndexed(
                items = tvSeason.episodes,
                key = { _, it -> it.id },
                contentType = { _, _ -> "episode_timeline_item" }
            ) { index, episode ->
                val isWatched = episode.episodeNumber in watchedEpisodes
                val isPrevWatched =
                    if (index > 0) tvSeason.episodes[index - 1].episodeNumber in watchedEpisodes else false
                val isNextWatched =
                    if (index < tvSeason.episodes.size - 1) tvSeason.episodes[index + 1].episodeNumber in watchedEpisodes else false

                TvEpisodeTimelineItem(
                    tvEpisode = episode,
                    isWatched = isWatched,
                    isPrevWatched = isPrevWatched,
                    isNextWatched = isNextWatched,
                    isFirst = index == 0,
                    isLast = index == tvSeason.episodes.size - 1,
                    isUpNext = episode.episodeNumber == firstUnwatchedEpisodeNumber,
                    onClick = { onEpisodeClick(episode) },
                    onToggleWatched = { onToggleEpisodeWatched(episode.episodeNumber) }
                )
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(SectionVerticalSpacing))
            }
        }
    }
}
