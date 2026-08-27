package com.ssverma.shared.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.theme.spacing

object CarouselDefaults {
    val HeroMaxItemWidth = Dp.Unspecified
    val HeroItemHeight = 220.dp
    val SmallItemMaskWidth = 56.dp
}

/**
 * Stateful version: Handles Loading/Error states via DriveCompose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T, FF> AppHeroCarousel(
    uiState: UiState<List<T>, FF>,
    carouselState: CarouselState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    maxItemWidth: Dp = CarouselDefaults.HeroMaxItemWidth,
    itemHeight: Dp = CarouselDefaults.HeroItemHeight,
    itemSpacing: Dp = MaterialTheme.spacing.medium,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    key: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    DriveCompose(
        uiState = uiState,
        loading = {
            // Shimmer exactly mimics the Centered Hero Spec: [Small] - [Large] - [Small]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Small Mask
                ShimmerPlaceholder(
                    modifier = Modifier
                        .height(itemHeight)
                        .width(CarouselDefaults.SmallItemMaskWidth),
                    shape = MaterialTheme.shapes.extraLarge
                )

                // Center Large Item (Takes up remaining space just like Dp.Unspecified)
                val centerModifier = if (maxItemWidth == Dp.Unspecified) {
                    Modifier.weight(1f)
                } else {
                    Modifier.width(maxItemWidth)
                }

                ShimmerPlaceholder(
                    modifier = Modifier
                        .height(itemHeight)
                        .then(centerModifier),
                    shape = MaterialTheme.shapes.extraLarge
                )

                // Right Small Mask
                ShimmerPlaceholder(
                    modifier = Modifier
                        .height(itemHeight)
                        .width(CarouselDefaults.SmallItemMaskWidth),
                    shape = MaterialTheme.shapes.extraLarge
                )
            }
        },
        onRetry = onRetry
    ) { items ->
        AppHeroCarousel(
            items = items,
            carouselState = carouselState,
            modifier = modifier,
            maxItemWidth = maxItemWidth,
            itemHeight = itemHeight,
            itemSpacing = itemSpacing,
            contentPadding = contentPadding,
            key = key,
            itemContent = itemContent
        )
    }
}

/**
 * Stateless version: Purely for rendering a list of items.
 * Uses HorizontalCenteredHeroCarousel for the official M3 Hero Spec.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AppHeroCarousel(
    items: List<T>,
    carouselState: CarouselState,
    modifier: Modifier = Modifier,
    maxItemWidth: Dp = CarouselDefaults.HeroMaxItemWidth,
    itemHeight: Dp = CarouselDefaults.HeroItemHeight,
    itemSpacing: Dp = MaterialTheme.spacing.medium,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    itemShape: Shape = MaterialTheme.shapes.extraLarge,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(itemHeight)
    ) {
        HorizontalCenteredHeroCarousel(
            state = carouselState,
            maxItemWidth = maxItemWidth,
            itemSpacing = itemSpacing,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        ) { index ->
            val item = items[index]
            val itemKey = key?.invoke(item) ?: index

            // THE STABILITY FIX: We wrap the item in a Compose key block.
            // This prevents the flickering loop when AdMob payloads update the ViewModel!
            key(itemKey) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // maskClip is MANDATORY for M3 Carousels to apply the squish animation.
                        // It applies to standard content AND injected AdMob views beautifully.
                        .maskClip(itemShape)
                ) {
                    itemContent(item)
                }
            }
        }
    }
}

/**
 * Standalone component for rendering standard movie/tv show content inside the Carousel.
 * Extracted so the Carousel can remain generic for AdMob injection.
 */
@Composable
fun HeroItem(
    title: String,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    formatBadge: String? = null,
    releaseDate: String? = null,
    voteAvg: Float = 0f,
    itemShape: Shape = MaterialTheme.shapes.extraLarge,
    overlayContent: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(itemShape)
            .clickable(onClick = onClick)
    ) {
        NetworkImage(
            url = imageUrl,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loadingPlaceholder = {
                ShimmerPlaceholder(
                    modifier = Modifier.fillMaxSize(),
                    shape = itemShape
                )
            },
            errorPlaceholder = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        )

        // Gradient Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 100f
                    )
                )
        )

        // Content Info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!releaseDate.isNullOrEmpty() || voteAvg > 0f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (!releaseDate.isNullOrEmpty()) {
                        Text(
                            text = releaseDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    if (voteAvg > 0f) {
                        Text(
                            text = "★ ${String.format("%.1f", voteAvg)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }

        // Format Badge (e.g. Movie / TV)
        if (formatBadge != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = formatBadge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Overlay Action (Watchlist/Favorite)
        if (overlayContent != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                overlayContent()
            }
        }
    }
}
