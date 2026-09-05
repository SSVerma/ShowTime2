package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.ui.StatefulContent
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.MediaItemShimmer
import com.ssverma.shared.ui.component.media.MediaItemDefaults
import com.ssverma.shared.ui.component.media.SeeAllCard
import com.ssverma.shared.ui.component.media.UniversalMediaCard
import com.ssverma.shared.ui.component.media.asUniversalMediaItem
import com.ssverma.showtime.R

fun LazyListScope.trendingWorldwideShelf(
    isMoviePopularSelected: Boolean,
    popularMoviesState: UiState<List<AdInjectable<MoviePreview>>, MovieFailure>,
    popularTvShowsState: UiState<List<AdInjectable<TvShowPreview>>, TvShowFailure>,
    onTogglePopularType: (isMovie: Boolean) -> Unit,
    onMovieClick: (MoviePreview) -> Unit,
    onTvShowClick: (TvShowPreview) -> Unit,
    onSeeAllClick: () -> Unit,
    onAdLoaded: (InjectableAd, NativeAd) -> Unit,
    onRetry: () -> Unit,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null
) {
    item(key = "trending_worldwide_shelf") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            // 1. Header with Title on Left and Segmented Switcher [ Movie | TV Series ] on Right
            SectionHeader(
                title = stringResource(id = R.string.popular_section),
                leadingIcon = Icons.AutoMirrored.Rounded.TrendingUp,
                leadingIconContainerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                leadingIconTint = MaterialTheme.colorScheme.tertiary,
                titleTextStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                trailingContent = {
                    PopularSegmentedSwitcher(
                        isMovieSelected = isMoviePopularSelected,
                        onToggle = onTogglePopularType
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 10.dp)
            )

            // 2. Animated Media Carousel with Trailing See All Card
            AnimatedContent(
                targetState = isMoviePopularSelected,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally(
                            animationSpec = tween(
                                320,
                                easing = FastOutSlowInEasing
                            )
                        ) { -it / 4 } +
                                fadeIn(animationSpec = tween(320, easing = FastOutSlowInEasing)))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(
                                        260,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { it / 4 } +
                                        fadeOut(animationSpec = tween(200))
                            )
                    } else {
                        (slideInHorizontally(
                            animationSpec = tween(
                                320,
                                easing = FastOutSlowInEasing
                            )
                        ) { it / 4 } +
                                fadeIn(animationSpec = tween(320, easing = FastOutSlowInEasing)))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(
                                        260,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { -it / 4 } +
                                        fadeOut(animationSpec = tween(200))
                            )
                    }
                },
                label = "PopularWorldwideCarouselAnimation",
                modifier = Modifier.fillMaxWidth()
            ) { isMovie ->
                if (isMovie) {
                    StatefulContent(
                        state = popularMoviesState,
                        onRetry = onRetry,
                        loading = {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(5) {
                                    MediaItemShimmer()
                                }
                            }
                        }
                    ) { movies ->
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = movies,
                                key = { item ->
                                    when (item) {
                                        is InjectableAd -> item.id
                                        is InjectableContent<*> -> (item.item as MoviePreview).id
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

                                    is InjectableContent<*> -> {
                                        val movie =
                                            (injectableItem as InjectableContent<MoviePreview>).item
                                        UniversalMediaCard(
                                            item = movie.asUniversalMediaItem(),
                                            onClick = { onMovieClick(movie) },
                                            isGridView = true,
                                            onShowFeedback = onShowFeedback,
                                            modifier = Modifier.width(MediaItemDefaults.PosterWidth)
                                        )
                                    }
                                }
                            }
                            item(key = "see_all_popular_movies") {
                                SeeAllCard(
                                    onClick = onSeeAllClick
                                )
                            }
                        }
                    }
                } else {
                    StatefulContent(
                        state = popularTvShowsState,
                        onRetry = onRetry,
                        loading = {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(5) {
                                    MediaItemShimmer()
                                }
                            }
                        }
                    ) { tvShows ->
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = tvShows,
                                key = { item ->
                                    when (item) {
                                        is InjectableAd -> item.id
                                        is InjectableContent<*> -> (item.item as TvShowPreview).id
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

                                    is InjectableContent<*> -> {
                                        val tvShow =
                                            (injectableItem as InjectableContent<TvShowPreview>).item
                                        UniversalMediaCard(
                                            item = tvShow.asUniversalMediaItem(),
                                            onClick = { onTvShowClick(tvShow) },
                                            isGridView = true,
                                            onShowFeedback = onShowFeedback,
                                            modifier = Modifier.width(MediaItemDefaults.PosterWidth)
                                        )
                                    }
                                }
                            }
                            item(key = "see_all_popular_tv") {
                                SeeAllCard(
                                    onClick = onSeeAllClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PopularSegmentedSwitcher(
    isMovieSelected: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
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
            PopularSegmentItem(
                title = stringResource(id = R.string.movie_badge),
                selected = isMovieSelected,
                onClick = { onToggle(true) }
            )

            PopularSegmentItem(
                title = stringResource(id = R.string.tv_badge),
                selected = !isMovieSelected,
                onClick = { onToggle(false) }
            )
        }
    }
}

@Composable
private fun PopularSegmentItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "popular_segment_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "popular_segment_text"
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
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
