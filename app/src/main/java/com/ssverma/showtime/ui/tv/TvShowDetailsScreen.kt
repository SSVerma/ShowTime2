package com.ssverma.showtime.ui.tv

import MediaItem
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.tv.TvSeason
import com.ssverma.showtime.domain.model.tv.TvShow
import com.ssverma.showtime.ui.GenreItem
import com.ssverma.showtime.ui.common.*
import com.ssverma.showtime.ui.highlightedItems
import com.ssverma.showtime.ui.movie.*

@Composable
fun TvShowDetailsScreen(
    viewModel: TvShowDetailsViewModel,
    onBackPressed: () -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: (movieId: Int) -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
    openTvShowList: (listingArgs: TvShowListingArgs) -> Unit,
    openTvSeasonDetails: (seasonLaunchable: TvSeasonLaunchable) -> Unit
) {
    Surface(color = MaterialTheme.colors.background) {
        DriveCompose(
            uiState = viewModel.tvShowDetailsUiState,
            onRetry = {
                viewModel.fetchTvShowDetails()
            }
        ) {
            TvShowContent(
                tvShow = it,
                viewModel = viewModel,
                onBackPressed = onBackPressed,
                openTvShowDetails = openTvShowDetails,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                openReviewsList = { openReviewsList(it.id) },
                openYoutube = { videoId ->
                    viewModel.openYoutubeApp(videoId = videoId)
                },
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
    onBackPressed: () -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: () -> Unit,
    openYoutube: (videoId: String) -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
    openTvShowList: (listingArgs: TvShowListingArgs) -> Unit,
    openTvSeasonDetails: (seasonLaunchable: TvSeasonLaunchable) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {

        /*Backdrop*/
        item {
            BackdropHeader(
                backdropImageUrl = tvShow.backdropImageUrl,
                onCloseIconClick = onBackPressed,
                onTrailerFabClick = { viewModel.onPlayTrailerClicked(tvShow) }
            )
        }

        /*Title*/
        item {
            Text(
                text = tvShow.title,
                style = MaterialTheme.typography.h5,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
            )
        }

        /*Tagline*/
        tvShow.tagline?.let { tagline ->
            item {
                Emphasize {
                    Text(
                        text = tagline,
                        style = MaterialTheme.typography.caption,
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

        /*Highlights*/
        item {
            Highlights(
                highlights = remember(tvShow) { tvShow.highlightedItems() },
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Overview section title*/
        item {
            OverviewSection(
                overview = tvShow.overview,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = SectionVerticalSpacing)
            )
        }

        /*Genre*/
        item {
            HorizontalList(
                items = tvShow.generes,
                contentPadding = PaddingValues(
                    top = SectionVerticalSpacing,
                    start = 16.dp,
                    end = 16.dp
                )
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

        /*Seasons*/
        item {
            SeasonsSection(
                seasons = tvShow.seasons,
                onSeasonClick = { season ->
                    openTvSeasonDetails(
                        TvSeasonLaunchable(
                            tvShowId = viewModel.tvShowId,
                            seasonNumber = season.seasonNumber
                        )
                    )
                },
                modifier = Modifier
                    .padding(top = SectionVerticalSpacing)
            )
        }

        /*Cast*/
        item {
            CreditSection(
                casts = tvShow.casts,
                onPersonClick = openPersonDetails,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Image shots*/
        item {
            ImageShotsSection(
                imageShots = viewModel.imageShots.value,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                maxImageShots = MaxImageShots,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Video shots*/
        item {
            VideoShotsSection(
                videos = tvShow.videos,
                onVideoClick = { openYoutube(it.key) },
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Reviews*/
        item {
            ReviewsSection(
                reviews = tvShow.reviews,
                onReviewsViewAllClick = openReviewsList,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Similar TV shows*/
        item {
            SimilarTvShowsSection(
                tvShows = tvShow.similarTvShows,
                sectionTitleRes = R.string.similar_shows,
                openMovieDetails = openTvShowDetails,
                modifier = Modifier.padding(top = SectionVerticalSpacing),
            )
        }

        /*Recommendations*/
        item {
            SimilarTvShowsSection(
                tvShows = tvShow.recommendations,
                sectionTitleRes = R.string.recommendations,
                openMovieDetails = openTvShowDetails,
                modifier = Modifier.padding(top = SectionVerticalSpacing),
            )
        }

        /*Keyword*/
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

        /*Bottom spacing*/
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SimilarTvShowsSection(
    tvShows: List<TvShow>,
    @StringRes sectionTitleRes: Int,
    openMovieDetails: (movieId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = sectionTitleRes),
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        headerContentSpacing = SectionContentHeaderSpacing,
        hideIf = tvShows.isNullOrEmpty(),
        modifier = modifier
    ) {
        HorizontalList(items = tvShows) {
            MediaItem(
                title = it.title,
                posterImageUrl = it.posterImageUrl,
                modifier = Modifier.width(100.dp),
                onClick = {
                    openMovieDetails(it.id)
                }
            )
        }
    }
}

@Composable
private fun SeasonsSection(
    seasons: List<TvSeason>,
    onSeasonClick: (season: TvSeason) -> Unit,
    modifier: Modifier = Modifier
) {
    var seasonCount by remember {
        mutableStateOf(if (seasons.size < MaxSeason) seasons.size else MaxReviews)
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
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateContentSize()
        ) {
            for (i in 0 until seasonCount) {
                TvSeasonItem(
                    tvSeason = seasons[i],
                    onClick = {
                        onSeasonClick(seasons[i])
                    }
                )
            }
            if (showSeasonViewAll) {
                TextButton(
                    onClick = { seasonCount = seasons.size },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.view_more))
                }
            }
        }
    }
}

private const val MaxImageShots = 6
private const val MaxSeason = 3