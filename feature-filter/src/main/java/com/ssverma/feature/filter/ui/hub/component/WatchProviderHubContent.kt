package com.ssverma.feature.filter.ui.hub.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.ripple
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
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.account.ui.stats.MediaStatsAction
import com.ssverma.feature.filter.ui.hub.MediaPreview
import com.ssverma.feature.filter.ui.hub.config.MovieHubDiscoverConfig
import com.ssverma.feature.filter.ui.hub.config.TvHubDiscoverConfig
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.AppHeroCarousel
import com.ssverma.shared.ui.component.HeroItem
import com.ssverma.shared.ui.component.MediaItemRowShimmer
import com.ssverma.shared.ui.component.WatchProviderHubBranding
import com.ssverma.shared.ui.component.WatchProviderLogo
import com.ssverma.shared.ui.component.media.MovieGridItem
import com.ssverma.shared.ui.component.media.SeeAllCard
import com.ssverma.shared.ui.component.media.TvShowGridItem
import com.ssverma.shared.ui.component.watchProviderSharedContentKey
import com.ssverma.shared.ui.R as SharedUiR

@Composable
fun WatchProviderHubContent(
    provider: ProviderInfo,
    heroItems: List<AdInjectable<MediaPreview>>,
    newItems: List<AdInjectable<MediaPreview>>,
    upcomingItems: List<AdInjectable<MediaPreview>>,
    topRatedItems: List<AdInjectable<MediaPreview>>,
    genres: List<Genre>,
    isMovieMode: Boolean,
    onToggleMode: (isMovie: Boolean) -> Unit,
    onMovieClick: (MoviePreview) -> Unit,
    onTvShowClick: (TvShowPreview) -> Unit,
    onGenreClick: (Genre) -> Unit,
    onBackClick: () -> Unit,
    onMovieSeeAllClick: (MovieDiscoverConfig) -> Unit,
    onTvSeeAllClick: (TvDiscoverConfig) -> Unit,
    onAdLoaded: (InjectableAd, NativeAd) -> Unit,
    modifier: Modifier = Modifier,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null,
    isLoading: Boolean = false
) {
    val scrollState = rememberLazyListState()
    val brandingColor = WatchProviderHubBranding.getBrandingColor(provider.providerId)
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

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

        // 2. Main Scrollable Content
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = MaterialTheme.spacing.extraLarge + bottomInset
            )
        ) {
            // Parallax Spacing Spacer: positions brand logo smoothly across the backdrop
            item(key = "parallax_spacer") {
                Spacer(modifier = Modifier.height(140.dp))
            }

            // Brand Identity Section with Movie / TV Switcher
            item(key = "brand_identity") {
                BrandIdentitySection(
                    provider = provider,
                    brandingColor = brandingColor,
                    isMovieMode = isMovieMode,
                    onToggleMode = onToggleMode
                )
            }

            // Hero Pager Section ("Featured Originals")
            item(key = "hero_pager") {
                HeroPagerSection(
                    items = heroItems,
                    isLoading = isLoading,
                    onMovieClick = onMovieClick,
                    onTvShowClick = onTvShowClick,
                    onAdLoaded = onAdLoaded,
                    onShowFeedback = onShowFeedback,
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                )
            }

            // Curated Row: New This Week
            item(key = "new_this_week") {
                HubSectionRow(
                    title = stringResource(SharedUiR.string.new_this_week),
                    items = newItems,
                    isLoading = isLoading,
                    onMovieClick = onMovieClick,
                    onTvShowClick = onTvShowClick,
                    onAdLoaded = onAdLoaded,
                    onShowFeedback = onShowFeedback,
                    onSeeAllClick = {
                        if (isMovieMode) {
                            onMovieSeeAllClick(
                                MovieHubDiscoverConfig.newReleases(providerId = provider.providerId)
                            )
                        } else {
                            onTvSeeAllClick(
                                TvHubDiscoverConfig.newReleases(providerId = provider.providerId)
                            )
                        }
                    },
                    modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
                )
            }

            // Curated Row: Upcoming
            item(key = "upcoming") {
                HubSectionRow(
                    title = stringResource(SharedUiR.string.upcoming),
                    items = upcomingItems,
                    isLoading = isLoading,
                    onMovieClick = onMovieClick,
                    onTvShowClick = onTvShowClick,
                    onAdLoaded = onAdLoaded,
                    onShowFeedback = onShowFeedback,
                    onSeeAllClick = {
                        if (isMovieMode) {
                            onMovieSeeAllClick(
                                MovieHubDiscoverConfig.upcoming(providerId = provider.providerId)
                            )
                        } else {
                            onTvSeeAllClick(
                                TvHubDiscoverConfig.upcoming(providerId = provider.providerId)
                            )
                        }
                    },
                    modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
                )
            }

            // Curated Row: Top Rated (Hidden Gems)
            item(key = "top_rated") {
                HubSectionRow(
                    title = stringResource(SharedUiR.string.top_rated_gems),
                    items = topRatedItems,
                    isLoading = isLoading,
                    onMovieClick = onMovieClick,
                    onTvShowClick = onTvShowClick,
                    onAdLoaded = onAdLoaded,
                    onShowFeedback = onShowFeedback,
                    onSeeAllClick = {
                        if (isMovieMode) {
                            onMovieSeeAllClick(
                                MovieHubDiscoverConfig.topRated(providerId = provider.providerId)
                            )
                        } else {
                            onTvSeeAllClick(
                                TvHubDiscoverConfig.topRated(providerId = provider.providerId)
                            )
                        }
                    },
                    modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
                )
            }

            // Explore by Genre
            if (!isLoading && genres.isNotEmpty()) {
                item(key = "genres") {
                    EndOfContentSection(
                        providerName = provider.providerName,
                        genres = genres,
                        onGenreClick = onGenreClick,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.large)
                    )
                }
            }
        }

        // 3. Sticky Glass Top Bar
        StickyGlassBar(
            provider = provider,
            scrollState = scrollState,
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StickyGlassBar(
    provider: ProviderInfo,
    scrollState: LazyListState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isScrolled by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || (scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset > 250)
        }
    }

    val elevation by animateDpAsState(
        targetValue = if (isScrolled) 4.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 280,
            easing = FastOutSlowInEasing
        ),
        label = "TopBarElevation"
    )

    val surfaceAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        animationSpec = tween(
            durationMillis = 280,
            easing = FastOutSlowInEasing
        ),
        label = "SurfaceAlpha"
    )

    val backButtonContainerColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isScrolled) Color.Transparent else Color.Black.copy(alpha = 0.35f),
        animationSpec = tween(durationMillis = 250),
        label = "BackButtonContainerColor"
    )

    val backButtonContentColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isScrolled) MaterialTheme.colorScheme.onSurface else Color.White,
        animationSpec = tween(durationMillis = 250),
        label = "BackButtonContentColor"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = surfaceAlpha),
            shadowElevation = elevation,
            tonalElevation = if (isScrolled) 2.dp else 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isScrolled,
                            enter = fadeIn(animationSpec = tween(220)),
                            exit = fadeOut(animationSpec = tween(180))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                WatchProviderLogo(
                                    provider = provider,
                                    onClick = { },
                                    size = 28.dp,
                                    enableSharedTransition = false,
                                    modifier = Modifier.clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = provider.providerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = backButtonContainerColor,
                                contentColor = backButtonContentColor
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(SharedUiR.string.close)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
private fun ParallaxHeader(
    heroItems: List<AdInjectable<MediaPreview>>,
    scrollState: LazyListState,
    isLoading: Boolean
) {
    val firstContent =
        heroItems.firstOrNull { it is InjectableContent } as? InjectableContent<MediaPreview>
    val backdropUrl = firstContent?.item?.backdropImageUrl.orEmpty()

    val density = LocalDensity.current
    val headerHeight = 360.dp
    val headerHeightPx = with(density) { headerHeight.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .graphicsLayer {
                val offset = if (scrollState.firstVisibleItemIndex == 0) {
                    scrollState.firstVisibleItemScrollOffset.toFloat() * 0.55f
                } else {
                    headerHeightPx
                }
                translationY = -offset
                alpha = (1.1f - (offset / (headerHeightPx * 0.8f))).coerceIn(0f, 1f)
            }
    ) {
        if (isLoading) {
            ShimmerPlaceholder(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp)
            )
        } else if (backdropUrl.isNotEmpty()) {
            NetworkImage(
                url = backdropUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Darkened gradient overlay for smooth blend into background
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
private fun BrandIdentitySection(
    provider: ProviderInfo,
    brandingColor: Color,
    isMovieMode: Boolean,
    onToggleMode: (isMovie: Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
            .padding(bottom = MaterialTheme.spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WatchProviderLogo(
            provider = provider,
            onClick = { },
            size = 80.dp,
            enableSharedTransition = true,
            sharedContentKey = watchProviderSharedContentKey(
                provider.providerId,
                isMovie = isMovieMode
            ),
            modifier = Modifier
                .graphicsLayer {
                    shadowElevation = 16.dp.toPx()
                    shape = CircleShape
                    clip = true
                }
                .background(Color.White, CircleShape)
                .padding(3.dp)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        Text(
            text = stringResource(SharedUiR.string.provider_universe, provider.providerName),
            style = MaterialTheme.typography.titleLarge.copy(
                letterSpacing = 2.sp,
                lineHeight = 28.sp
            ),
            fontWeight = FontWeight.Black,
            color = brandingColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

        Text(
            text = stringResource(SharedUiR.string.streaming_exclusively_on, provider.providerName),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Mode Switcher: [ 🎬 Movies | 📺 TV Shows ]
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        ) {
            Row(
                modifier = Modifier.padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HubSegmentItem(
                    title = stringResource(id = SharedUiR.string.movies),
                    selected = isMovieMode,
                    onClick = { onToggleMode(true) }
                )

                HubSegmentItem(
                    title = stringResource(id = SharedUiR.string.tv_series),
                    selected = !isMovieMode,
                    onClick = { onToggleMode(false) }
                )
            }
        }
    }
}

@Composable
private fun HubSegmentItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "hub_segment_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "hub_segment_text"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroPagerSection(
    items: List<AdInjectable<MediaPreview>>,
    isLoading: Boolean,
    onMovieClick: (MoviePreview) -> Unit,
    onTvShowClick: (TvShowPreview) -> Unit,
    onAdLoaded: (InjectableAd, NativeAd) -> Unit,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(SharedUiR.string.featured_originals),
            titleTextStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            hideTrailingAction = true,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
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
                contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium)
            ) { injectableItem ->
                when (injectableItem) {
                    is InjectableAd -> {
                        ShowTimeNativeAd(
                            ad = injectableItem.ad,
                            onAdLoaded = { ad -> onAdLoaded(injectableItem, ad) },
                            style = injectableItem.style
                        )
                    }

                    is InjectableContent<MediaPreview> -> {
                        val media = injectableItem.item
                        HeroItem(
                            title = media.title,
                            imageUrl = media.backdropImageUrl,
                            onClick = {
                                when (media) {
                                    is MediaPreview.Movie -> onMovieClick(media.movie)
                                    is MediaPreview.TvShow -> onTvShowClick(media.tvShow)
                                }
                            },
                            overlayContent = {
                                val isMovie = media is MediaPreview.Movie
                                MediaStatsAction(
                                    mediaType = if (isMovie) MediaType.Movie else MediaType.Tv,
                                    mediaId = media.id,
                                    title = media.title,
                                    posterImageUrl = media.posterImageUrl,
                                    backdropImageUrl = media.backdropImageUrl,
                                    voteAvg = media.voteAvg,
                                    releaseDate = media.displayDate.orEmpty(),
                                    containerColor = MaterialTheme.colorScheme.surface.copy(
                                        alpha = 0.85f
                                    ),
                                    onShowFeedback = onShowFeedback,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HubSectionRow(
    title: String,
    items: List<AdInjectable<MediaPreview>>,
    isLoading: Boolean,
    onMovieClick: (MoviePreview) -> Unit,
    onTvShowClick: (TvShowPreview) -> Unit,
    onAdLoaded: (InjectableAd, NativeAd) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null
) {
    if (isLoading) {
        Column(modifier = modifier) {
            SectionHeader(
                title = title,
                titleTextStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                hideTrailingAction = true,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            MediaItemRowShimmer(
                contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
            )
        }
    } else if (items.isNotEmpty()) {
        Column(modifier = modifier) {
            SectionHeader(
                title = title,
                titleTextStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                hideTrailingAction = true,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            LazyRow(
                contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = items,
                    key = { item ->
                        when (item) {
                            is InjectableAd -> item.id
                            is InjectableContent<MediaPreview> -> item.item.id
                        }
                    }
                ) { injectableItem ->
                    when (injectableItem) {
                        is InjectableAd -> {
                            ShowTimeNativeAd(
                                ad = injectableItem.ad,
                                onAdLoaded = { ad -> onAdLoaded(injectableItem, ad) },
                                style = injectableItem.style
                            )
                        }

                        is InjectableContent<MediaPreview> -> {
                            when (val media = injectableItem.item) {
                                is MediaPreview.Movie -> {
                                    MovieGridItem(
                                        movie = media.movie,
                                        onClick = onMovieClick,
                                        overlayContent = {
                                            MediaStatsAction(
                                                mediaType = MediaType.Movie,
                                                mediaId = media.movie.id,
                                                title = media.movie.title,
                                                posterImageUrl = media.movie.posterImageUrl,
                                                backdropImageUrl = media.movie.backdropImageUrl,
                                                voteAvg = media.movie.voteAvg,
                                                releaseDate = media.movie.displayReleaseDate.orEmpty(),
                                                containerColor = MaterialTheme.colorScheme.surface.copy(
                                                    alpha = 0.85f
                                                ),
                                                onShowFeedback = onShowFeedback,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    )
                                }

                                is MediaPreview.TvShow -> {
                                    TvShowGridItem(
                                        tvShow = media.tvShow,
                                        onClick = onTvShowClick,
                                        overlayContent = {
                                            MediaStatsAction(
                                                mediaType = MediaType.Tv,
                                                mediaId = media.tvShow.id,
                                                title = media.tvShow.title,
                                                posterImageUrl = media.tvShow.posterImageUrl,
                                                backdropImageUrl = media.tvShow.backdropImageUrl,
                                                voteAvg = media.tvShow.voteAvg,
                                                releaseDate = media.tvShow.displayFirstAirDate.orEmpty(),
                                                containerColor = MaterialTheme.colorScheme.surface.copy(
                                                    alpha = 0.85f
                                                ),
                                                onShowFeedback = onShowFeedback,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "see_all_$title") {
                    SeeAllCard(onClick = onSeeAllClick)
                }
            }
        }
    }
}

@Composable
private fun EndOfContentSection(
    providerName: String,
    genres: List<Genre>,
    onGenreClick: (Genre) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
    ) {
        SectionHeader(
            title = stringResource(SharedUiR.string.explore_by_genre),
            subtitle = stringResource(SharedUiR.string.looking_for_more, providerName),
            titleTextStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            hideTrailingAction = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        LazyHorizontalStaggeredGrid(
            rows = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(0.dp),
            horizontalItemSpacing = MaterialTheme.spacing.small,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
        ) {
            items(genres, key = { it.id }) { genre ->
                AssistChip(
                    onClick = { onGenreClick(genre) },
                    label = {
                        Text(
                            text = genre.name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    shape = MaterialTheme.shapes.small,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        borderWidth = 1.dp
                    )
                )
            }
        }
    }
}
