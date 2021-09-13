package com.ssverma.showtime.ui.people

import TmdbBackdropAspectRatio
import TmdbPosterAspectRatio
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PersonDetailsScreen(
    viewModel: PersonViewModel,
    onBackPress: () -> Unit
) {

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var profileImagePageIndex by remember { mutableStateOf(0) }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                BackHandler {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }
            }

            ImagePagerScreen(
                liveImageShots = viewModel.imageShots,
                defaultPageIndex = profileImagePageIndex,
                onBackPressed = {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        DriveCompose(observable = viewModel.livePersonDetails) {
            PersonContent(
                person = it,
                onBackPress = onBackPress,
                openImagePage = { pageIndex ->
                    profileImagePageIndex = pageIndex
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    }
                },
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
            )
        }
    }
}

@Composable
fun PersonContent(
    person: Person,
    onBackPress: () -> Unit,
    openImagePage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        BackdropHeader(
            backdropImageUrl = person.imageShots.lastOrNull()?.imageUrl ?: person.imageUrl,
            profileImageUrl = person.imageUrl,
            onBackPress = onBackPress
        )

        Text(
            text = person.name,
            style = MaterialTheme.typography.h5,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = SectionSpacing, start = 16.dp, end = 16.dp)
        )

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

        Spacer(modifier = Modifier.padding(top = 24.dp))

        HorizontalList(items = person.imageShots) {
            ImageShotItem(
                imageShot = it,
                onClick = {
                    openImagePage(person.imageShots.indexOf(it))//TODO:improve
                },
                modifier = Modifier
                    .width(100.dp)
                    .aspectRatio(person.imageShots.first().aspectRatio)
            )
        }

        val mediaByType = remember {
            person.media.groupBy { it.mediaType }
        }

        PersonMediaTabs(
            personMediaByType = mediaByType,
            modifier = Modifier.padding(top = SectionSpacing)
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun PersonMediaTabs(
    personMediaByType: Map<MediaType, List<PersonMedia>>,
    modifier: Modifier = Modifier
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

    var clickedMedia: PersonMedia? by remember {
        mutableStateOf(null)
    }

    if (clickedMedia != null) {
        Dialog(onDismissRequest = { clickedMedia = null }) {
            Surface(
                shape = MaterialTheme.shapes.medium.copy(CornerSize(8.dp))
            ) {
                Text(
                    text = clickedMedia?.overview.emptyIfNull(),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.background(MaterialTheme.colors.background)
    ) { page ->
        personMediaByType.onEachIndexed { pageIndex, entry ->
            if (pageIndex == page) {
                Column {
                    entry.value.forEachIndexed { index, personMedia ->
                        TimelineItem(
                            media = personMedia,
                            onClick = {
                                clickedMedia = personMedia
                            },
                            modifier = Modifier.padding(
                                top = 8.dp,
                                bottom = if (index == entry.value.lastIndex) 24.dp else 0.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TimelineItem(
    media: PersonMedia,
    onClick: () -> Unit,
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
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.70f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NetworkImage(
                    url = media.posterImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .width(40.dp)
                        .aspectRatio(TmdbPosterAspectRatio)
                )
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(text = media.title, style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.as_n, media.character),
                        style = MaterialTheme.typography.caption
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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