package com.ssverma.showtime.ui.people

import TmdbBackdropAspectRatio
import TmdbPersonAspectRatio
import TmdbPosterAspectRatio
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.ssverma.core.ui.UiText
import com.ssverma.core.ui.image.NetworkImage
import com.ssverma.core.ui.layout.HorizontalLazyListIndexed
import com.ssverma.shared.ui.component.Avatar
import com.ssverma.shared.ui.component.BackdropNavigationAction
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.MediaType
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.domain.model.PersonMedia
import com.ssverma.showtime.domain.model.emptyImageShot
import com.ssverma.showtime.ui.Highlight
import com.ssverma.showtime.ui.ImagePagerScreen
import com.ssverma.core.ui.DriveCompose
import com.ssverma.showtime.ui.movie.Highlights
import com.ssverma.showtime.ui.movie.ImageShotItem
import kotlinx.coroutines.launch
import kotlin.math.min

private enum class BottomSheetContent {
    Images,
    Media
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PersonDetailsScreen(
    viewModel: PersonDetailsViewModel,
    onBackPress: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openPersonAllImages: (personId: Int) -> Unit,
) {

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var sheetContentType by remember { mutableStateOf(BottomSheetContent.Images) }

    var profileImagePageIndex by remember { mutableStateOf(0) }

    DriveCompose(
        uiState = viewModel.personDetailUiState,
        onRetry = { viewModel.fetchPersonDetails() }
    ) { person ->
        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetGesturesEnabled = false,
            sheetContent = {
                if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                    BackHandler {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    }
                }

                when (sheetContentType) {
                    BottomSheetContent.Images -> {
                        ImagePagerScreen(
                            observableImageShots = viewModel.imageShots,
                            defaultPageIndex = profileImagePageIndex,
                            onBackPressed = {
                                coroutineScope.launch {
                                    bottomSheetScaffoldState.bottomSheetState.collapse()
                                }
                            }
                        )
                    }
                    BottomSheetContent.Media -> {
                        PersonMediaTabs(
                            personMediaByType = person.mediaByType,
                            openMovieDetails = openMovieDetails,
                            openTvShowDetails = openTvShowDetails,
                            showAllMedia = true,
                            onViewAllMediaClicked = {
                                //No need to handle, only for recent media
                            },
                            modifier = Modifier.statusBarsPadding()
                        )
                    }
                }
            },
            sheetBackgroundColor = MaterialTheme.colors.background,
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            PersonContent(
                person = person,
                onBackPress = onBackPress,
                openImagePage = { pageIndex ->
                    sheetContentType = BottomSheetContent.Images
                    profileImagePageIndex = pageIndex
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    }
                },
                openMovieDetails = openMovieDetails,
                openTvShowDetails = openTvShowDetails,
                openPersonAllMedia = {
                    sheetContentType = BottomSheetContent.Media
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    }
                },
                openPersonAllImages = openPersonAllImages,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun PersonContent(
    person: Person,
    onBackPress: () -> Unit,
    openImagePage: (Int) -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openPersonAllMedia: () -> Unit,
    openPersonAllImages: (personId: Int) -> Unit,
    modifier: Modifier = Modifier
) {

    var isBioExpended by remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier) {
        item {
            BackdropHeader(
                backdropImageUrl = person.imageShots.lastOrNull()?.imageUrl ?: person.imageUrl,
                profileImageUrl = person.imageUrl,
                onBackPress = onBackPress
            )
        }

        item {
            Text(
                text = person.name,
                style = MaterialTheme.typography.h5,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SectionSpacing, start = 16.dp, end = 16.dp)
            )
        }

        item {
            val na = stringResource(id = R.string.na)

            Highlights(
                highlights = remember {
                    listOf(
                        Highlight(labelRes = R.string.known_for, person.knownFor),
                        Highlight(labelRes = R.string.place_of_birth, person.placeOfBirth),
                        Highlight(labelRes = R.string.dob, person.dob ?: na),
                    )
                },
                modifier = Modifier.padding(top = SectionSpacing)
            )
        }

        item {
            Text(
                text = person.biography,
                textAlign = TextAlign.Start,
                maxLines = if (isBioExpended) Int.MAX_VALUE else BiographyMaxLines,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .padding(top = SectionSpacing)
                    .animateContentSize()
                    .clickable {
                        isBioExpended = !isBioExpended
                    }
            )
        }

