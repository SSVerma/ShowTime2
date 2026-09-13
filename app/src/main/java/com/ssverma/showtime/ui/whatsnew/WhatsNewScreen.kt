package com.ssverma.showtime.ui.whatsnew

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.component.ShowTimeLogo
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.feature.CinephileFeature
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.whatsnew.component.WhatsNewFeatureCard
import com.ssverma.showtime.ui.whatsnew.component.WhatsNewPageIndicator
import kotlinx.coroutines.launch

@Composable
fun WhatsNewScreen(
    features: List<WhatsNewFeature>,
    onTryFeature: (WhatsNewFeature) -> Unit,
    onFinishTour: () -> Unit,
    onSkipTour: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { features.size })
    var exploredFeatureIds by rememberSaveable { mutableStateOf(emptySet<String>()) }

    val currentFeature = features.getOrNull(pagerState.currentPage) ?: features.first()

    val auraTargetColor = when (currentFeature.feature) {
        CinephileFeature.MY_LISTS -> MaterialTheme.colorScheme.primary
        CinephileFeature.DISCOVERY -> MaterialTheme.colorScheme.secondary
        CinephileFeature.COMMUNITY_LISTS -> MaterialTheme.colorScheme.tertiary
        CinephileFeature.MOVIE_MATCH -> MaterialTheme.colorScheme.error
        CinephileFeature.TASTE_PROFILE -> MaterialTheme.colorScheme.secondary
        CinephileFeature.DAILY_GAME -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }

    val animatedAuraColor by animateColorAsState(
        targetValue = auraTargetColor.copy(alpha = 0.18f),
        animationSpec = tween(durationMillis = 500),
        label = "whats_new_aura"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Atmospheric Ambient Light Glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            animatedAuraColor,
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = MaterialTheme.spacing.medium)
        ) {
            // 1. Top Bar with Brand & Progress Spotlight Tag
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.spacing.large,
                        vertical = MaterialTheme.spacing.small
                    )
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ShowTimeLogo(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.smallMedium))
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(onClick = onSkipTour) {
                        Text(
                            text = stringResource(id = R.string.whats_new_skip),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                // Spotlight Step Badge
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = stringResource(
                                id = R.string.whats_new_spotlight_tag,
                                pagerState.currentPage + 1,
                                features.size
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Explored pill
                    val isCurrentExplored = currentFeature.feature.id in exploredFeatureIds
                    AnimatedVisibility(
                        visible = isCurrentExplored,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(id = R.string.whats_new_explored_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 2. Fixed-Height Feature Showcase Carousel
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                WhatsNewFeatureCard(
                    feature = features[page],
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Bottom Action Dock
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.large)
            ) {
                // Page indicator with explored checkmarks
                val exploredFeatureSet = features
                    .map { it.feature }
                    .filter { it.id in exploredFeatureIds }
                    .toSet()

                WhatsNewPageIndicator(
                    pageCount = features.size,
                    currentPage = pagerState.currentPage,
                    features = features,
                    exploredFeatures = exploredFeatureSet,
                    onIndicatorClick = { targetPage ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(targetPage)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.mediumLarge))

                val isLastPage = pagerState.currentPage == features.size - 1
                val isExplored = currentFeature.feature.id in exploredFeatureIds
                val featureTitle = stringResource(id = currentFeature.titleRes)

                if (isLastPage) {
                    // Final Page Primary CTA: Explore ShowTime 2.0
                    Button(
                        onClick = onFinishTour,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.whats_new_get_started),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

                    // Final Page Secondary Action: Try Daily Game
                    TextButton(
                        onClick = {
                            exploredFeatureIds = exploredFeatureIds + currentFeature.feature.id
                            onTryFeature(currentFeature)
                        },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = stringResource(
                                id = if (isExplored) {
                                    R.string.whats_new_revisit_feature
                                } else {
                                    R.string.whats_new_try_feature
                                },
                                featureTitle
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Spotlight Pages Primary CTA: Try Feature
                    Button(
                        onClick = {
                            exploredFeatureIds = exploredFeatureIds + currentFeature.feature.id
                            if (pagerState.currentPage < features.size - 1) {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(pagerState.currentPage + 1)
                                }
                            }
                            onTryFeature(currentFeature)
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = stringResource(
                                id = if (isExplored) {
                                    R.string.whats_new_revisit_feature
                                } else {
                                    R.string.whats_new_try_feature
                                },
                                featureTitle
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

                    // Spotlight Pages Secondary Action: Next
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.whats_new_next),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
