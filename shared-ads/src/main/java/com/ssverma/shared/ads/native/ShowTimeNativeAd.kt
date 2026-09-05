package com.ssverma.shared.ads.native

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.ssverma.core.ads.ui.LocalAdConfigProvider
import com.ssverma.core.ads.ui.rememberNativeAd
import com.ssverma.core.image.DefaultImagePlaceHolder
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
    var isFailed by mutableStateOf(false)
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
            state.isFailed = false
            onAdLoaded(it)
        },
        onAdFailedToLoad = {
            state.isLoaded = false
            state.isFailed = true
        }
    )

    // Use whichever ad is available
    val activeAd = ad ?: internallyLoadedAd

    val rootModifier = when (style) {
        NativeAdStyle.Carousel -> modifier.fillMaxSize()
        NativeAdStyle.CircularLogo -> modifier
        else -> modifier.fillMaxWidth()
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

        // Shimmer overlay - fades out smoothly once ad is loaded, or disappears if ad loading failed
        AnimatedVisibility(
            visible = !state.isLoaded && !state.isFailed,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = when (style) {
                NativeAdStyle.Carousel -> Modifier.fillMaxSize()
                NativeAdStyle.CircularLogo -> Modifier.fillMaxSize()
                else -> Modifier.fillMaxWidth()
            }
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
    val containerModifier = when (style) {
        NativeAdStyle.Carousel -> Modifier.fillMaxSize()
        NativeAdStyle.CircularLogo -> Modifier.fillMaxSize()
        else -> Modifier.fillMaxWidth()
    }

    AndroidView(
        modifier = containerModifier,
        factory = { ctx ->
            NativeAdView(ctx).apply {
                // We create ONE ComposeView to hold all our beautiful UI
                val composeView = ComposeView(ctx).apply {
                    layoutParams = when (style) {
                        NativeAdStyle.Carousel,
                        NativeAdStyle.CircularLogo -> ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        else -> ViewGroup.LayoutParams(
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
            NativeAdStyle.CircularLogo -> NativeAdCircularLogoContent(nativeAd, onAdClicked)
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

        NativeAdStyle.CircularLogo -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .clip(CircleShape)
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
    Card(
        onClick = onAdClicked,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(MediaItemDefaults.ListItemHeight)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AdPoster(
                nativeAd,
                modifier = Modifier
                    .width(84.dp)
                    .fillMaxHeight(),
                showAttribution = true
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = nativeAd.headline ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    AdStats(nativeAd)

                    if (!nativeAd.body.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nativeAd.body ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

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
    Card(
        onClick = onAdClicked,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier.width(MediaItemDefaults.PosterWidth)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(TmdbPosterAspectRatio)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                AdPoster(nativeAd, modifier = Modifier.fillMaxSize(), showAttribution = false)

                // Top gradient scrim for high-contrast badge readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                )

                AdAttribution(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                )

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

            // Card Body (Headline, Subtitle/Advertiser, Trailing Action)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MediaItemDefaults.GridCardMetadataHeight)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = nativeAd.headline ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                ) {
                    Box(modifier = Modifier.weight(1f, fill = false)) {
                        AdStats(nativeAd)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
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
private fun AdPoster(
    nativeAd: NativeAd,
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true
) {
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

        if (showAttribution) {
            AdAttribution(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(MaterialTheme.spacing.small)
            )
        }
    }
}

@Composable
private fun AdAttribution(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color.Black.copy(alpha = 0.75f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "Ad",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
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
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (nativeAd.advertiser != null) {
            if (nativeAd.starRating != null) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = nativeAd.advertiser ?: "",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
private fun NativeAdCircularLogoContent(
    nativeAd: NativeAd,
    onAdClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                shadowElevation = 2.dp.toPx()
                shape = CircleShape
                clip = true
            }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .clickable(onClick = onAdClicked),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val adImageData = nativeAd.icon?.drawable
                ?: nativeAd.icon?.uri
                ?: nativeAd.images.firstOrNull()?.drawable
                ?: nativeAd.images.firstOrNull()?.uri

            if (adImageData != null) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(adImageData)
                        .crossfade(true)
                        .build(),
                    contentDescription = nativeAd.headline ?: "Ad",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { DefaultImagePlaceHolder() },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (nativeAd.headline ?: nativeAd.advertiser ?: "AD").take(2)
                                    .uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (nativeAd.headline ?: nativeAd.advertiser ?: "AD").take(2)
                            .uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Google AdMob Policy: Clear "Ad" badge overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 1.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 3.dp, vertical = 0.5.dp)
            ) {
                Text(
                    text = "Ad",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}
