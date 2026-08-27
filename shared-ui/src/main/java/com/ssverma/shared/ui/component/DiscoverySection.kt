package com.ssverma.shared.ui.component

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.component.media.SeeAllCard
import com.ssverma.core.ui.R as CoreUiR

data class DiscoveryCategory<T, out R>(
    @param:StringRes val titleRes: Int,
    val payload: R,
    val uiState: UiState<List<T>, *>,
    val onFetchData: () -> Unit
)

@Composable
fun <T, R> DiscoverySection(
    categories: List<DiscoveryCategory<T, R>>,
    onSeeAllClicked: (R) -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = false,
    headerTitle: String = stringResource(id = R.string.discover),
    itemContent: @Composable (categoryPayload: R, item: T) -> Unit
) {
    if (categories.isEmpty()) return

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val safeTabIndex = selectedTabIndex.coerceIn(0, categories.lastIndex)
    val currentCategory = categories[safeTabIndex]

    LaunchedEffect(safeTabIndex) {
        currentCategory.onFetchData()
    }

    Column(modifier = modifier) {
        // 1. Optional Header with "Discover" title on the left and animated tinted "See All" pill on the right
        if (showHeader) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {
                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                AnimatedContent(
                    targetState = safeTabIndex,
                    transitionSpec = {
                        (fadeIn(tween(200)) + slideInHorizontally { it / 4 })
                            .togetherWith(fadeOut(tween(150)) + slideOutHorizontally { -it / 4 })
                    },
                    label = "DiscoverySectionHeaderSeeAllAction"
                ) { tabIndex ->
                    val tabCategory = categories[tabIndex]

                    Surface(
                        onClick = { onSeeAllClicked(tabCategory.payload) },
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
        }

        // 2. Full-Width Segmented Capsule Pill Selector
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .padding(
                    top = if (showHeader) MaterialTheme.spacing.small else 0.dp,
                    bottom = MaterialTheme.spacing.medium
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEachIndexed { index, category ->
                    val isSelected = safeTabIndex == index
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
                            .weight(1f)
                            .clip(CircleShape)
                            .background(backgroundColor, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedTabIndex = index
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(category.titleRes),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 3. Smooth Directional Content Transitions
        AnimatedContent(
            targetState = safeTabIndex,
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
                onRetry = { category.onFetchData() }
            ) { items ->
                LazyRow(
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium),
                ) {
                    items(items) { item ->
                        itemContent(category.payload, item)
                    }

                    item(key = "discovery_see_all_${category.titleRes}") {
                        SeeAllCard(
                            onClick = { onSeeAllClicked(category.payload) }
                        )
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
            .padding(horizontal = MaterialTheme.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
    ) {
        repeat(3) { MediaItemShimmer() }
    }
}
