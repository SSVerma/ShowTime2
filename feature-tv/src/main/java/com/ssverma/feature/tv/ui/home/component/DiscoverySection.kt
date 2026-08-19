package com.ssverma.feature.tv.ui.home.component

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
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.account.ui.stats.MediaStatsAction
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.feature.tv.ui.common.TvShowPreviewUiState
import com.ssverma.feature.tv.ui.list.component.TvIndicator
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.MediaItemShimmer
import com.ssverma.shared.ui.component.media.TvShowGridItem
import com.ssverma.core.ui.R as CoreUiR

data class DiscoveryCategory(
    @param:StringRes val titleRes: Int,
    val config: TvShowListingConfig,
    val uiState: TvShowPreviewUiState
)

@Composable
fun DiscoverySection(
    popularTvShowsState: TvShowPreviewUiState,
    topRatedTvShowsState: TvShowPreviewUiState,
    upcomingTvShowsState: TvShowPreviewUiState,
    onFetchPopular: () -> Unit,
    onFetchTopRated: () -> Unit,
    onFetchUpcoming: () -> Unit,
    onTvShowClicked: (tvShow: TvShowPreview) -> Unit,
    onSeeAllClicked: (TvShowListingRoute) -> Unit,
    onWatchProviderClick: (provider: ProviderInfo) -> Unit,
    onAdLoaded: (InjectableAd, com.google.android.gms.ads.nativead.NativeAd) -> Unit,
    modifier: Modifier = Modifier,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val categories = remember(popularTvShowsState, topRatedTvShowsState, upcomingTvShowsState) {
        listOf(
            DiscoveryCategory(
                titleRes = R.string.popular,
                config = TvShowListingConfig.Filterable.Popular(),
                uiState = popularTvShowsState
            ),
            DiscoveryCategory(
                titleRes = R.string.top_rated,
                config = TvShowListingConfig.Filterable.TopRated(),
                uiState = topRatedTvShowsState
            ),
            DiscoveryCategory(
                titleRes = R.string.upcoming,
                config = TvShowListingConfig.Filterable.Upcoming(),
                uiState = upcomingTvShowsState
            )
        )
    }

    LaunchedEffect(selectedTabIndex) {
        when (categories[selectedTabIndex].config) {
            is TvShowListingConfig.Filterable.Popular -> onFetchPopular()
            is TvShowListingConfig.Filterable.TopRated -> onFetchTopRated()
            is TvShowListingConfig.Filterable.Upcoming -> onFetchUpcoming()
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
                label = "DiscoverTvSeeAllAction"
            ) { tabIndex ->
                val currentCategory = categories[tabIndex]

                Surface(
                    onClick = {
                        val args = when (currentCategory.config) {
                            is TvShowListingConfig.Filterable.Popular -> TvShowListingArgs.Popular(
                                titleRes = currentCategory.titleRes
                            )

                            is TvShowListingConfig.Filterable.TopRated -> TvShowListingArgs.TopRated(
                                titleRes = currentCategory.titleRes
                            )

                            is TvShowListingConfig.Filterable.Upcoming -> TvShowListingArgs.Upcoming(
                                titleRes = currentCategory.titleRes
                            )

                            else -> TvShowListingArgs.Popular(titleRes = currentCategory.titleRes)
                        }
                        onSeeAllClicked(TvShowListingRoute(args = args))
                    },
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
                            text = stringResource(CoreUiR.string.see_all),
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
            label = "DiscoveryTvContentTransition"
        ) { index ->
            val category = categories[index]

            DriveCompose(
                uiState = category.uiState,
                loading = { DiscoveryLoadingPlaceholder() },
                onRetry = {
                    when (category.config) {
                        is TvShowListingConfig.Filterable.Popular -> onFetchPopular()
                        is TvShowListingConfig.Filterable.TopRated -> onFetchTopRated()
                        is TvShowListingConfig.Filterable.Upcoming -> onFetchUpcoming()
                        else -> {}
                    }
                }
            ) { tvShows ->
                HorizontalLazyList(
                    items = tvShows,
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.large),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                ) { injectableItem ->
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
                            val tvShowPreview = (element as InjectableContent<TvShowPreview>).item
                            TvShowGridItem(
                                tvShow = tvShowPreview,
                                showRating = category.config !is TvShowListingConfig.Filterable.Upcoming && category.config !is TvShowListingConfig.Filterable.TopRated,
                                indicator = { preview ->
                                    TvIndicator(config = category.config, tvShow = preview)
                                },
                                onClick = { preview -> onTvShowClicked(preview) },
                                overlayContent = {
                                    MediaStatsAction(
                                        mediaType = MediaType.Tv,
                                        mediaId = tvShowPreview.id,
                                        title = tvShowPreview.title,
                                        posterImageUrl = tvShowPreview.posterImageUrl,
                                        backdropImageUrl = tvShowPreview.backdropImageUrl,
                                        voteAvg = tvShowPreview.voteAvg,
                                        releaseDate = tvShowPreview.displayFirstAirDate.orEmpty(),
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
