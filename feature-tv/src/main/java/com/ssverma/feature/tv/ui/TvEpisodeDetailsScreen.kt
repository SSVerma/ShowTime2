package com.ssverma.feature.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.DriveCompose
import com.ssverma.feature.tv.R
import com.ssverma.shared.domain.model.tv.TvEpisode
import com.ssverma.shared.ui.TmdbBackdropAspectRatio
import com.ssverma.shared.ui.bottomsheet.ImageShotBottomSheet
import com.ssverma.shared.ui.bottomsheet.SheetContentType
import com.ssverma.shared.ui.bottomsheet.rememberImageShotBottomSheetState
import com.ssverma.shared.ui.component.BackdropNavigationAction
import com.ssverma.shared.ui.component.Highlight
import com.ssverma.shared.ui.component.Highlights
import com.ssverma.shared.ui.component.section.CreditSection
import com.ssverma.shared.ui.component.section.ImageShotsSection
import com.ssverma.shared.ui.component.section.OverviewSection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TvEpisodeDetailsScreen(
    viewModel: TvEpisodeDetailsViewModel,
    onBackPress: () -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
) {
    val imageSheetState = rememberImageShotBottomSheetState(
        bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    )

    val coroutineScope = rememberCoroutineScope()

    val tvEpisodeUiState by viewModel.observableTvEpisode.collectAsState()

    DriveCompose(
        uiState = tvEpisodeUiState,
        onRetry = { viewModel.fetchTvEpisode() }
    ) { episode ->
        ImageShotBottomSheet(
            imageShots = episode.stills,
            sheetState = imageSheetState,
        ) {
            TvEpisodeContent(
                episode = episode,
                onBackPress = onBackPress,
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
private fun TvEpisodeContent(
    episode: TvEpisode,
    onBackPress: () -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            BackdropHeader(
                backdropImageUrl = episode.posterImageUrl,
                onBackPress = onBackPress
            )
        }

        item {
            Text(
                text = episode.title,
                style = MaterialTheme.typography.h5,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        item {
            Highlights(
                highlights = remember { episode.highlightedItems() },
                modifier = Modifier
                    .padding(top = SectionSpacing)
                    .padding(horizontal = 16.dp)
            )
        }

        item {
            OverviewSection(
                overview = episode.overview,
                modifier = Modifier
                    .padding(top = SectionSpacing)
                    .padding(horizontal = 16.dp)
            )
        }

        item {
            ImageShotsSection(
                imageShots = episode.stills,
                maxImageShots = MaxImageShots,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                modifier = Modifier.padding(top = SectionSpacing)
            )
        }

        item {
            CreditSection(
                casts = episode.casts,
                onPersonClick = openPersonDetails,
                modifier = Modifier.padding(top = SectionSpacing)
            )
        }

        item {
            CreditSection(
                casts = episode.guestStars,
                titleRes = R.string.guest_appearance,
                onPersonClick = openPersonDetails,
                modifier = Modifier.padding(top = SectionSpacing)
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

private fun TvEpisode.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.season_number,
            value = seasonNumber.toString()
        ),
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.toString()
        ),
        Highlight(
            labelRes = R.string.air_date,
            value = displayAirDate.orEmpty()
        ),
    )
}

private val SectionSpacing = 24.dp
private val SurfaceCornerRoundSize = 12.dp
private const val MaxImageShots = 6