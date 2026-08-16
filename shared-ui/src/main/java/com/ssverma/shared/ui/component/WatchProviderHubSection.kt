package com.ssverma.shared.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
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
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.navigation.nav3.LocalSharedTransitionScope
import com.ssverma.core.ui.StatefulContent
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.ui.R as SharedUiR

fun watchProviderSharedContentKey(providerId: Int, isMovie: Boolean): String =
    "watch_provider_logo_${providerId}_${if (isMovie) "movie" else "tv"}"

@Composable
fun WatchProviderHubSection(
    providersUiState: UiState<List<ProviderInfo>, Failure.CoreFailure>,
    onProviderClick: (ProviderInfo) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isMovie: Boolean = true
) {
    if (providersUiState is UiState.Success && providersUiState.data.isEmpty()) {
        return
    }

    StatefulContent(
        state = providersUiState,
        onRetry = onRetry,
        loading = {
            WatchProviderShimmer(modifier = modifier)
        }
    ) { providers ->
        WatchProviderEntryCard(
            providers = providers,
            onProviderClick = onProviderClick,
            isMovie = isMovie,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun WatchProviderEntryCard(
    providers: List<ProviderInfo>,
    onProviderClick: (ProviderInfo) -> Unit,
    isMovie: Boolean,
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                shape = RoundedCornerShape(28.dp)
                clip = true
            }
            .border(
                border = BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4285F4),
                            Color(0xFF9B72CB),
                            Color(0xFFD96570),
                            Color(0xFFF4AF5F)
                        )
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
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
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(SharedUiR.string.streaming_universe),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(SharedUiR.string.streaming_universe_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (providers.isNotEmpty()) {
                        FilledIconButton(
                            onClick = { showAllSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = stringResource(SharedUiR.string.see_all)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (providers.isNotEmpty()) {
                    val displayProviders = remember(providers) {
                        providers.distinctBy { it.providerId }.take(10)
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        maxItemsInEachRow = 5
                    ) {
                        displayProviders.forEach { provider ->
                            WatchProviderLogo(
                                provider = provider,
                                onClick = { onProviderClick(provider) },
                                size = 56.dp,
                                enableSharedTransition = true,
                                sharedContentKey = watchProviderSharedContentKey(
                                    provider.providerId,
                                    isMovie
                                ),
                                modifier = Modifier
                                    .graphicsLayer {
                                        shadowElevation = 4.dp.toPx()
                                        shape = CircleShape
                                        clip = true
                                    }
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        CircleShape
                                    )
                            )
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = {
            androidx.compose.foundation.layout.WindowInsets.safeDrawing.only(
                androidx.compose.foundation.layout.WindowInsetsSides.Top
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = false
        )
    ) {
        BackHandler(enabled = true) {
            onDismiss()
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val bottomPadding =
                androidx.compose.foundation.layout.WindowInsets.navigationBars.asPaddingValues()
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onProviderClick(provider)
                                onDismiss()
                            }
                            .padding(4.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WatchProviderShimmer(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                shape = RoundedCornerShape(28.dp)
                clip = true
            }
            .border(
                border = BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4285F4),
                            Color(0xFF9B72CB),
                            Color(0xFFD96570),
                            Color(0xFFF4AF5F)
                        )
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
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
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                ShimmerPlaceholder(
                    modifier = Modifier
                        .width(200.dp)
                        .height(28.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerPlaceholder(
                    modifier = Modifier
                        .width(260.dp)
                        .height(18.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 5
                ) {
                    repeat(10) {
                        ShimmerPlaceholder(
                            modifier = Modifier
                                .size(56.dp)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WatchProviderLogo(
    provider: ProviderInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 64.dp,
    enableSharedTransition: Boolean = false,
    sharedContentKey: Any = "watch_provider_logo_${provider.providerId}"
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedContentScope.current

    val sharedModifier =
        if (enableSharedTransition && sharedTransitionScope != null) {
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
        modifier = modifier
            .then(sharedModifier)
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick),
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
