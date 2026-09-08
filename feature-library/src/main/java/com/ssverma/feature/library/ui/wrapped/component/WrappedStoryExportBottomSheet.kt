package com.ssverma.feature.library.ui.wrapped.component

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.theme.spacing
import com.ssverma.core.ui.util.findActivity
import com.ssverma.feature.library.R
import com.ssverma.feature.library.util.ShareImageHelper
import com.ssverma.feature.payment.ui.FeatureQuotaGateBottomSheet
import com.ssverma.shared.domain.model.stats.WrappedYearSummary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedStoryExportBottomSheet(
    summary: WrappedYearSummary,
    selectedStyle: WrappedStoryStyle,
    isProActive: Boolean,
    isPassActive: Boolean,
    isExporting: Boolean,
    isProPaymentEnabled: Boolean,
    isGateOpen: Boolean,
    onStyleSelected: (WrappedStoryStyle) -> Unit,
    onAttemptExport: (onAllowed: () -> Unit) -> Unit,
    onDismissGate: () -> Unit,
    onWatchAdForPass: (Activity) -> Unit,
    onOpenProPaywall: () -> Unit,
    onDismissRequest: () -> Unit,
    onSetExporting: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    userName: String? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()

    val saveSuccess = stringResource(R.string.wrapped_save_success)
    val saveFailed = stringResource(R.string.wrapped_save_failed)
    val chooserTitle = stringResource(R.string.wrapped_share_chooser)

    ShowTimeBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = MaterialTheme.spacing.large)
        ) {
            // Style Selector Header
            Text(
                text = stringResource(R.string.wrapped_style_label),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )

            // Style Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = MaterialTheme.spacing.medium, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WrappedStoryStyle.entries.forEach { style ->
                    val isSelected = selectedStyle == style
                    val label = when (style) {
                        WrappedStoryStyle.CLASSIC_VELVET -> stringResource(R.string.wrapped_style_classic)
                        WrappedStoryStyle.OLED_NOIR -> stringResource(R.string.wrapped_style_oled)
                        WrappedStoryStyle.NEON_CYBERPUNK -> stringResource(R.string.wrapped_style_cyberpunk)
                        WrappedStoryStyle.GOLDEN_VIP -> stringResource(R.string.wrapped_style_gold)
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onStyleSelected(style) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(label)
                                if (style.isProOnly && !isProActive && !isPassActive) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Live Story Card Preview (Captured into Bitmap)
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
                WrappedStoryCardView(
                    summary = summary,
                    userName = userName,
                    style = selectedStyle,
                    showWatermark = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Action Buttons (Save & Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        onAttemptExport {
                            coroutineScope.launch {
                                onSetExporting(true)
                                try {
                                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                    val success = ShareImageHelper.saveBitmapToGallery(
                                        context = context,
                                        bitmap = bitmap,
                                        title = "ShowTime_Wrapped_${
                                            summary.yearLabel.replace(
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
                                } catch (_: Exception) {
                                } finally {
                                    onSetExporting(false)
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.wrapped_save_story_card),
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        onAttemptExport {
                            coroutineScope.launch {
                                onSetExporting(true)
                                try {
                                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                    ShareImageHelper.shareBitmap(
                                        context = context,
                                        bitmap = bitmap,
                                        chooserTitle = chooserTitle
                                    )
                                } catch (_: Exception) {
                                } finally {
                                    onSetExporting(false)
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isExporting) {
                        ShowTimeLoadingIndicator(modifier = Modifier.size(18.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.wrapped_share_story_card),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Gate Bottom Sheet
    if (isGateOpen) {
        FeatureQuotaGateBottomSheet(
            title = stringResource(R.string.wrapped_gate_title),
            description = stringResource(R.string.wrapped_gate_desc),
            rewardActionLabel = stringResource(R.string.wrapped_watch_ad_pass),
            onWatchAdClick = {
                val activity = context.findActivity()
                if (activity != null) {
                    onWatchAdForPass(activity)
                }
            },
            onUpgradeProClick = {
                onDismissGate()
                onOpenProPaywall()
            },
            onDismissRequest = onDismissGate,
            isProPaymentEnabled = isProPaymentEnabled,
            icon = Icons.Rounded.AutoAwesome
        )
    }
}
