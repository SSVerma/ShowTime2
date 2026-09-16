package com.ssverma.feature.library.ui.receipt

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.library.R
import com.ssverma.feature.library.domain.model.ReceiptSnapshot
import com.ssverma.feature.library.domain.model.ReceiptSource
import com.ssverma.feature.library.domain.model.ReceiptStyle
import com.ssverma.feature.library.ui.receipt.component.CinemaReceiptEmptyState
import com.ssverma.feature.library.ui.receipt.component.CinemaReceiptStyleSelector
import com.ssverma.feature.library.util.ShareImageHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaReceiptBottomSheet(
    snapshot: ReceiptSnapshot?,
    selectedStyle: ReceiptStyle,
    onStyleSelected: (ReceiptStyle) -> Unit,
    selectedSource: ReceiptSource,
    onSourceSelected: (ReceiptSource) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    isCustomCollection: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val scrollState = rememberScrollState()
    var isExporting by remember { mutableStateOf(false) }

    val handleDismiss: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            onDismiss()
        }
    }

    // 1. Detect if content extends/scrolls below the bottom action bar
    val isContentBelowBar = scrollState.canScrollForward

    val topShadowAlpha by animateFloatAsState(
        targetValue = if (isContentBelowBar) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "ReceiptTopShadowAlpha"
    )

    ShowTimeBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Scrollable Content flowing edge-to-edge behind the bottom bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row (padded)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.cinema_receipt),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = stringResource(R.string.receipt_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = handleDismiss
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // Source Selector (if not inside custom collection)
                if (!isCustomCollection) {
                    Text(
                        text = stringResource(R.string.receipt_source),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.spacing.medium)
                    )

                    val chipColors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Edge-to-edge horizontal chip scroll
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = MaterialTheme.spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        val isHistory = selectedSource == ReceiptSource.HISTORY
                        FilterChip(
                            selected = isHistory,
                            onClick = { onSourceSelected(ReceiptSource.HISTORY) },
                            label = {
                                Text(
                                    text = stringResource(R.string.receipt_period_history),
                                    fontWeight = if (isHistory) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = CircleShape,
                            colors = chipColors,
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isHistory,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                selectedBorderColor = Color.Transparent
                            )
                        )

                        val isFavorites = selectedSource == ReceiptSource.FAVORITES
                        FilterChip(
                            selected = isFavorites,
                            onClick = { onSourceSelected(ReceiptSource.FAVORITES) },
                            label = {
                                Text(
                                    text = stringResource(R.string.receipt_period_favorites),
                                    fontWeight = if (isFavorites) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = CircleShape,
                            colors = chipColors,
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isFavorites,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                selectedBorderColor = Color.Transparent
                            )
                        )

                        val isWatchlist = selectedSource == ReceiptSource.WATCHLIST
                        FilterChip(
                            selected = isWatchlist,
                            onClick = { onSourceSelected(ReceiptSource.WATCHLIST) },
                            label = {
                                Text(
                                    text = stringResource(R.string.receipt_period_watchlist),
                                    fontWeight = if (isWatchlist) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Bookmark,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = CircleShape,
                            colors = chipColors,
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isWatchlist,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))
                }

                // Style Selector Chips
                CinemaReceiptStyleSelector(
                    selectedStyle = selectedStyle,
                    isProActive = true,
                    isPassActive = true,
                    onStyleSelected = onStyleSelected
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // Live Receipt Preview (Recorded to GraphicsLayer for sharing)
                if (snapshot != null && snapshot.items.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.spacing.medium)
                            .drawWithContent {
                                graphicsLayer.record {
                                    this@drawWithContent.drawContent()
                                }
                                drawLayer(graphicsLayer)
                            }
                    ) {
                        CinemaReceiptView(
                            snapshot = snapshot,
                            style = selectedStyle,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    CinemaReceiptEmptyState(
                        selectedSource = selectedSource,
                        selectedCustomListTitle = null
                    )
                }

                // Bottom spacer ensuring all receipt content scrolls completely above the lifted bottom action bar
                Spacer(modifier = Modifier.height(88.dp))
            }

            // Lifted Bottom Bar (Seamless Surface color matching sheet + Upward Top Shadow)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                // Upward top shadow on top of the bar (stable GPU layer, zero layout shift)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-3).dp)
                        .graphicsLayer { alpha = topShadowAlpha }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.06f)
                                )
                            )
                        )
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    val shareTitle = stringResource(R.string.receipt_share_chooser_title)
                    val saveSuccess = stringResource(R.string.receipt_saved_success)
                    val saveFailed = stringResource(R.string.receipt_save_failed)

                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f * topShadowAlpha)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = MaterialTheme.spacing.medium,
                                    end = MaterialTheme.spacing.medium,
                                    top = MaterialTheme.spacing.small,
                                    bottom = MaterialTheme.spacing.medium
                                ),
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                        ) {
                            Button(
                                onClick = {
                                    if (snapshot == null || snapshot.items.isEmpty()) return@Button
                                    isExporting = true
                                    coroutineScope.launch {
                                        try {
                                            val bitmap =
                                                graphicsLayer.toImageBitmap().asAndroidBitmap()
                                            ShareImageHelper.shareBitmap(
                                                context = context,
                                                bitmap = bitmap,
                                                chooserTitle = shareTitle
                                            )
                                        } finally {
                                            isExporting = false
                                        }
                                    }
                                },
                                enabled = !isExporting && snapshot != null && snapshot.items.isNotEmpty(),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isExporting) {
                                    ShowTimeLoadingIndicator(
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                }
                                Text(
                                    text = stringResource(R.string.receipt_share),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    if (snapshot == null || snapshot.items.isEmpty()) return@OutlinedButton
                                    isExporting = true
                                    coroutineScope.launch {
                                        try {
                                            val bitmap =
                                                graphicsLayer.toImageBitmap().asAndroidBitmap()
                                            val success = ShareImageHelper.saveBitmapToGallery(
                                                context = context,
                                                bitmap = bitmap,
                                                title = "ShowTime_Receipt_${
                                                    snapshot.title.replace(
                                                        " ",
                                                        "_"
                                                    )
                                                }"
                                            )
                                            Toast.makeText(
                                                context,
                                                if (success) saveSuccess else saveFailed,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } finally {
                                            isExporting = false
                                        }
                                    }
                                },
                                enabled = !isExporting && snapshot != null && snapshot.items.isNotEmpty(),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.outlinedButtonColors(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                Text(
                                    text = stringResource(R.string.receipt_save),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

