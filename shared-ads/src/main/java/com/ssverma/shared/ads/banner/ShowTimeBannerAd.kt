package com.ssverma.shared.ads.banner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.ads.config.BannerAdSize
import com.ssverma.core.ads.ui.BannerAd
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.theme.spacing

@Stable
class BannerAdState {
    var isLoaded by mutableStateOf(false)
    var isFailed by mutableStateOf(false)
}

@Composable
fun rememberBannerAdState(): BannerAdState {
    return remember { BannerAdState() }
}

@Composable
fun ShowTimeBannerAd(
    modifier: Modifier = Modifier,
    state: BannerAdState = rememberBannerAdState(),
    adSize: BannerAdSize = BannerAdSize.Large,
    analyticsEventPrefix: String = "banner_ad"
) {
    // If the ad explicitly failed, completely collapse the space so we don't show an empty gap.
    if (state.isFailed) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring()), // Smoothly collapses ONLY if it fails
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.spacing.small)
                .border(
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                            )
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            val cardScope = this

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // The actual Ad Container
                Box(
                    modifier = Modifier.padding(MaterialTheme.spacing.extraSmall)
                ) {
                    BannerAd(
                        adSize = adSize,
                        analyticsEventPrefix = analyticsEventPrefix,
                        onAdLoaded = {
                            state.isLoaded = true
                            state.isFailed = false
                        },
                        onAdFailedToLoad = {
                            state.isLoaded = false
                            state.isFailed = true
                        }
                    )
                }

                // The Shimmer Placeholder (Reserves the exact height while loading!)
                cardScope.AnimatedVisibility(
                    visible = !state.isLoaded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    // AdMob's internal size definitions are already in DP, so this maps perfectly.
                    val adHeight = adSize.adMobSize.height.dp

                    ShimmerPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(adHeight)
                            .padding(MaterialTheme.spacing.extraSmall),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Ad Attribution Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .clip(RoundedCornerShape(bottomStart = 6.dp, topEnd = 12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(bottomStart = 6.dp, topEnd = 12.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Ad",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
