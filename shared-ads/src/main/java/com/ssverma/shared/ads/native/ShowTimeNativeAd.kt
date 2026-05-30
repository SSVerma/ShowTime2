package com.ssverma.shared.ads.native

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.ssverma.core.ads.ui.LocalAdConfigProvider
import com.ssverma.core.ads.ui.rememberNativeAd
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.component.MediaItemShimmer
import com.ssverma.shared.ui.component.MediaListItemShimmer
import com.ssverma.shared.ui.component.media.MediaItemDefaults

/**
 * State object to manage the native ad loading status.
 */
@Stable
class NativeAdState(initialLoaded: Boolean = false) {
    var isLoaded by mutableStateOf(initialLoaded)
}

@Composable
fun rememberNativeAdState(initialLoaded: Boolean = false): NativeAdState {
    return remember { NativeAdState(initialLoaded) }
}

@Composable
fun ShowTimeNativeAd(
    modifier: Modifier = Modifier,
    ad: NativeAd? = null,
    loadInternally: Boolean = ad == null,
    onAdLoaded: (NativeAd) -> Unit = {},
    state: NativeAdState = rememberNativeAdState(initialLoaded = ad != null),
    style: NativeAdStyle = NativeAdStyle.List,
    analyticsEventPrefix: String = "native_ad"
) {
    val adConfigProvider = LocalAdConfigProvider.current

    if (!adConfigProvider.isAdsEnabled) return

    // Sync state if a pre-loaded ad is passed in from outside (e.g., from a ViewModel)
    SideEffect {
        if (ad != null && !state.isLoaded) {
            state.isLoaded = true
        }
    }

    // Load a new ad internally if one wasn't provided
    val internallyLoadedAd = rememberNativeAd(
        loadAd = loadInternally,
        analyticsEventPrefix = analyticsEventPrefix,
        onAdLoaded = {
            state.isLoaded = true
            onAdLoaded(it)
        },
        onAdFailedToLoad = { state.isLoaded = false }
    )

    // Use whichever ad is available
    val activeAd = ad ?: internallyLoadedAd

    val rootModifier = if (style == NativeAdStyle.Carousel) {
        modifier.fillMaxSize()
    } else {
        modifier.fillMaxWidth()
    }

    Box(modifier = rootModifier) {
        // The actual ad Container - Only renders when an ad payload is ready
        if (activeAd != null) {
            NativeAdContainer(
                ad = activeAd,
                style = style,
                analyticsEventPrefix = analyticsEventPrefix
            )
        }

        // Shimmer overlay - fades out smoothly once ad is loaded
        AnimatedVisibility(
            visible = !state.isLoaded,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = if (style == NativeAdStyle.Carousel) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
        ) {
            NativeAdPlaceholder(style = style)
        }
    }
}

/**
 * The single AndroidView bridge that injects pure Jetpack Compose UI into AdMob's ecosystem.
 */
