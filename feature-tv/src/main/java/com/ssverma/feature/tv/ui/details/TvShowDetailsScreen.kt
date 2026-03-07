package com.ssverma.feature.tv.ui.details

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchShareTextIntent
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.foundation.Emphasize
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.HorizontalLazyListSection
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.feature.account.ui.stats.MediaStatsAction
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.navigation.args.TvSeasonArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingAvailableTypes
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.tv.TvSeason
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.utils.ShareMediaUtils
import com.ssverma.shared.ui.component.ActionSize
import com.ssverma.shared.ui.component.BackdropHeader
import com.ssverma.shared.ui.component.GenreItem
import com.ssverma.shared.ui.component.Highlight
import com.ssverma.shared.ui.component.Highlights
import com.ssverma.shared.ui.component.media.MediaItem
import com.ssverma.shared.ui.component.section.CreditSection
import com.ssverma.shared.ui.component.section.ImageShotsSection
import com.ssverma.shared.ui.component.section.OverviewSection
import com.ssverma.shared.ui.component.section.ReviewsSection
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionContentHeaderSpacing
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionVerticalSpacing
import com.ssverma.shared.ui.component.section.TagsSection
import com.ssverma.shared.ui.component.section.VideoShotsSection
import com.ssverma.shared.ui.emptyIfAbsent

@Composable
fun TvShowDetailsScreen(
    onBackPressed: () -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: (tvShowId: Int) -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
    openTvShowList: (listingArgs: TvShowListingArgs) -> Unit,
    openTvSeasonDetails: (seasonArgs: TvSeasonArgs) -> Unit,
    viewModel: TvShowDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(color = MaterialTheme.colorScheme.background) {
        DriveCompose(
            uiState = uiState.tvShowDetailsUiState,
            onRetry = { viewModel.fetchTvShowDetails() }
        ) { tvShow ->
            TvShowContent(
                tvShow = tvShow,
                viewModel = viewModel,
                uiState = uiState,
                onBackPressed = onBackPressed,
                openTvShowDetails = openTvShowDetails,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                openReviewsList = { openReviewsList(tvShow.id) },
                openYoutube = { videoId -> viewModel.openYoutubeApp(videoId = videoId) },
                openPersonDetails = openPersonDetails,
                openTvShowList = openTvShowList,
                openTvSeasonDetails = openTvSeasonDetails
            )
        }
    }
}

