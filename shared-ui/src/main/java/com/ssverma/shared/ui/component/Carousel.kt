package com.ssverma.shared.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.theme.spacing

object CarouselDefaults {
    val HeroMaxItemWidth = Dp.Unspecified
    val HeroItemHeight = 200.dp
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
    contentPadding: PaddingValues = PaddingValues(horizontal = MaterialTheme.spacing.large),
    imageUrl: (T) -> String,
    title: (T) -> String,
    onItemClick: (T) -> Unit
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
            imageUrl = imageUrl,
            title = title,
            onItemClick = onItemClick
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
    contentPadding: PaddingValues = PaddingValues(horizontal = MaterialTheme.spacing.large),
    itemShape: Shape = MaterialTheme.shapes.extraLarge,
    overlayGradient: Brush = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
    ),
    imageUrl: (T) -> String,
    title: (T) -> String,
    onItemClick: (T) -> Unit
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .maskClip(itemShape)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        ),
                        shape = itemShape
                    )
                    .clickable { onItemClick(item) }
            ) {
                NetworkImage(
                    url = imageUrl(item),
                    contentDescription = title(item),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Info Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(overlayGradient)
                        .padding(MaterialTheme.spacing.medium)
                ) {
                    Text(
                        text = title(item),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
