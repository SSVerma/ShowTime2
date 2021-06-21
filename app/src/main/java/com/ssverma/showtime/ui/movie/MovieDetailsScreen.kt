package com.ssverma.showtime.ui.movie

import MovieItem
import TmdbBackdropAspectRatio
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.google.accompanist.insets.statusBarsPadding
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.*
import com.ssverma.showtime.ui.common.*

@Composable
fun MovieDetailsScreen(
    viewModel: MovieDetailsViewModel,
    onBackPressed: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit
) {
    Surface(
        color = MaterialTheme.colors.background
    ) {
        DriveCompose(observable = viewModel.liveMockMovie) {
            MovieContent(it, onBackPressed, openMovieDetails)
        }
    }
}

@Composable
fun MovieContent(
    movie: Movie,
    onBackPressed: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
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
                backdropImageUrl = movie.backdropImageUrl,
                onCloseIconClick = onBackPressed
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

        /*Overview section title*/
        item {
            OverviewSection(
                overview = movie.overview,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = SectionVerticalSpacing)
            )
        }

        /*Cast*/
        item {
            CreditSection(
                casts = movie.casts,
                modifier = Modifier.padding(top = SectionContentHeaderSpacing)
            )
        }

        /*Image shots*/
        item {
            ImageShotsSection(
                images = movie.backdrops,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Video shots*/
        item {
            VideoShotsSection(
                videos = movie.videos,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Reviews*/
        item {
            ReviewsSection(
                reviews = movie.reviews,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Similar movies*/
        item {
            SimilarMoviesSection(
                movies = movie.similarMovies,
                openMovieDetails = openMovieDetails,
                modifier = Modifier.padding(top = SectionVerticalSpacing),
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
    onCloseIconClick: () -> Unit
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
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colors.surface.copy(alpha = 0.54f),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 8.dp)
                    .size(40.dp)
            ) {
                IconButton(onClick = onCloseIconClick) {
                    Icon(
                        imageVector = AppIcons.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onSurface
                    )
                }
            }
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
                        topEnd = CornerSize(SurfaceCornerRoundSize)
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
            onClick = { /*TODO*/ },
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
    Section(
        sectionHeader = {
            SectionHeader(title = stringResource(id = R.string.overview))
        },
        hideIf = overview.isEmpty(),
        headerContentSpacing = SectionContentHeaderSpacing,
        modifier = modifier
    ) {
        Text(
            text = overview,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Justify
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Highlights(
    highlights: List<Highlight>,
    modifier: Modifier = Modifier
) {

    VerticalGrid(
        items = highlights,
        columnCount = 3,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = modifier
    ) { _, item ->
        HighlightItem(
            labelRes = item.labelRes,
            value = item.value,
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
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ImageShotsSection(
    images: List<ImageShot>,
    modifier: Modifier = Modifier
) {

    val topShots = remember {
        if (images.size <= MaxImageShots) {
            images
        } else {
            images.subList(0, MaxImageShots)
        }
    }

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.shots),
                modifier = Modifier.padding(horizontal = 16.dp),
                onTrailingActionClicked = {
                    //TODO
                }
            )
        },
        headerContentSpacing = SectionContentHeaderSpacing,
        hideIf = topShots.isEmpty(),
        modifier = modifier
    ) {
        VerticalGrid(
            items = topShots,
            columnCount = 3,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) { _, item ->
            Card(
                shape = MaterialTheme.shapes.medium.copy(CornerSize(16.dp)),
                elevation = 0.dp,
                onClick = {
                    //
                },
                modifier = Modifier
                    .aspectRatio(item.aspectRatio)
            ) {
                NetworkImage(
                    url = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VideoShotsSection(
    videos: List<Video>,
    modifier: Modifier = Modifier
) {

    val topShots = remember {
        if (videos.size <= MaxVideoShots) {
            videos
        } else {
            videos.subList(0, MaxVideoShots)
        }
    }

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.videos),
                subtitle = stringResource(id = R.string.video_header_subtitle),
                modifier = Modifier.padding(horizontal = 16.dp),
                onTrailingActionClicked = {
                    //TODO
                },
            )
        },
        hideIf = topShots.isNullOrEmpty(),
        modifier = modifier
    ) {
        VerticalGrid(
            items = topShots,
            columnCount = 2,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) { _, item ->
            VideoItem(
                video = item,
                onVideoClick = {
                    //
                }
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
    modifier: Modifier = Modifier
) {
    val reviewCount = if (reviews.size < MaxReviews) reviews.size else MaxReviews

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.reviews),
                modifier = Modifier.padding(horizontal = 16.dp),
                onTrailingActionClicked = {
                    //TODO
                }
            )
        },
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
            color = MaterialTheme.colors.surface.copy(alpha = if (isSystemInDarkTheme()) 0.32f else 1f),
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    val authorName = if (review.author.name.isNullOrEmpty())
                        stringResource(id = R.string.unknown) else review.author.name

                    /*Author*/
                    Text(
                        text = authorName,
                        style = MaterialTheme.typography.subtitle1.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colors.primary.copy(alpha = 0.16f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(horizontal = 4.dp)
                    )

                    /*Timestamp*/
                    review.displayCreatedAt?.let {
                        Text(text = it, style = MaterialTheme.typography.caption)
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
    modifier: Modifier = Modifier
) {
    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.casts),
                modifier = Modifier.padding(horizontal = 16.dp),
                onTrailingActionClicked = {
                    //TODO
                }
            )
        },
        headerContentSpacing = SectionContentHeaderSpacing,
        hideIf = casts.isNullOrEmpty(),
        modifier = modifier
    ) {
        HorizontalList(items = casts) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Avatar(
                    imageUrl = it.avatarImageUrl,
                    onClick = {
                        //TODO
                    },
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it.name, style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = it.character, style = MaterialTheme.typography.caption)
            }
        }
    }
}

@Composable
fun SimilarMoviesSection(
    movies: List<Movie>,
    openMovieDetails: (movieId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.similar_movies),
                modifier = Modifier.padding(horizontal = 16.dp),
                onTrailingActionClicked = {
                    //TODO
                }
            )
        },
        headerContentSpacing = SectionContentHeaderSpacing,
        hideIf = movies.isNullOrEmpty(),
        modifier = modifier
    ) {
        HorizontalList(items = movies) {
            MovieItem(
                title = it.title,
                posterImageUrl = it.posterImageUrl,
                modifier = Modifier.width(100.dp),
                onClick = { openMovieDetails(it.id) }
            )
        }
    }
}

private val ActionSize = 40.dp
private val SurfaceCornerRoundSize = 12.dp
private val SectionVerticalSpacing = 32.dp
private val SectionContentHeaderSpacing = 16.dp
private const val MaxImageShots = 9
private const val MaxVideoShots = 6
private const val MaxReviews = 3