@Composable
private fun TvShowContent(
    tvShow: TvShow,
    viewModel: TvShowDetailsViewModel,
    uiState: TvShowDetailsScreenUiState,
    onBackPressed: () -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: () -> Unit,
    openYoutube: (videoId: String) -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
    openTvShowList: (listingArgs: TvShowListingArgs) -> Unit,
    openTvSeasonDetails: (seasonArgs: TvSeasonArgs) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        item {
            BackdropHeader(
                backdropImageUrl = tvShow.backdropImageUrl,
                onCloseIconClick = onBackPressed,
                onTrailerFabClick = { viewModel.onPlayTrailerClicked(tvShow) },
                secondaryActions = {
                    MediaStatsAction(
                        mediaType = MediaType.Tv,
                        mediaId = tvShow.id
                    )
                    FloatingActionButton(
                        onClick = {
                            val shareableText = ShareMediaUtils.buildShareableMediaText(
                                mediaTitle = tvShow.title,
                                mediaTagline = tvShow.tagline,
                                mediaOverview = tvShow.overview,
                                appPackageName = context.packageName
                            )
                            context.dispatchShareTextIntent(text = shareableText)
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(ActionSize)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    }
                }
            )
        }

        item {
            Text(
                text = tvShow.title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
            )
        }

        tvShow.tagline?.let { tagline ->
            item {
                Emphasize {
                    Text(
                        text = tagline,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            Highlights(
                highlights = remember(tvShow) { tvShow.highlightedItems() },
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        item {
            OverviewSection(
                overview = tvShow.overview,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = SectionVerticalSpacing)
            )
        }

        item {
            HorizontalLazyList(
                items = tvShow.generes,
                contentPadding = PaddingValues(top = SectionVerticalSpacing, start = 16.dp, end = 16.dp)
            ) { genre ->
                GenreItem(genre = genre) {
                    openTvShowList(
                        TvShowListingArgs(
                            listingType = TvShowListingAvailableTypes.Genre,
                            title = genre.name,
                            genreId = genre.id
                        )
                    )
                }
            }
        }

        item {
            SeasonsSection(
                seasons = tvShow.seasons,
                onSeasonClick = { season ->
                    openTvSeasonDetails(
                        TvSeasonArgs(
                            tvShowId = viewModel.tvShowId,
                            seasonNumber = season.seasonNumber
                        )
                    )
                },
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        item {
            CreditSection(
                casts = tvShow.casts,
                onPersonClick = openPersonDetails,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        item {
            ImageShotsSection(
                imageShots = uiState.imageShots,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                maxImageShots = 6,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        item {
            VideoShotsSection(
                videos = tvShow.videos,
                onVideoClick = { openYoutube(it.key) },
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        item {
            ReviewsSection(
                reviews = tvShow.reviews,
                onReviewsViewAllClick = openReviewsList,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        item {
            RelevantTvShowsSection(
                tvShows = tvShow.similarTvShows,
                sectionTitleRes = R.string.similar_shows,
                openTvShowDetails = openTvShowDetails,
                modifier = Modifier.padding(top = SectionVerticalSpacing),
            )
        }

        item {
            RelevantTvShowsSection(
                tvShows = tvShow.recommendations,
                sectionTitleRes = R.string.recommendations,
                openTvShowDetails = openTvShowDetails,
                modifier = Modifier.padding(top = SectionVerticalSpacing),
            )
        }

        item {
            TagsSection(
                keywords = tvShow.keywords,
                onClick = { keyword ->
                    openTvShowList(
                        TvShowListingArgs(
                            listingType = TvShowListingAvailableTypes.Keyword,
                            title = keyword.name,
                            keywordId = keyword.id
                        )
                    )
                },
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

@Composable
private fun RelevantTvShowsSection(
    tvShows: List<TvShow>,
    @StringRes sectionTitleRes: Int,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalLazyListSection(
        items = tvShows,
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = sectionTitleRes),
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        itemContent = {
            MediaItem(
                title = it.title,
                posterImageUrl = it.posterImageUrl,
                modifier = Modifier.width(100.dp),
                onClick = { openTvShowDetails(it.id) }
            )
        },
        hideIf = tvShows.isEmpty(),
        modifier = modifier
    )
}

@Composable
private fun SeasonsSection(
    seasons: List<TvSeason>,
    onSeasonClick: (season: TvSeason) -> Unit,
    modifier: Modifier = Modifier
) {
    var seasonCount by remember {
        mutableIntStateOf(if (seasons.size < 3) seasons.size else 3)
    }
    val showSeasonViewAll by remember { derivedStateOf { seasonCount < seasons.size } }

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.seasons_n, seasons.size),
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        headerContentSpacing = SectionContentHeaderSpacing,
        hideIf = seasons.isEmpty(),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp).animateContentSize()
        ) {
            for (i in 0 until seasonCount) {
                TvSeasonItem(tvSeason = seasons[i], onClick = { onSeasonClick(seasons[i]) })
            }
            if (showSeasonViewAll) {
                TextButton(onClick = { seasonCount = seasons.size }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.view_more))
                }
            }
        }
    }
}

private fun TvShow.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(labelRes = R.string.rating, value = voteAvg.emptyIfAbsent()),
        Highlight(labelRes = R.string.first_air_date, value = displayFirstAirDate.orEmpty()),
        Highlight(labelRes = R.string.status, value = status),
        Highlight(labelRes = R.string.language, value = originalLanguage),
        Highlight(labelRes = R.string.seasons, value = seasonCount.toString()),
        Highlight(labelRes = R.string.episode_number, value = episodeCount.toString())
    )
}
