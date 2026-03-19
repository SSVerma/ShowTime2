package com.ssverma.feature.filter.ui.hub.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.layout.HorizontalLazyListSection
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.filter.ui.hub.MediaPreview
import com.ssverma.feature.filter.ui.hub.config.MovieHubDiscoverConfig
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.ui.component.AppHeroCarousel
import com.ssverma.shared.ui.component.GenreItem
import com.ssverma.shared.ui.component.MediaItemRowShimmer
import com.ssverma.shared.ui.component.WatchProviderHubBranding
import com.ssverma.shared.ui.component.WatchProviderLogo
import com.ssverma.shared.ui.component.media.MovieGridItem
import com.ssverma.shared.ui.component.media.TvShowGridItem
import com.ssverma.shared.ui.R as SharedUiR

@Composable
fun WatchProviderHubContent(
    provider: ProviderInfo,
    heroItems: List<MediaPreview>,
    newItems: List<MediaPreview>,
    todayItems: List<MediaPreview>,
    topRatedItems: List<MediaPreview>,
    genres: List<Genre>,
    isMovieMode: Boolean,
    onMediaClick: (Any) -> Unit,
    onGenreClick: (Genre) -> Unit,
    onBackClick: () -> Unit,
    onMovieSeeAllClick: (MovieDiscoverConfig) -> Unit,
    onTvSeeAllClick: (TvDiscoverConfig) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val scrollState = rememberLazyListState()
    val brandingColor = WatchProviderHubBranding.getBrandingColor(provider.providerId)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Parallax Fixed Header (Cinematic Backdrop)
        ParallaxHeader(
            heroItems = heroItems,
            scrollState = scrollState,
            isLoading = isLoading
        )

        // 2. Main Content
        Scaffold(
            topBar = {
                StickyGlassBar(
                    provider = provider,
                    scrollState = scrollState,
                    onBackClick = onBackClick
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    bottom = 80.dp
                )
            ) {
                // Parallax Spacing
                item {
                    Spacer(modifier = Modifier.height(280.dp))
                }

                // Brand Identity Section
                item {
                    BrandIdentitySection(
                        provider = provider,
                        brandingColor = brandingColor,
                        isLoading = isLoading
                    )
                }

                // Hero Pager Section
                item {
                    HeroPagerSection(
                        items = heroItems,
                        isLoading = isLoading,
                        onItemClick = onMediaClick,
                        modifier = Modifier.padding(bottom = MaterialTheme.spacing.large)
                    )
                }

                // Curated Row: New This Week
                item {
                    HubSectionRow(
                        title = stringResource(SharedUiR.string.new_this_week),
                        items = newItems,
                        isLoading = isLoading,
                        onItemClick = onMediaClick,
                        onSeeAllClick = {
                            if (isMovieMode) {
                                onMovieSeeAllClick(
                                    MovieHubDiscoverConfig.newReleases(providerId = provider.providerId)
                                )
                            } else {
                                // TODO
                            }
                        },
                    )
                }

                // Curated Row: Upcoming
                item {
                    HubSectionRow(
                        title = stringResource(SharedUiR.string.upcoming),
                        items = todayItems,
                        isLoading = isLoading,
                        onItemClick = onMediaClick,
                        onSeeAllClick = {
                            if (isMovieMode) {
                                onMovieSeeAllClick(
                                    MovieHubDiscoverConfig.upcoming(providerId = provider.providerId)
                                )
                            } else {
                                // TODO
                            }
                        },
                    )
                }

                // Curated Row: Top Rated (Hidden Gems)
                item {
                    HubSectionRow(
                        title = stringResource(SharedUiR.string.top_rated_gems),
                        items = topRatedItems,
                        isLoading = isLoading,
                        onItemClick = onMediaClick,
                        onSeeAllClick = {
                            if (isMovieMode) {
                                onMovieSeeAllClick(
                                    MovieHubDiscoverConfig.topRated(providerId = provider.providerId)
                                )
                            } else {
                                // TODO
                            }
                        },
                    )
                }

                if (!isLoading) {
                    item {
                        EndOfContentSection(
                            providerName = provider.providerName,
                            genres = genres,
                            onGenreClick = onGenreClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickyGlassBar(
    provider: ProviderInfo,
    scrollState: LazyListState,
    onBackClick: () -> Unit
) {
    val isScrolled by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || (scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset > 200) }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isScrolled) 0.95f else 0f,
        label = "GlassAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isScrolled) MaterialTheme.colorScheme.surface.copy(alpha = alpha)
                else Color.Transparent
            )
            .statusBarsPadding()
    ) {
        CenterAlignedTopAppBar(
            title = {
                AnimatedVisibility(
                    visible = isScrolled,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WatchProviderLogo(
                            provider = provider,
                            onClick = { /* Already on Hub */ },
                            size = 32.dp,
                            modifier = Modifier.clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = provider.providerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onBackClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isScrolled) Color.Transparent else Color.Black.copy(
                            alpha = 0.3f
                        ),
                        contentColor = if (isScrolled) MaterialTheme.colorScheme.onSurface else Color.White
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(SharedUiR.string.close)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun ParallaxHeader(
    heroItems: List<MediaPreview>,
    scrollState: LazyListState,
    isLoading: Boolean
) {
    val backdropUrl = when (val firstItem = heroItems.firstOrNull()) {
        is MediaPreview.Movie -> firstItem.movie.backdropImageUrl
        is MediaPreview.TvShow -> firstItem.tvShow.backdropImageUrl
        null -> ""
    }

    val density = LocalDensity.current
    val headerHeight = 450.dp
    val headerHeightPx = with(density) { headerHeight.toPx() }

    val parallaxOffset = remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex == 0) {
                scrollState.firstVisibleItemScrollOffset.toFloat() * 0.6f
            } else {
                headerHeightPx
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .graphicsLayer {
                translationY = -parallaxOffset.value
                alpha = (1.2f - (parallaxOffset.value / (headerHeightPx * 0.8f))).coerceIn(0f, 1f)
            }
    ) {
        Crossfade(targetState = isLoading, label = "BackdropFade") { loading ->
            if (loading) {
                ShimmerPlaceholder(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(0.dp)
                )
            } else {
                NetworkImage(
                    url = backdropUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Darkened overlay for better contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
    }
}

@Composable
fun BrandIdentitySection(
    provider: ProviderInfo,
    brandingColor: Color,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.large)
            .padding(bottom = MaterialTheme.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WatchProviderLogo(
            provider = provider,
            onClick = { /* Identity Logo Not Usually Clickable */ },
            size = 100.dp,
            modifier = Modifier
                .graphicsLayer {
                    shadowElevation = 24.dp.toPx()
                    shape = CircleShape
                    clip = true
                }
                .background(Color.White, CircleShape)
                .padding(4.dp)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        val title = (provider.providerName + " Universe").uppercase()

        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall.copy(
                letterSpacing = 4.sp,
                lineHeight = 44.sp
            ),
            fontWeight = FontWeight.Black,
            color = brandingColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

        Text(
            text = stringResource(SharedUiR.string.streaming_exclusively_on, provider.providerName),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroPagerSection(
    items: List<MediaPreview>,
    isLoading: Boolean,
    onItemClick: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(SharedUiR.string.featured_originals),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.large,
                vertical = MaterialTheme.spacing.medium
            )
        )

        Crossfade(targetState = isLoading, label = "HeroFade") { loading ->
            if (loading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.large),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                ) {
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .weight(1f)
                            .height(200.dp),
                        shape = MaterialTheme.shapes.large
                    )
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .weight(0.3f)
                            .height(200.dp),
                        shape = MaterialTheme.shapes.large
                    )
                }
            } else if (items.isNotEmpty()) {
                val carouselState = rememberCarouselState { items.size }
                AppHeroCarousel(
                    items = items,
                    carouselState = carouselState,
                    itemHeight = 220.dp,
                    maxItemWidth = 340.dp,
                    imageUrl = { item ->
                        when (item) {
                            is MediaPreview.Movie -> item.movie.backdropImageUrl
                            is MediaPreview.TvShow -> item.tvShow.backdropImageUrl
                        }
                    },
                    title = { item ->
                        when (item) {
                            is MediaPreview.Movie -> item.movie.title
                            is MediaPreview.TvShow -> item.tvShow.title
                        }
                    },
                    onItemClick = { item ->
                        when (item) {
                            is MediaPreview.Movie -> onItemClick(item.movie)
                            is MediaPreview.TvShow -> onItemClick(item.tvShow)
                        }
                    },
                    overlayContent = null // Hidden in Hub
                )
            }
        }
    }
}

@Composable
fun HubSectionRow(
    title: String,
    items: List<MediaPreview>,
    isLoading: Boolean,
    onItemClick: (Any) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Column(modifier = modifier) {
            SectionHeader(
                title = title,
                modifier = Modifier.padding(start = MaterialTheme.spacing.large)
            )
            MediaItemRowShimmer(
                contentPadding = PaddingValues(
                    horizontal = MaterialTheme.spacing.large,
                    vertical = MaterialTheme.spacing.medium
                ),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                itemWidth = 150.dp
            )
        }
    } else if (items.isNotEmpty()) {
        HorizontalLazyListSection(
            items = items,
            headerContentSpacing = MaterialTheme.spacing.small,
            sectionHeader = {
                SectionHeader(
                    title = title,
                    onTrailingActionClicked = onSeeAllClick,
                    modifier = Modifier.padding(start = MaterialTheme.spacing.large)
                )
            },
            itemContent = { item ->
                when (item) {
                    is MediaPreview.Movie -> {
                        MovieGridItem(
                            movie = item.movie,
                            onClick = { onItemClick(item.movie) },
                            overlayContent = null,
                            modifier = Modifier.width(150.dp)
                        )
                    }

                    is MediaPreview.TvShow -> {
                        TvShowGridItem(
                            tvShow = item.tvShow,
                            onClick = { onItemClick(item.tvShow) },
                            overlayContent = null,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }
            },
            contentPadding = PaddingValues(
                horizontal = MaterialTheme.spacing.large,
                vertical = MaterialTheme.spacing.medium
            ),
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EndOfContentSection(
    providerName: String,
    genres: List<Genre>,
    onGenreClick: (Genre) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, bottom = 48.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            modifier = Modifier
                .width(64.dp)
                .padding(bottom = MaterialTheme.spacing.large),
            thickness = 4.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )

        Text(
            text = stringResource(SharedUiR.string.end_of_curated_content),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Text(
            text = stringResource(SharedUiR.string.looking_for_more, providerName),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        Text(
            text = stringResource(SharedUiR.string.explore_by_genre),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            genres.forEach { genre ->
                GenreItem(
                    genre = genre,
                    onGenreClicked = { onGenreClick(genre) }
                )
            }
        }
    }
}
