package com.ssverma.feature.library.ui.receipt

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.ui.Screen
import com.ssverma.core.ui.theme.spacing
import com.ssverma.core.ui.util.findActivity
import com.ssverma.feature.library.R
import com.ssverma.feature.library.analytics.LibraryAnalyticsScreenName
import com.ssverma.feature.library.analytics.receipt.CinemaReceiptAnalyticsEvent
import com.ssverma.feature.library.ui.receipt.component.CinemaReceiptBottomActions
import com.ssverma.feature.library.ui.receipt.component.CinemaReceiptEmptyState
import com.ssverma.feature.library.ui.receipt.component.CinemaReceiptPersonalizeDialog
import com.ssverma.feature.library.ui.receipt.component.CinemaReceiptPersonalizeSection
import com.ssverma.feature.library.ui.receipt.component.CinemaReceiptSourceSelector
import com.ssverma.feature.library.ui.receipt.component.CinemaReceiptStyleSelector
import com.ssverma.feature.library.util.ShareImageHelper
import com.ssverma.shared.ads.gate.FeatureGateConfig
import com.ssverma.shared.ads.gate.FeaturePassPolicy
import com.ssverma.shared.ads.gate.GatePresentationStyle
import com.ssverma.shared.ads.gate.PassDurations
import com.ssverma.shared.ads.gate.ShowTimeFeatureGate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaReceiptScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenProPaywall: () -> Unit = {},
    viewModel: CinemaReceiptViewModel = hiltViewModel()
) {
    val analytics = LocalAnalytics.current
    TrackScreenView(screenName = LibraryAnalyticsScreenName.CINEMA_RECEIPT)

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val shareChooserTitle = stringResource(R.string.receipt_share_chooser_title)
    val saveSuccess = stringResource(R.string.receipt_saved_success)
    val saveFailed = stringResource(R.string.receipt_save_failed)

    Screen(
        title = stringResource(R.string.cinema_receipt),
        onBackPressed = onBackPressed,
        scrollBehavior = scrollBehavior,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

            // Subtitle
            Text(
                text = stringResource(R.string.receipt_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))

            // Source Selector
            CinemaReceiptSourceSelector(
                selectedSource = uiState.selectedSource,
                selectedCustomList = uiState.selectedCustomList,
                customLists = uiState.customLists,
                onSourceSelected = { source ->
                    analytics.logEvent(CinemaReceiptAnalyticsEvent.PeriodSelected(source.name))
                    viewModel.selectSource(source)
                },
                onCustomListSelected = { customList ->
                    viewModel.selectCustomList(customList)
                }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))

            // Style Selector Chips
            CinemaReceiptStyleSelector(
                selectedStyle = uiState.selectedStyle,
                isProActive = uiState.isProActive,
                isPassActive = uiState.isPassActive,
                onStyleSelected = { style ->
                    viewModel.selectStyle(style)
                }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))

            // Personalize Ticket Section
            CinemaReceiptPersonalizeSection(
                theaterName = uiState.theaterName,
                collectorName = uiState.collectorName,
                onOpenEdit = { viewModel.setEditPersonalizationOpen(true) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Receipt Canvas / Card Preview or Empty State
            val snapshot = uiState.snapshot
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
                        style = uiState.selectedStyle,
                        showWatermark = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                // Bottom Action Buttons (Share & Download)
                CinemaReceiptBottomActions(
                    isExporting = uiState.isExporting,
                    onSaveClick = {
                        analytics.logEvent(
                            CinemaReceiptAnalyticsEvent.ReceiptSaved(
                                period = uiState.selectedSource.name,
                                itemCount = snapshot.items.size
                            )
                        )
                        viewModel.attemptExport {
                            coroutineScope.launch {
                                viewModel.setExporting(true)
                                try {
                                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                    val success = ShareImageHelper.saveBitmapToGallery(
                                        context = context,
                                        bitmap = bitmap,
                                        title = "ShowTime_Receipt_${
                                            snapshot.title.replace(" ", "_")
                                        }"
                                    )
                                    Toast.makeText(
                                        context,
                                        if (success) saveSuccess else saveFailed,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (_: Exception) {
                                } finally {
                                    viewModel.setExporting(false)
                                }
                            }
                        }
                    },
                    onShareClick = {
                        analytics.logEvent(
                            CinemaReceiptAnalyticsEvent.ReceiptShared(
                                period = uiState.selectedSource.name,
                                itemCount = snapshot.items.size
                            )
                        )
                        viewModel.attemptExport {
                            coroutineScope.launch {
                                viewModel.setExporting(true)
                                try {
                                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                    ShareImageHelper.shareBitmap(
                                        context = context,
                                        bitmap = bitmap,
                                        chooserTitle = shareChooserTitle
                                    )
                                } catch (_: Exception) {
                                } finally {
                                    viewModel.setExporting(false)
                                }
                            }
                        }
                    }
                )
            } else {
                CinemaReceiptEmptyState(
                    selectedSource = uiState.selectedSource,
                    selectedCustomListTitle = uiState.selectedCustomList?.title
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
        }
    }

    if (uiState.isGateOpen) {
        ShowTimeFeatureGate(
            config = CinemaReceiptGateConfig,
            isAdLoading = false,
            isProPaymentEnabled = uiState.isProPaymentEnabled,
            onWatchAdClick = {
                val activity = context.findActivity()
                if (activity != null) {
                    viewModel.watchAdForWatermarkFreePass(activity)
                }
            },
            onUpgradeProClick = {
                viewModel.dismissGate()
                onOpenProPaywall()
            },
            onDismissRequest = { viewModel.dismissGate() }
        )
    }

    if (uiState.isEditPersonalizationOpen) {
        CinemaReceiptPersonalizeDialog(
            initialTheaterName = uiState.theaterName,
            initialCollectorName = uiState.collectorName,
            onDismiss = { viewModel.setEditPersonalizationOpen(false) },
            onSave = { theater, collector ->
                viewModel.updatePersonalization(theater, collector)
            }
        )
    }
}

private val CinemaReceiptGateConfig = FeatureGateConfig(
    titleRes = R.string.receipt_gate_title,
    descriptionRes = R.string.receipt_gate_desc,
    rewardActionLabelRes = R.string.receipt_watch_ad_pass,
    icon = Icons.Rounded.Star,
    presentationStyle = GatePresentationStyle.BottomSheet,
    passPolicy = FeaturePassPolicy.TimedPass(
        passKey = CinemaReceiptPassKey,
        durationMs = PassDurations.SHORT_EXPORT_WINDOW_MS,
        durationLabel = "2h"
    )
)
