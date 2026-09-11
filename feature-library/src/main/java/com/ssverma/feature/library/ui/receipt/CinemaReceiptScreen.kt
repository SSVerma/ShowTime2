package com.ssverma.feature.library.ui.receipt

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ssverma.common.ui.quota.FeatureQuotaGateBottomSheet
import com.ssverma.core.ui.Screen
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.theme.spacing
import com.ssverma.core.ui.util.findActivity
import com.ssverma.feature.library.R
import com.ssverma.feature.library.domain.model.ReceiptSource
import com.ssverma.feature.library.domain.model.ReceiptStyle
import com.ssverma.feature.library.util.ShareImageHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaReceiptScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenProPaywall: () -> Unit = {},
    viewModel: CinemaReceiptViewModel = hiltViewModel()
) {
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
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            // Subtitle
            Text(
                text = stringResource(R.string.receipt_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Source Selector
            Text(
                text = stringResource(R.string.receipt_source),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = MaterialTheme.spacing.medium, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedSource == ReceiptSource.HISTORY && uiState.selectedCustomList == null,
                    onClick = { viewModel.selectSource(ReceiptSource.HISTORY) },
                    label = { Text(stringResource(R.string.receipt_period_history)) },
                    colors = FilterChipDefaults.filterChipColors()
                )
                FilterChip(
                    selected = uiState.selectedSource == ReceiptSource.THIS_MONTH && uiState.selectedCustomList == null,
                    onClick = { viewModel.selectSource(ReceiptSource.THIS_MONTH) },
                    label = { Text(stringResource(R.string.receipt_period_this_month)) },
                    colors = FilterChipDefaults.filterChipColors()
                )
                FilterChip(
                    selected = uiState.selectedSource == ReceiptSource.THIS_YEAR && uiState.selectedCustomList == null,
                    onClick = { viewModel.selectSource(ReceiptSource.THIS_YEAR) },
                    label = { Text(stringResource(R.string.receipt_period_this_year)) },
                    colors = FilterChipDefaults.filterChipColors()
                )
                FilterChip(
                    selected = uiState.selectedSource == ReceiptSource.LAST_90_DAYS && uiState.selectedCustomList == null,
                    onClick = { viewModel.selectSource(ReceiptSource.LAST_90_DAYS) },
                    label = { Text(stringResource(R.string.receipt_period_last_90_days)) },
                    colors = FilterChipDefaults.filterChipColors()
                )
                FilterChip(
                    selected = uiState.selectedSource == ReceiptSource.FAVORITES && uiState.selectedCustomList == null,
                    onClick = { viewModel.selectSource(ReceiptSource.FAVORITES) },
                    label = { Text(stringResource(R.string.receipt_period_favorites)) },
                    colors = FilterChipDefaults.filterChipColors()
                )
                FilterChip(
                    selected = uiState.selectedSource == ReceiptSource.WATCHLIST && uiState.selectedCustomList == null,
                    onClick = { viewModel.selectSource(ReceiptSource.WATCHLIST) },
                    label = { Text(stringResource(R.string.receipt_period_watchlist)) },
                    colors = FilterChipDefaults.filterChipColors()
                )
                uiState.customLists.forEach { customList ->
                    FilterChip(
                        selected = uiState.selectedCustomList?.listId == customList.listId,
                        onClick = { viewModel.selectCustomList(customList) },
                        label = { Text(customList.title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            // Style Selector Chips
            Text(
                text = stringResource(R.string.receipt_style),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = MaterialTheme.spacing.medium, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedStyle == ReceiptStyle.THERMAL,
                    onClick = { viewModel.selectStyle(ReceiptStyle.THERMAL) },
                    label = { Text(stringResource(R.string.receipt_style_thermal)) }
                )
                FilterChip(
                    selected = uiState.selectedStyle == ReceiptStyle.GOLDEN_PASS,
                    onClick = { viewModel.selectStyle(ReceiptStyle.GOLDEN_PASS) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.receipt_style_gold))
                            if (!uiState.isProActive && !uiState.isPassActive) {
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
                FilterChip(
                    selected = uiState.selectedStyle == ReceiptStyle.CYBERPUNK,
                    onClick = { viewModel.selectStyle(ReceiptStyle.CYBERPUNK) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.receipt_style_cyberpunk))
                            if (!uiState.isProActive && !uiState.isPassActive) {
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

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            // Personalize Ticket Section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.theaterName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.collectorName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.setEditPersonalizationOpen(true) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.receipt_personalize_ticket),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Receipt Canvas / Card Preview
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.attemptExport {
                                coroutineScope.launch {
                                    viewModel.setExporting(true)
                                    try {
                                        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
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
                                    } catch (_: Exception) {
                                    } finally {
                                        viewModel.setExporting(false)
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
                            text = stringResource(R.string.receipt_save),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
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
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isExporting) {
                            ShowTimeLoadingIndicator(
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.receipt_share),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium, vertical = 32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.empty_history_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.empty_history_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
        }
    }

    if (uiState.isGateOpen) {
        FeatureQuotaGateBottomSheet(
            title = stringResource(R.string.receipt_gate_title),
            description = stringResource(R.string.receipt_gate_desc),
            rewardActionLabel = stringResource(R.string.receipt_watch_ad_pass),
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
            onDismissRequest = { viewModel.dismissGate() },
            isProPaymentEnabled = uiState.isProPaymentEnabled,
            icon = Icons.Rounded.Star
        )
    }

    if (uiState.isEditPersonalizationOpen) {
        var tempTheater by remember { mutableStateOf(uiState.theaterName) }
        var tempCollector by remember { mutableStateOf(uiState.collectorName) }

        AlertDialog(
            onDismissRequest = { viewModel.setEditPersonalizationOpen(false) },
            title = { Text(stringResource(R.string.receipt_personalize_ticket)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.receipt_personalize_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = tempTheater,
                        onValueChange = { tempTheater = it },
                        label = { Text(stringResource(R.string.receipt_theater_name_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempCollector,
                        onValueChange = { tempCollector = it },
                        label = { Text(stringResource(R.string.receipt_collector_name_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePersonalization(tempTheater, tempCollector)
                    }
                ) {
                    Text(stringResource(R.string.receipt_save_personalization))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.setEditPersonalizationOpen(false) }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
