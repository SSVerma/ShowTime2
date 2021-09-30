package com.ssverma.showtime.ui.tv

import TmdbBackdropAspectRatio
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.TvEpisode
import com.ssverma.showtime.domain.model.highlightedItems
import com.ssverma.showtime.ui.common.*
import com.ssverma.showtime.ui.movie.CreditSection
import com.ssverma.showtime.ui.movie.Highlights
import com.ssverma.showtime.ui.movie.ImageShotsSection
import com.ssverma.showtime.ui.movie.OverviewSection

class TvEpisodeLaunchable(
    val tvShowId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
)

@Composable
fun TvEpisodeDetailsScreen(
    viewModel: TvEpisodeDetailsViewModel,
    onBackPress: () -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
) {
    val bottomSheetCurrentItem = remember { mutableStateOf(SheetContentType.None) }
    val clickedImageIndex = remember { mutableStateOf(0) }

    DriveCompose(observable = viewModel.liveEpisode) { episode ->
        ImageShotBottomSheet(
            imageShots = episode.posters,
            sheetItem = bottomSheetCurrentItem,
            tappedImageIndex = clickedImageIndex
        ) {
            TvEpisodeContent(
                episode = episode,
                onBackPress = onBackPress,
                openPersonDetails = openPersonDetails,
                openImageShotsList = {
                    bottomSheetCurrentItem.value = SheetContentType.ImageList
                },
                openImageShot = { pageIndex ->
                    clickedImageIndex.value = pageIndex
                    bottomSheetCurrentItem.value = SheetContentType.ImagePager
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
                highlights = episode.highlightedItems(),
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
            ImageShotsSection(
                imageShots = episode.posters,
                maxImageShots = MaxImageShots,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
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

private val SectionSpacing = 20.dp
private val SurfaceCornerRoundSize = 12.dp
private const val MaxImageShots = 3