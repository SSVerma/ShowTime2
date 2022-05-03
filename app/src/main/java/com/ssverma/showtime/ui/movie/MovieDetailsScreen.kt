package com.ssverma.showtime.ui.movie

import MediaItem
import TmdbBackdropAspectRatio
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.accompanist.insets.navigationBarsPadding
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.*
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.domain.model.movie.highlightedItems
import com.ssverma.showtime.extension.placeholderIfNullOrEmpty
import com.ssverma.showtime.ui.GenreItem
import com.ssverma.showtime.ui.TagItem
import com.ssverma.showtime.ui.common.*

@Composable
fun MovieDetailsScreen(
    viewModel: MovieDetailsViewModel,
    onBackPressed: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: (movieId: Int) -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
    openMovieList: (listingArgs: MovieListingArgs) -> Unit,
) {
    Surface(
        color = MaterialTheme.colors.background
    ) {
        DriveCompose(observable = viewModel.liveMovieDetails) {
            MovieContent(
                movie = it,
                viewModel = viewModel,
                onBackPressed = onBackPressed,
                openMovieDetails = openMovieDetails,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                openReviewsList = { openReviewsList(it.id) },
                openYoutube = { videoId ->
                    viewModel.openYoutubeApp(videoId = videoId)
                },
                openPersonDetails = openPersonDetails,
                openMovieList = openMovieList
            )
        }
    }
}