@Composable
private fun NativeAdContainer(
    ad: NativeAd,
    style: NativeAdStyle,
    analyticsEventPrefix: String
) {
    val containerModifier = if (style == NativeAdStyle.Carousel) {
        Modifier.fillMaxSize()
    } else {
        Modifier.fillMaxWidth()
    }

    AndroidView(
        modifier = containerModifier,
        factory = { ctx ->
            NativeAdView(ctx).apply {
                // We create ONE ComposeView to hold all our beautiful UI
                val composeView = ComposeView(ctx).apply {
                    layoutParams = if (style == NativeAdStyle.Carousel) {
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    } else {
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                }
                addView(composeView)

                // Transparent overlay to securely capture native touches for AdMob
                val clickOverlay = android.view.View(ctx).apply {
                    layoutParams = android.widget.FrameLayout.LayoutParams(
                        android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                        android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
                addView(clickOverlay)

                // THE MAGIC BULLET: Register the transparent overlay for clicks!
                this.callToActionView = clickOverlay
                this.headlineView = clickOverlay
                this.bodyView = clickOverlay
                this.iconView = clickOverlay
                // Note: AdMob automatically injects the "AdChoices" icon into the top-right corner.
            }
        },
        update = { view ->
            // Must set the ad before rendering content to register impressions
            view.setNativeAd(ad)

            val composeView = view.getChildAt(0) as ComposeView
            composeView.setContent {
                NativeAdViewContent(
                    nativeAd = ad,
                    style = style,
                    analyticsEventPrefix = analyticsEventPrefix,
                    onAdClicked = {
                        view.callToActionView?.performClick() ?: view.performClick()
                    }
                )
            }
        }
    )
}

@Composable
private fun NativeAdViewContent(
    nativeAd: NativeAd,
    style: NativeAdStyle,
    analyticsEventPrefix: String,
    onAdClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (style) {
            NativeAdStyle.List -> NativeAdListContent(nativeAd, onAdClicked)
            NativeAdStyle.Grid -> NativeAdGridContent(nativeAd, onAdClicked)
            NativeAdStyle.Carousel -> NativeAdCarouselContent(nativeAd, onAdClicked)
        }
    }
}

@Composable
private fun NativeAdPlaceholder(
    style: NativeAdStyle,
    modifier: Modifier = Modifier
) {
    when (style) {
        NativeAdStyle.Grid -> {
            MediaItemShimmer(modifier = modifier)
        }

        NativeAdStyle.List -> {
            MediaListItemShimmer(modifier = modifier)
        }

        NativeAdStyle.Carousel -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.extraLarge)
            ) {
                ShimmerPlaceholder(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun NativeAdListContent(
    nativeAd: NativeAd,
    onAdClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MediaItemDefaults.ListItemHeight)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.large
            )
            .clickable(onClick = onAdClicked)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AdPoster(
                nativeAd,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(TmdbPosterAspectRatio)
            )

            Column(
                modifier = Modifier
                    .padding(MaterialTheme.spacing.medium)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Text(
                    text = nativeAd.headline ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                AdStats(nativeAd)

                Text(
                    text = nativeAd.body ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )

                AdCTA(nativeAd, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

@Composable
private fun NativeAdGridContent(
    nativeAd: NativeAd,
    onAdClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(MediaItemDefaults.PosterWidth)
            .clickable(onClick = onAdClicked)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(TmdbPosterAspectRatio)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.large
                )
        ) {
            AdPoster(nativeAd, modifier = Modifier.fillMaxSize())

            AdCTA(
                nativeAd = nativeAd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(MaterialTheme.spacing.small)
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.small)
            )
        }

        Column(
            modifier = Modifier.padding(vertical = MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = nativeAd.headline ?: "",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            AdStats(nativeAd)
        }
    }
}

@Composable
private fun NativeAdCarouselContent(
    nativeAd: NativeAd,
    onAdClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.extraLarge
            )
            .clickable(onClick = onAdClicked)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageUrl = nativeAd.images.firstOrNull()?.uri?.toString()
                ?: nativeAd.icon?.uri?.toString()

            NetworkImage(
                url = imageUrl ?: "",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(MaterialTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
            ) {
                AdAttribution(modifier = Modifier.padding(bottom = 4.dp))

                Text(
                    text = nativeAd.headline ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = nativeAd.advertiser ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    AdCTA(nativeAd)
                }
            }
        }
    }
}

@Composable
private fun AdPoster(nativeAd: NativeAd, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        val imageUrl = nativeAd.images.firstOrNull()?.uri?.toString()
            ?: nativeAd.icon?.uri?.toString()

        if (imageUrl != null) {
            NetworkImage(
                url = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AdAttribution(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(MaterialTheme.spacing.small)
        )
    }
}

@Composable
private fun AdAttribution(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Ad",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AdStats(nativeAd: NativeAd) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val rating = nativeAd.starRating
        if (rating != null) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "%.1f".format(rating),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (nativeAd.advertiser != null) {
            if (nativeAd.starRating != null) {
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = nativeAd.advertiser ?: "",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdCTA(
    nativeAd: NativeAd,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    val cta = nativeAd.callToAction
    if (cta != null) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(containerColor)
                .padding(
                    horizontal = 12.dp,
                    vertical = 4.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cta,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
