package com.ssverma.showtime.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.image.NetworkImage
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.shared.ui.TmdbBackdropAspectRatio
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.bottomsheet.ImageShotBottomSheet
import com.ssverma.shared.ui.bottomsheet.SheetContentType
import com.ssverma.shared.ui.bottomsheet.rememberImageShotBottomSheetState
import com.ssverma.shared.ui.component.BackdropNavigationAction
import com.ssverma.shared.ui.component.Highlights
import com.ssverma.shared.ui.component.section.CreditSection
import com.ssverma.shared.ui.component.section.ImageShotsSection
import com.ssverma.shared.ui.component.section.OverviewSection
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.tv.TvEpisode
import com.ssverma.showtime.domain.model.tv.TvSeason
import com.ssverma.showtime.ui.highlightedItems
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TvSeasonDetailsScreen(
    viewModel: TvSeasonDetailsViewModel,
    onBackPress: () -> Unit,
    openEpisodeDetails: (episodeArgs: TvEpisodeArgs) -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
) {
    val imageSheetState = rememberImageShotBottomSheetState(
        bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    )

    val coroutineScope = rememberCoroutineScope()

    val tvSeasonUiState by viewModel.observableTvSeason.collectAsState()

    DriveCompose(
        uiState = tvSeasonUiState,
        onRetry = { viewModel.fetchTvSeason() }
    ) { tvSeason ->
        ImageShotBottomSheet(
            imageShots = tvSeason.posters,
            sheetState = imageSheetState
        ) {
            TvSeasonContent(
                tvSeason = tvSeason,
                onBackPress = onBackPress,
                onEpisodeClick = { episode ->
                    openEpisodeDetails(
                        TvEpisodeArgs(
                            tvShowId = viewModel.tvShowId,
                            seasonNumber = episode.seasonNumber,
                            episodeNumber = episode.episodeNumber
                        )
                    )
                },
                openPersonDetails = openPersonDetails,
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
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun TvSeasonContent(
    tvSeason: TvSeason,
    onBackPress: () -> Unit,
    onEpisodeClick: (episode: TvEpisode) -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
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
                style = MaterialTheme.typography.h5,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        item {
            Highlights(
                highlights = tvSeason.highlightedItems(),
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
            SectionHeader(
                title = stringResource(id = R.string.episodes),
                hideTrailingAction = true,
                modifier = Modifier
                    .padding(top = SectionSpacing, bottom = 8.dp)
                    .padding(horizontal = 16.dp)
            )
        }

        items(items = tvSeason.episodes) {
            TvEpisodeItem(
                tvEpisode = it,
                onClick = { onEpisodeClick(it) },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.padding(vertical = SectionSpacing))
        }
    }
}

@Composable
private fun BackdropHeader(
    backdropImageUrl: String,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {

    /*Backdrop*/
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(TmdbBackdropAspectRatio)
    ) {

        /*Backdrop image*/
        NetworkImage(
            url = backdropImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        /*Navigation*/
        BackdropNavigationAction(onIconClick = onBackPress)

        /*Rounded surface*/
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SurfaceCornerRoundSize)
                .background(
                    color = MaterialTheme.colors.background,
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TvEpisodeItem(
    tvEpisode: TvEpisode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkImage(
                url = tvEpisode.posterImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .width(72.dp)
                    .aspectRatio(TmdbPosterAspectRatio)
                    .clip(MaterialTheme.shapes.medium.copy(CornerSize(8.dp)))
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = tvEpisode.title, style = MaterialTheme.typography.subtitle1)
                Text(
                    text = tvEpisode.displayAirDate.orEmpty(),
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = stringResource(id = R.string.rating_n, tvEpisode.voteAvg),
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = tvEpisode.overview,
                    style = MaterialTheme.typography.caption,
                    maxLines = 2,
                    fontStyle = FontStyle.Italic,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private val SectionSpacing = 20.dp
private val SurfaceCornerRoundSize = 12.dp
private const val MaxImageShots = 3