        item {
            HorizontalLazyListIndexed(
                items = remember { person.imageShots + listOf(emptyImageShot()) },
                contentPadding = PaddingValues(
                    top = SectionSpacing,
                    start = 16.dp,
                    end = 16.dp
                )
            ) { index, imageShot ->
                if (index <= person.imageShots.lastIndex) {
                    ImageShotItem(
                        imageShot = imageShot,
                        contentScale = ContentScale.Crop,
                        onClick = {
                            openImagePage(index)
                        },
                        modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(TmdbPersonAspectRatio)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(TmdbPersonAspectRatio)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colors.onSurface,
                                shape = MaterialTheme.shapes.medium.copy(CornerSize(16.dp))
                            )
                            .clickable {
                                openPersonAllImages(person.id)
                            }
                    ) {
                        Text(
                            text = stringResource(id = R.string.view_tagged_images),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        item {
            PersonMediaTabs(
                personMediaByType = person.mediaByType,
                openMovieDetails = openMovieDetails,
                openTvShowDetails = openTvShowDetails,
                onViewAllMediaClicked = openPersonAllMedia,
                modifier = Modifier.padding(top = SectionSpacing)
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun PersonMediaTabs(
    personMediaByType: Map<MediaType, List<PersonMedia>>,
    openMovieDetails: (movieId: Int) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    onViewAllMediaClicked: () -> Unit,
    modifier: Modifier = Modifier,
    showAllMedia: Boolean = false,
) {
    if (personMediaByType.isEmpty()) {
        return
    }

    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        },
        backgroundColor = MaterialTheme.colors.background,
        modifier = modifier
    ) {
        personMediaByType.onEachIndexed { index, entry ->
            Tab(
                text = {
                    Text(text = stringResource(id = entry.key.asUiText().resId) + " (${entry.value.size})")
                },
                selected = pagerState.currentPage == index,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
            )
        }
    }

    var clickedMediaInfo: String? by remember {
        mutableStateOf(null)
    }

    if (clickedMediaInfo != null) {
        Dialog(onDismissRequest = { clickedMediaInfo = null }) {
            Surface(
                shape = MaterialTheme.shapes.medium.copy(CornerSize(8.dp))
            ) {
                Text(
                    text = clickedMediaInfo.orEmpty(),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        count = personMediaByType.size,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
    ) { page ->
        personMediaByType.onEachIndexed { pageIndex, entry ->
            if (pageIndex == page) {
                if (showAllMedia) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        itemsIndexed(entry.value) { index, media ->
                            TimelineItem(
                                media = media,
                                onInfoIconClick = { clickedMediaInfo = media.overview },
                                openMovieDetails = openMovieDetails,
                                openTvShowDetails = openTvShowDetails
                            )
                        }
                    }
                } else {
                    PersonRecentMedia(
                        entry = entry,
                        onMediaInfoClicked = { clickedMediaInfo = it },
                        openMovieDetails = openMovieDetails,
                        openTvShowDetails = openTvShowDetails,
                        onViewAllMediaClicked = onViewAllMediaClicked
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonRecentMedia(
    entry: Map.Entry<MediaType, List<PersonMedia>>,
    onMediaInfoClicked: (mediaInfo: String) -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    onViewAllMediaClicked: () -> Unit,
) {
    Column {
        val recentMedia = remember {
            entry.value.subList(0, min(TopMediaCount, entry.value.size))
        }
        recentMedia.forEachIndexed { index, personMedia ->
            TimelineItem(
                media = personMedia,
                onInfoIconClick = { onMediaInfoClicked(personMedia.overview) },
                openMovieDetails = openMovieDetails,
                openTvShowDetails = openTvShowDetails,
                modifier = Modifier.padding(
                    top = 8.dp,
                    bottom = if (index == entry.value.lastIndex) 24.dp else 0.dp
                )
            )
        }

        if (recentMedia.size < entry.value.size) {
            TextButton(
                onClick = onViewAllMediaClicked,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 24.dp)
            ) {
                Text(text = stringResource(id = R.string.see_all))
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TimelineItem(
    media: PersonMedia,
    onInfoIconClick: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {

        Text(
            text = media.displayReleaseDate ?: "",
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .weight(0.30f)
                .padding(horizontal = 16.dp)
        )

        Card(
            onClick = {
                when (media.mediaType) {
                    MediaType.Movie -> {
                        openMovieDetails(media.id)
                    }
                    MediaType.Tv -> {
                        openTvShowDetails(media.id)
                    }
                    MediaType.Unknown -> {
                        //
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.70f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                NetworkImage(
                    url = media.posterImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .width(40.dp)
                        .aspectRatio(TmdbPosterAspectRatio)
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(0.95f)
                ) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.subtitle1,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.as_n, media.character),
                        style = MaterialTheme.typography.caption
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                IconButton(
                    onClick = onInfoIconClick,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.54f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BackdropHeader(
    backdropImageUrl: String,
    profileImageUrl: String,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(modifier) {
        val (refBackdrop, refProfile, refRoundedSurface) = createRefs()

        /*Backdrop*/
        Box(modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(TmdbBackdropAspectRatio)
            .constrainAs(refBackdrop) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
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
        }

        /*Rounded surface*/
        Box(modifier = Modifier
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
            .constrainAs(refRoundedSurface) {
                bottom.linkTo(refBackdrop.bottom)
                start.linkTo(refBackdrop.start)
                end.linkTo(refBackdrop.end)
            }
        )

        /*Profile*/
        Avatar(imageUrl = profileImageUrl,
            onClick = {},
            modifier = Modifier
                .size(96.dp)
                .constrainAs(refProfile) {
                    top.linkTo(refRoundedSurface.top)
                    bottom.linkTo(refRoundedSurface.bottom)
                    start.linkTo(refRoundedSurface.start)
                    end.linkTo(refRoundedSurface.end)
                }
        )
    }
}

fun MediaType.asUiText(): UiText.StaticText {
    return when (this) {
        MediaType.Movie -> UiText.StaticText(resId = R.string.movie)
        MediaType.Tv -> UiText.StaticText(resId = R.string.tv)
        MediaType.Unknown -> UiText.StaticText(resId = R.string.unknown)
    }
}

private val SectionSpacing = 16.dp
private const val TopMediaCount = 10
private const val BiographyMaxLines = 4
private val SurfaceCornerRoundSize = 12.dp