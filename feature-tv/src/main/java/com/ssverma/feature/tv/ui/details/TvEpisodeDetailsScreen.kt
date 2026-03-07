package com.ssverma.feature.tv.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvEpisodeDetailsScreen(
    onBackPress: () -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
    viewModel: TvEpisodeDetailsViewModel = hiltViewModel()
) {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val imageSheetState = rememberImageShotBottomSheetState(
        bottomSheetScaffoldState = bottomSheetScaffoldState
    )

    val coroutineScope = rememberCoroutineScope()

    val tvEpisodeUiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(color = MaterialTheme.colorScheme.background) {
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
                style = MaterialTheme.typography.headlineSmall,
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(TmdbBackdropAspectRatio)
    ) {
        NetworkImage(
            url = backdropImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        BackdropNavigationAction(onIconClick = onBackPress)

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
