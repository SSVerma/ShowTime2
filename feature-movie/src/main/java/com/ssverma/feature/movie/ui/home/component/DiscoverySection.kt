package com.ssverma.feature.movie.ui.home.component

import androidx.annotation.StringRes
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.convertor.asMovieListingConfig
import com.ssverma.feature.movie.ui.common.MoviePreviewUiState
import com.ssverma.feature.movie.ui.list.component.MovieIndicator
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.MediaItemShimmer
import com.ssverma.shared.ui.component.WatchProviderTrigger
import com.ssverma.shared.ui.component.WatchProviderTriggerVariant
import com.ssverma.shared.ui.component.media.MovieGridItem

data class DiscoveryCategory(
    @param:StringRes val titleRes: Int,
    val route: MovieListingArgs,
    val uiState: MoviePreviewUiState
)

@Composable
fun DiscoverySection(
    popularMoviesState: MoviePreviewUiState,
    topRatedMoviesState: MoviePreviewUiState,
    upcomingMoviesState: MoviePreviewUiState,
    onFetchPopular: () -> Unit,
    onFetchTopRated: () -> Unit,
    onFetchUpcoming: () -> Unit,
    onMovieClicked: (movie: MoviePreview) -> Unit,
    onSeeAllClicked: (MovieListingArgs) -> Unit,
    onWatchProviderClick: (provider: ProviderInfo) -> Unit,
    onAdLoaded: (InjectableAd, NativeAd) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val categories = remember(popularMoviesState, topRatedMoviesState, upcomingMoviesState) {
        listOf(
            DiscoveryCategory(
                titleRes = R.string.popuplar,
                route = MovieListingArgs.Popular(titleRes = R.string.popuplar),
                uiState = popularMoviesState
            ),
            DiscoveryCategory(
                titleRes = R.string.top_rated,
                route = MovieListingArgs.TopRated(titleRes = R.string.top_rated),
                uiState = topRatedMoviesState
            ),
            DiscoveryCategory(
                titleRes = R.string.upcoming,
                route = MovieListingArgs.Upcoming(titleRes = R.string.upcoming),
                uiState = upcomingMoviesState
            )
        )
    }

    LaunchedEffect(selectedTabIndex) {
        when (categories[selectedTabIndex].route) {
            is MovieListingArgs.Popular -> onFetchPopular()
            is MovieListingArgs.TopRated -> onFetchTopRated()
            is MovieListingArgs.Upcoming -> onFetchUpcoming()
            else -> {}
        }
    }

    Column(modifier = modifier) {
        // 1. Header with "Discover" title on the left and animated tinted "See All" pill on the right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.large)
        ) {
            Text(
                text = stringResource(R.string.discover),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    (fadeIn(tween(200)) + slideInHorizontally { it / 4 })
                        .togetherWith(fadeOut(tween(150)) + slideOutHorizontally { -it / 4 })
                },
                label = "DiscoverSeeAllAction"
            ) { tabIndex ->
                val currentCategory = categories[tabIndex]

                Surface(
                    onClick = { onSeeAllClicked(currentCategory.route) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            start = 12.dp,
                            end = 8.dp,
                            top = 6.dp,
                            bottom = 6.dp
                        )
                    ) {
                        Text(
                            text = stringResource(com.ssverma.core.ui.R.string.see_all),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        // 2. Segmented Capsule Pill Selector
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.large)
                .padding(top = MaterialTheme.spacing.small, bottom = MaterialTheme.spacing.medium)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEachIndexed { index, category ->
                    val isSelected = selectedTabIndex == index
                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        label = "PillBg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        label = "PillText"
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(backgroundColor, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedTabIndex = index
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(category.titleRes),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = contentColor
                        )
                    }
                }
            }
        }

        // 3. Smooth Directional Content Transitions
        AnimatedContent(
            targetState = selectedTabIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { width -> width / 4 } + fadeIn(tween(220)))
                        .togetherWith(slideOutHorizontally { width -> -width / 4 } + fadeOut(
                            tween(
                                180
                            )
                        ))
                } else {
                    (slideInHorizontally { width -> -width / 4 } + fadeIn(tween(220)))
                        .togetherWith(slideOutHorizontally { width -> width / 4 } + fadeOut(
                            tween(
                                180
                            )
                        ))
                }
            },
            label = "DiscoveryContentTransition"
        ) { index ->
            val category = categories[index]

            DriveCompose(
                uiState = category.uiState,
                loading = { DiscoveryLoadingPlaceholder() },
                onRetry = {
                    when (category.route) {
                        is MovieListingArgs.Popular -> onFetchPopular()
                        is MovieListingArgs.TopRated -> onFetchTopRated()
                        is MovieListingArgs.Upcoming -> onFetchUpcoming()
                        else -> {}
                    }
                }
            ) { movies ->
                LazyRow(
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.large),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
                    items(movies) { injectableItem ->
                        when (val element = injectableItem) {
                            is InjectableAd -> {
                                ShowTimeNativeAd(
                                    ad = element.ad,
                                    onAdLoaded = { ad -> onAdLoaded(element, ad) },
                                    style = element.style
                                )
                            }

                            is InjectableContent<*> -> {
                                @Suppress("UNCHECKED_CAST")
                                val moviePreview = (element as InjectableContent<MoviePreview>).item
                                MovieGridItem(
                                    movie = moviePreview,
                                    onClick = onMovieClicked,
                                    indicator = {
                                        MovieIndicator(
                                            config = category.route.asMovieListingConfig(),
                                            movie = it
                                        )
                                    },
                                    overlayContent = {
                                        WatchProviderTrigger(
                                            mediaId = moviePreview.id,
                                            isMovie = true,
                                            variant = WatchProviderTriggerVariant.Icon,
                                            onWatchProviderClick = onWatchProviderClick,
                                            modifier = Modifier.padding(MaterialTheme.spacing.small)
                                        )
                                    }
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
private fun DiscoveryLoadingPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        repeat(3) { MediaItemShimmer() }
    }
}
