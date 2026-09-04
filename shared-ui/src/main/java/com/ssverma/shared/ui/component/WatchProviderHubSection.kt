package com.ssverma.shared.ui.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.navigation.nav3.LocalNavAnimatedVisibilityScope
import com.ssverma.core.navigation.nav3.LocalSharedTransitionScope
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.core.ui.R as CoreUiR
import com.ssverma.shared.ui.R as SharedUiR

fun watchProviderSharedContentKey(providerId: Int, source: String = "default"): String =
    "watch_provider_logo_${providerId}_${source}"

@Composable
fun WatchProviderHubSection(
    providersUiState: UiState<List<ProviderInfo>, Failure.CoreFailure>,
    onProviderClick: (ProviderInfo) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isMovie: Boolean = true,
    source: String = if (isMovie) "movie_home" else "tv_home",
    headerTrailingContent: (@Composable () -> Unit)? = null,
    adSlotIndex: Int? = 2,
    adContent: (@Composable () -> Unit)? = null
) {
    if (providersUiState is UiState.Success && providersUiState.data.isEmpty()) {
        return
    }

    DriveCompose(
        uiState = providersUiState,
        onRetry = onRetry,
        loading = {
            WatchProviderShimmer(modifier = modifier)
        }
    ) { providers ->
        WatchProviderEntryCard(
            providers = providers,
            onProviderClick = onProviderClick,
            source = source,
            headerTrailingContent = headerTrailingContent,
            adSlotIndex = adSlotIndex,
            adContent = adContent,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WatchProviderEntryCard(
    providers: List<ProviderInfo>,
    onProviderClick: (ProviderInfo) -> Unit,
    source: String,
    headerTrailingContent: (@Composable () -> Unit)?,
    adSlotIndex: Int? = null,
    adContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAllSheet by remember { mutableStateOf(false) }

    val sheetGestureState = rememberNavigationEventState(
        currentInfo = remember { object : NavigationEventInfo() {} }
    )

    NavigationBackHandler(
        state = sheetGestureState,
        isBackEnabled = showAllSheet,
        onBackCompleted = {
            showAllSheet = false
        }
    )

    val uniqueProviders = remember(providers) {
        providers.distinctBy { it.providerId }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                shadowElevation = 4.dp.toPx()
                shape = RoundedCornerShape(24.dp)
                clip = true
            }
            .border(
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                SectionHeader(
                    title = stringResource(SharedUiR.string.streaming_universe),
                    titleTextStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    trailingContent = headerTrailingContent?.let { trailing ->
                        {
                            trailing()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (uniqueProviders.isNotEmpty()) {
                    val maxProviders = if (uniqueProviders.size > 8) {
                        if (adContent != null) 6 else 7
                    } else {
                        if (adContent != null) 7 else 8
                    }

                    val displayProviders = remember(uniqueProviders, maxProviders) {
                        uniqueProviders.take(maxProviders)
                    }

                    val showSeeAllButton = uniqueProviders.size > displayProviders.size

                    val slots = buildList<@Composable () -> Unit> {
                        var providerIndex = 0
                        for (i in 0 until 8) {
                            if (adSlotIndex != null && i == adSlotIndex && adContent != null) {
                                add { adContent() }
                            } else if (i == 7 && showSeeAllButton) {
                                add {
                                    SeeAllPill(
                                        totalCount = uniqueProviders.size,
                                        onClick = { showAllSheet = true }
                                    )
                                }
                            } else if (providerIndex < displayProviders.size) {
                                val provider = displayProviders[providerIndex++]
                                add {
                                    WatchProviderLogo(
                                        provider = provider,
                                        onClick = { onProviderClick(provider) },
                                        size = 52.dp,
                                        enableSharedTransition = true,
                                        sharedContentKey = watchProviderSharedContentKey(
                                            provider.providerId,
                                            source = source
                                        ),
                                        modifier = Modifier
                                            .graphicsLayer {
                                                shadowElevation = 2.dp.toPx()
                                                shape = CircleShape
                                                clip = true
                                            }
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                    alpha = 0.5f
                                                ),
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }

                    val row1 = slots.take(4)
                    val row2 = slots.drop(4)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 4) {
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (i < row1.size) {
                                        row1[i]()
                                    }
                                }
                            }
                        }

                        if (row2.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until 4) {
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (i < row2.size) {
                                            row2[i]()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAllSheet) {
        StreamingUniverseSheet(
            providers = providers,
            onProviderClick = onProviderClick,
            onDismiss = { showAllSheet = false }
        )
    }
}

@Composable
private fun SeeAllPill(
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remaining = (totalCount - 7).coerceAtLeast(1)
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        ),
        modifier = modifier.size(52.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "+$remaining",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(CoreUiR.string.see_all),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreamingUniverseSheet(
    providers: List<ProviderInfo>,
    onProviderClick: (ProviderInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val uniqueProviders = remember(providers) {
        providers.distinctBy { it.providerId }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val gridState = rememberLazyGridState()

    val isScrolled by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0
        }
    }

    val shadowAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        label = "ShadowAlpha"
    )

    ShowTimeBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val bottomPadding = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(80.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 88.dp,
                    bottom = 32.dp + bottomPadding
                )
            ) {
                items(uniqueProviders, key = { it.providerId }) { provider ->
                    Surface(
                        onClick = {
                            onProviderClick(provider)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            WatchProviderLogo(
                                provider = provider,
                                onClick = {
                                    onProviderClick(provider)
                                    onDismiss()
                                },
                                size = 64.dp,
                                enableSharedTransition = false,
                                modifier = Modifier
                                    .graphicsLayer {
                                        shadowElevation = 2.dp.toPx()
                                        shape = CircleShape
                                        clip = true
                                    }
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = provider.providerName,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Top Bar with title, subtitle, close button and dynamic elevation shadow
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(start = 24.dp, end = 16.dp, top = 4.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(SharedUiR.string.streaming_universe),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(SharedUiR.string.streaming_universe_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(SharedUiR.string.close)
                            )
                        }
                    }
                }

                // Dynamic bottom-only elevation shadow that fades in on scroll
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .graphicsLayer { alpha = shadowAlpha }
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun WatchProviderShimmer(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                shadowElevation = 4.dp.toPx()
                shape = RoundedCornerShape(24.dp)
                clip = true
            }
            .border(
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .width(160.dp)
                            .height(24.dp),
                        shape = RoundedCornerShape(4.dp)
                    )

                    ShimmerPlaceholder(
                        modifier = Modifier
                            .size(32.dp),
                        shape = CircleShape
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(4) {
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ShimmerPlaceholder(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                                CircleShape
                                            ),
                                        shape = CircleShape
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WatchProviderLogo(
    provider: ProviderInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    enableSharedTransition: Boolean = false,
    sharedContentKey: Any = "watch_provider_logo_${provider.providerId}"
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current

    val sharedModifier =
        if (enableSharedTransition && sharedTransitionScope != null && animatedVisibilityScope != null) {
            with(sharedTransitionScope) {
                Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = sharedContentKey),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ ->
                        spring(
                            dampingRatio = 0.8f,
                            stiffness = 380f
                        )
                    },
                    clipInOverlayDuringTransition = OverlayClip(CircleShape),
                    zIndexInOverlay = 1f
                )
            }
        } else {
            Modifier
        }

    Surface(
        onClick = onClick,
        shape = CircleShape,
        modifier = modifier
            .then(sharedModifier)
            .size(size),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        NetworkImage(
            url = provider.logoPath,
            contentDescription = provider.providerName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}


