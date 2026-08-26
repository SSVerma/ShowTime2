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
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.Screen
import com.ssverma.core.ui.theme.spacing
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
                    label = { Text(stringResource(R.string.receipt_style_gold)) }
                )
                FilterChip(
                    selected = uiState.selectedStyle == ReceiptStyle.CYBERPUNK,
                    onClick = { viewModel.selectStyle(ReceiptStyle.CYBERPUNK) },
                    label = { Text(stringResource(R.string.receipt_style_cyberpunk)) }
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

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
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
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
}