@Composable
fun MovieContent(
    movie: Movie,
    viewModel: MovieDetailsViewModel,
    onBackPressed: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: () -> Unit,
    openYoutube: (videoId: String) -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
    openMovieList: (listingArgs: MovieListingArgs) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageShots by viewModel.imageShots.observeAsState(emptyList())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {

        /*Backdrop*/
        item {
            BackdropHeader(
                backdropImageUrl = movie.backdropImageUrl,
                onCloseIconClick = onBackPressed,
                onTrailerFabClick = { viewModel.onPlayTrailerClicked(movie) }
            )
        }

        /*Title*/
        item {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.h5,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
            )
        }

        /*Tagline*/
        movie.tagline?.let { tagline ->
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
                highlights = remember(movie) { movie.highlightedItems() },
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Overview section*/
        item {
            OverviewSection(
                overview = movie.overview,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = SectionVerticalSpacing)
            )
        }

        /*Genre*/
        item {
            HorizontalList(
                items = movie.generes,
                contentPadding = PaddingValues(
                    top = SectionVerticalSpacing,
                    start = 16.dp,
                    end = 16.dp
                )
            ) { genre ->
                GenreItem(genre = genre) {
                    openMovieList(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.Genre,
                            title = genre.name,
                            genreId = genre.id
                        )
                    )
                }
            }
        }

        /*Cast*/
        item {
            CreditSection(
                casts = movie.casts,
                onPersonClick = openPersonDetails,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Image shots*/
        item {
            ImageShotsSection(
                imageShots = imageShots,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Video shots*/
        item {
            VideoShotsSection(
                videos = movie.videos,
                onVideoClick = { openYoutube(it.key) },
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Reviews*/
        item {
            ReviewsSection(
                reviews = movie.reviews,
                onReviewsViewAllClick = openReviewsList,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Similar movies*/
        item {
            RelevantMoviesSection(
                movies = movie.similarMovies,
                sectionTitleRes = R.string.similar_movies,
                openMovieDetails = openMovieDetails,
                modifier = Modifier.padding(top = SectionVerticalSpacing),
            )
        }

        /*Recommendations*/
        item {
            RelevantMoviesSection(
                movies = movie.recommendations,
                sectionTitleRes = R.string.recommendations,
                openMovieDetails = openMovieDetails,
                modifier = Modifier.padding(top = SectionVerticalSpacing),
            )
        }

        /*Keyword*/
        item {
            TagsSection(
                keywords = movie.keywords,
                onClick = { keyword ->
                    openMovieList(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.Keyword,
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
fun BackdropHeader(
    modifier: Modifier = Modifier,
    backdropImageUrl: String,
    onCloseIconClick: () -> Unit,
    onTrailerFabClick: () -> Unit,
) {

    ConstraintLayout(modifier) {
        val (refBackdrop, refRoundedSurface, refTrailerFab, refSecondaryActions) = createRefs()

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

            /*Backdrop*/
            NetworkImage(
                url = backdropImageUrl,
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier.fillMaxSize()
            )

            /*Navigation action*/
            BackdropNavigationAction(onIconClick = onCloseIconClick)
        }

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
                .constrainAs(refRoundedSurface) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        /*Trailer Action*/
        FloatingActionButton(
            onClick = onTrailerFabClick,
            modifier = Modifier
                .padding(start = 16.dp)
                .constrainAs(refTrailerFab) {
                    top.linkTo(refRoundedSurface.top)
                    bottom.linkTo(refRoundedSurface.top)
                    start.linkTo(refRoundedSurface.start)
                }
        ) {
            Icon(imageVector = AppIcons.PlayArrow, contentDescription = null)
        }

        /*Secondary actions*/
        Actions(
            onAddToClick = { /*TODO*/ },
            onSendIconClick = {/*TODO*/ },
            modifier = Modifier
                .padding(end = 16.dp)
                .constrainAs(refSecondaryActions) {
                    top.linkTo(refRoundedSurface.top)
                    bottom.linkTo(refRoundedSurface.top)
                    end.linkTo(refRoundedSurface.end)
                }
        )
    }
}

@Composable
fun Actions(
    modifier: Modifier = Modifier,
    onAddToClick: () -> Unit,
    onSendIconClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //Add to ... (Favorite, Watchlist, etc.)
        Action(
            icon = AppIcons.Add,
            onClick = onAddToClick
        )
        //Send
        Action(
            icon = AppIcons.Send,
            onClick = onSendIconClick
        )
    }
}


@Composable
fun Action(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        backgroundColor = MaterialTheme.colors.surface,
        modifier = modifier.size(ActionSize)
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}

@Composable
fun OverviewSection(
    overview: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.overview),
                hideTrailingAction = true
            )
        },
        hideIf = overview.isEmpty(),
        headerContentSpacing = SectionContentHeaderSpacing,
        modifier = modifier
    ) {
        Text(
            text = overview,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Justify,
            maxLines = if (expanded) Int.MAX_VALUE else OverviewMaxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable {
                expanded = !expanded
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Highlights(
    highlights: List<Highlight>,
    modifier: Modifier = Modifier,
    columnCount: Int = 3,
    @StringRes absentPlaceholderRes: Int = R.string.na,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(24.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceAround
) {

    VerticalGrid(
        items = highlights,
        columnCount = columnCount,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier
    ) { _, item ->
        HighlightItem(
            labelRes = item.labelRes,
            value = item.value.placeholderIfNullOrEmpty(placeholderRes = absentPlaceholderRes),
            modifier = Modifier
        )
    }

}

@Composable
fun HighlightItem(
    @StringRes labelRes: Int,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Emphasize {
            Text(text = stringResource(id = labelRes), style = MaterialTheme.typography.caption)
        }

        Text(
            text = value,
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ImageShotsSection(
    imageShots: List<ImageShot>,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    maxImageShots: Int = MaxImageShots,
) {
    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.shots),
                modifier = Modifier.padding(horizontal = 16.dp),
                onTrailingActionClicked = openImageShotsList,
                hideTrailingAction = imageShots.size <= maxImageShots
            )
        },
        headerContentSpacing = SectionContentHeaderSpacing,
        hideIf = imageShots.isEmpty(),
        modifier = modifier
    ) {
        VerticalGrid(
            items = imageShots,
            columnCount = 3,
            max = maxImageShots,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) { index, item ->
            ImageShotItem(
                imageShot = item,
                onClick = { openImageShot(index) },
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(imageShots.first().aspectRatio)
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageShotItem(
    imageShot: ImageShot,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentScale: ContentScale = ContentScale.Fit,
    shape: Shape = MaterialTheme.shapes.medium.copy(CornerSize(16.dp))
) {
    Card(
        shape = shape,
        elevation = 0.dp,
        onClick = onClick,
        modifier = modifier,
        backgroundColor = backgroundColor
    ) {
        NetworkImage(
            url = imageShot.imageUrl,
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VideoShotsSection(
    videos: List<Video>,
    onVideoClick: (video: Video) -> Unit,
    modifier: Modifier = Modifier
) {
    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.videos),
                subtitle = stringResource(id = R.string.video_header_subtitle),
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        hideIf = videos.isNullOrEmpty(),
        headerContentSpacing = SectionContentHeaderSpacing,
        modifier = modifier
    ) {
        HorizontalList(items = videos) {
            VideoItem(
                video = it,
                onVideoClick = {
                    onVideoClick(it)
                },
                modifier = Modifier
                    .width(VideoIteWidth)
                    .height(VideoIteHeight)
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VideoItem(
    video: Video,
    onVideoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.medium.copy(CornerSize(8.dp)),
        elevation = 0.dp,
        onClick = onVideoClick,
        modifier = modifier.aspectRatio(TmdbBackdropAspectRatio)
    ) {
        Box {
            NetworkImage(
                url = video.youtubeThumbnailUrl(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .scrim(
                        colors = listOf(
                            MaterialTheme.colors.surface.copy(alpha = 0.30f),
                            MaterialTheme.colors.surface.copy(alpha = 0.10f),
                        )
                    )
            )

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.54f),
                onClick = onVideoClick,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = AppIcons.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp),
                    tint = MaterialTheme.colors.surface
                )
            }
        }
    }

}

@Composable
fun ReviewsSection(
    reviews: List<Review>,
    onReviewsViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reviewCount = if (reviews.size < MaxReviews) reviews.size else MaxReviews

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.reviews),
                modifier = Modifier.padding(horizontal = 16.dp),
                onTrailingActionClicked = onReviewsViewAllClick,
                hideTrailingAction = reviews.size <= MaxReviews
            )
        },
        headerContentSpacing = SectionContentHeaderSpacing,
        hideIf = reviews.isEmpty(),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            for (i in 0 until reviewCount) {
                ReviewItem(
                    review = reviews[i]
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReviewItem(
    review: Review,
    modifier: Modifier = Modifier
) {

    var expended by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
    ) {
        Avatar(
            imageUrl = review.author.avatarImageUrl,
            onClick = {
                //TODO
            },
            modifier = Modifier
                .padding(end = 16.dp)
        )

        Surface(
            shape = MaterialTheme.shapes.medium.copy(topStart = CornerSize(0.dp)),
            onClick = {
                expended = !expended
            },
            elevation = if (expended) 0.5.dp else 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .animateContentSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    val authorName = if (!review.author.name.isNullOrEmpty()) {
                        review.author.name
                    } else if (review.author.userName.isNotEmpty()) {
                        review.author.userName
                    } else {
                        stringResource(id = R.string.unknown)
                    }

                    /*Author*/
                    BoxWithConstraints {
                        Text(
                            text = authorName,
                            style = MaterialTheme.typography.subtitle1.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .background(
                                    color = MaterialTheme.colors.primary.copy(alpha = 0.16f),
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(horizontal = 4.dp)
                                .widthIn(max = maxWidth / 2)
                        )
                    }

                    /*Timestamp*/
                    review.displayCreatedAt?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.caption,
                            textAlign = TextAlign.End
                        )
                    }
                }

                /*Review content*/
                Text(
                    text = review.content,
                    style = MaterialTheme.typography.body1,
                    maxLines = if (expended) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun CreditSection(
    casts: List<Cast>,
    onPersonClick: (personId: Int) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int = R.string.casts,
) {
    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = titleRes),
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        headerContentSpacing = SectionContentHeaderSpacing,
        hideIf = casts.isNullOrEmpty(),
        modifier = modifier
    ) {
        HorizontalList(items = casts) {
            CastItem(
                cast = it,
                onClick = {
                    onPersonClick(it.id)
                },
                modifier = Modifier.width(104.dp)
            )
        }
    }
}

@Composable
fun CastItem(
    cast: Cast,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Avatar(
            imageUrl = cast.avatarImageUrl,
            onClick = onClick,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = cast.name,
            style = MaterialTheme.typography.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = cast.character,
            style = MaterialTheme.typography.caption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RelevantMoviesSection(
    movies: List<Movie>,
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
        hideIf = movies.isNullOrEmpty(),
        modifier = modifier
    ) {
        HorizontalList(items = movies) {
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
fun TagsSection(
    keywords: List<Keyword>,
    onClick: (keyword: Keyword) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int = R.string.tags,
) {
    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = titleRes),
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        headerContentSpacing = SectionContentHeaderSpacing,
        hideIf = keywords.isNullOrEmpty(),
        modifier = modifier
    ) {
        HorizontalList(items = keywords) {
            TagItem(keyword = it) {
                onClick(it)
            }
        }
    }
}

val ActionSize = 40.dp
val SurfaceCornerRoundSize = 12.dp
val SectionVerticalSpacing = 32.dp
val SectionContentHeaderSpacing = 16.dp
private const val MaxImageShots = 9
const val MaxReviews = 3
val VideoIteWidth = 200.dp
val VideoIteHeight = 112.5.dp
private const val OverviewMaxLines = 5