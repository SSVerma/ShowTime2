package com.ssverma.feature.library.ui.share

import android.app.Activity
import android.content.Intent
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.util.findActivity
import com.ssverma.feature.library.R
import com.ssverma.feature.library.util.ShareImageHelper
import com.ssverma.shared.domain.model.library.ListShareCardFormat
import com.ssverma.shared.domain.model.library.ListShareTheme
import com.ssverma.shared.domain.model.library.SecretSharedListItem
import com.ssverma.shared.domain.utils.ShareMediaUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListShareExportBottomSheet(
    title: String,
    description: String?,
    ownerName: String,
    items: List<SecretSharedListItem>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onOpenProPaywall: () -> Unit = {},
    viewModel: ListShareExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val clipboardManager = LocalClipboardManager.current

    var showRevokeConfirmDialog by remember { mutableStateOf(false) }

    val shareChooserTitle = stringResource(R.string.secret_share_chooser_title)
    val linkCopiedMsg = stringResource(R.string.secret_share_link_copied)
    val saveSuccessMsg = stringResource(R.string.receipt_saved_success)
    val saveFailedMsg = stringResource(R.string.receipt_save_failed)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.secret_share_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.secret_share_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Format Selection Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.secret_share_format_label) + ":",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilterChip(
                    selected = uiState.selectedFormat == ListShareCardFormat.STORY_9_16,
                    onClick = { viewModel.selectFormat(ListShareCardFormat.STORY_9_16) },
                    label = { Text(stringResource(R.string.secret_share_format_story)) }
                )
                FilterChip(
                    selected = uiState.selectedFormat == ListShareCardFormat.SQUARE_1_1,
                    onClick = { viewModel.selectFormat(ListShareCardFormat.SQUARE_1_1) },
                    label = { Text(stringResource(R.string.secret_share_format_square)) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Theme Selection Carousel
            Text(
                text = stringResource(R.string.secret_share_theme_label),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionChip(
                    label = "Classic",
                    isSelected = uiState.selectedTheme == ListShareTheme.CLASSIC_SHOWTIME,
                    isLocked = false,
                    onClick = { viewModel.selectTheme(ListShareTheme.CLASSIC_SHOWTIME) }
                )
                ThemeOptionChip(
                    label = "Vintage 35mm",
                    isSelected = uiState.selectedTheme == ListShareTheme.VINTAGE_35MM,
                    isLocked = !uiState.isProActive && !uiState.isPassActive,
                    onClick = { viewModel.selectTheme(ListShareTheme.VINTAGE_35MM) }
                )
                ThemeOptionChip(
                    label = "OLED Noir",
                    isSelected = uiState.selectedTheme == ListShareTheme.OLED_MIDNIGHT,
                    isLocked = !uiState.isProActive && !uiState.isPassActive,
                    onClick = { viewModel.selectTheme(ListShareTheme.OLED_MIDNIGHT) }
                )
                ThemeOptionChip(
                    label = "Cyberpunk",
                    isSelected = uiState.selectedTheme == ListShareTheme.NEON_CYBERPUNK,
                    isLocked = !uiState.isProActive && !uiState.isPassActive,
                    onClick = { viewModel.selectTheme(ListShareTheme.NEON_CYBERPUNK) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Card Preview
            val previewPosters = items.map { it.posterImageUrl }.filter { it.isNotBlank() }
            val ratedItems = items.filter { it.voteAvg > 0f }
            val avgRating = if (ratedItems.isNotEmpty()) {
                ratedItems.map { it.voteAvg }.average().toFloat()
            } else {
                0f
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(if (uiState.selectedFormat == ListShareCardFormat.STORY_9_16) 0.82f else 0.95f)
                    .align(Alignment.CenterHorizontally)
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
            ) {
                ListShareStoryCardView(
                    title = title,
                    ownerName = ownerName,
                    itemCount = items.size,
                    averageRating = avgRating,
                    posters = previewPosters,
                    theme = uiState.selectedTheme,
                    format = uiState.selectedFormat,
                    showWatermark = !uiState.isWatermarkFree
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Collaborative Co-Curators Toggle
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.secret_share_collaborative_title),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.secret_share_collaborative_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = uiState.isCollaborative,
                        onCheckedChange = { viewModel.setCollaborative(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Share Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Generate & Copy / Share Secret Link
                Button(
                    onClick = {
                        val existingCode = uiState.shareCode
                        if (existingCode != null) {
                            val shareText = ShareMediaUtils.buildFormattedSecretListMarkdown(
                                title = title,
                                description = description,
                                authorName = ownerName,
                                shareCode = existingCode,
                                itemTitlesWithRating = items.map { it.title to it.voteAvg }
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(
                                Intent.createChooser(shareIntent, shareChooserTitle)
                            )
                        } else {
                            viewModel.generateSecretLink(
                                title = title,
                                description = description,
                                items = items,
                                ownerName = ownerName,
                                onSuccess = { code ->
                                    val url = ShareMediaUtils.buildSecretListUrl(code)
                                    clipboardManager.setText(AnnotatedString(url))
                                    Toast.makeText(context, linkCopiedMsg, Toast.LENGTH_SHORT)
                                        .show()

                                    val shareText =
                                        ShareMediaUtils.buildFormattedSecretListMarkdown(
                                            title = title,
                                            description = description,
                                            authorName = ownerName,
                                            shareCode = code,
                                            itemTitlesWithRating = items.map { it.title to it.voteAvg }
                                        )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, shareChooserTitle)
                                    )
                                }
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isCreatingLink) {
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
                            text = if (uiState.shareCode != null) {
                                stringResource(R.string.secret_share_copy_link)
                            } else {
                                stringResource(R.string.secret_share_create_cta)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Share Story Image Card
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.setExportingImage(true)
                            try {
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                ShareImageHelper.shareBitmap(
                                    context = context,
                                    bitmap = bitmap,
                                    chooserTitle = shareChooserTitle
                                )
                            } catch (_: Exception) {
                            } finally {
                                viewModel.setExportingImage(false)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isExportingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.secret_share_export_image),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Save to Photos & Revoke Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                val success = ShareImageHelper.saveBitmapToGallery(
                                    context = context,
                                    bitmap = bitmap,
                                    title = "ShowTime_Secret_${title.replace(" ", "_")}"
                                )
                                Toast.makeText(
                                    context,
                                    if (success) saveSuccessMsg else saveFailedMsg,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (_: Exception) {
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.secret_share_save_image))
                }

                if (uiState.shareCode != null) {
                    TextButton(
                        onClick = { showRevokeConfirmDialog = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.secret_share_revoke_action))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Revoke Confirmation Dialog
        if (showRevokeConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showRevokeConfirmDialog = false },
                title = { Text(stringResource(R.string.secret_share_revoke_action)) },
                text = { Text(stringResource(R.string.secret_share_revoke_confirm)) },
                confirmButton = {
                    Button(
                        onClick = {
                            showRevokeConfirmDialog = false
                            viewModel.revokeSecretShare {
                                Toast.makeText(context, "Secret link revoked", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.remove_item))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRevokeConfirmDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Luxury Themes Gate Dialog
        if (uiState.isGateOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.closeGate() },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = { Text(stringResource(R.string.secret_share_gate_title)) },
                text = { Text(stringResource(R.string.secret_share_gate_desc)) },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val activity = context.findActivity()
                                if (activity != null) {
                                    viewModel.unlockThemesWithRewardedAd(activity)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.secret_share_watch_ad_pass))
                        }

                        if (uiState.isProPaymentEnabled) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.closeGate()
                                    onOpenProPaywall()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Get ShowTime Pro")
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeGate() }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
private fun ThemeOptionChip(
    label: String,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label)
                if (isLocked) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        },
        colors = FilterChipDefaults.filterChipColors()
    )
}
