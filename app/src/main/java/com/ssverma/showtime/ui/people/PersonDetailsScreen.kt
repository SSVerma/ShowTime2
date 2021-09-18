package com.ssverma.showtime.ui.people

import TmdbBackdropAspectRatio
import TmdbPosterAspectRatio
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.Highlight
import com.ssverma.showtime.domain.model.MediaType
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.domain.model.PersonMedia
import com.ssverma.showtime.extension.emptyIfNull
import com.ssverma.showtime.ui.ImagePagerScreen
import com.ssverma.showtime.ui.common.*
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
) {

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var sheetContentType by remember { mutableStateOf(BottomSheetContent.Images) }

    var profileImagePageIndex by remember { mutableStateOf(0) }

    DriveCompose(observable = viewModel.livePersonDetails) { person ->
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
                            liveImageShots = viewModel.imageShots,
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
                    .padding(16.dp)
                    .animateContentSize()
                    .clickable {
                        isBioExpended = !isBioExpended
                    }
            )
        }

        item {
            HorizontalList(items = person.imageShots) {
                ImageShotItem(
                    imageShot = it,
                    onClick = {
                        openImagePage(person.imageShots.indexOf(it))//TODO:improve
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .aspectRatio(person.imageShots.first().aspectRatio)
                        .padding(top = SectionSpacing)
                )
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
    val pagerState = rememberPagerState(pageCount = personMediaByType.size)
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
                    Text(text = stringResource(id = entry.key.displayNameRes) + " (${entry.value.size})")
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
                    text = clickedMediaInfo.emptyIfNull(),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .navigationBarsPadding(bottom = showAllMedia)
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
        val (refBackdrop, refProfile) = createRefs()

        /*Backdrop*/
        Box(
            modifier = Modifier
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

        /*Profile*/
        Avatar(imageUrl = profileImageUrl,
            onClick = {},
            modifier = Modifier
                .size(96.dp)
                .constrainAs(refProfile) {
                    top.linkTo(refBackdrop.bottom)
                    bottom.linkTo(refBackdrop.bottom)
                    start.linkTo(refBackdrop.start)
                    end.linkTo(refBackdrop.end)
                }
        )
    }
}

private val SectionSpacing = 16.dp
private const val TopMediaCount = 10
private const val BiographyMaxLines = 4