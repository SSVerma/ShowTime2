package com.ssverma.feature.library.ui.share

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.util.findActivity
import com.ssverma.feature.library.R
import com.ssverma.feature.library.ui.share.component.SecretShareCollaborativeToggleCard
import com.ssverma.feature.library.ui.share.component.SecretShareCuratorCard
import com.ssverma.feature.library.ui.share.component.SecretShareEditNameDialog
import com.ssverma.feature.library.ui.share.component.SecretShareFormatSelector
import com.ssverma.feature.library.ui.share.component.SecretShareImageActionsRow
import com.ssverma.feature.library.ui.share.component.SecretShareLinkActionsRow
import com.ssverma.feature.library.ui.share.component.SecretShareLuxuryGateDialog
import com.ssverma.feature.library.ui.share.component.SecretShareRevokeButton
import com.ssverma.feature.library.ui.share.component.SecretShareRevokeConfirmDialog
import com.ssverma.feature.library.ui.share.component.SecretShareThemeSelector
import com.ssverma.feature.library.ui.share.component.copyToClipboard
import com.ssverma.feature.library.util.ShareImageHelper
import com.ssverma.shared.domain.model.library.ListShareCardFormat
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
    customListId: String? = null,
    initialShareCode: String? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onOpenProPaywall: () -> Unit = {},
    viewModel: ListShareExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    var showRevokeConfirmDialog by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }

    val storedCuratorName = remember(context, ownerName) {
        val prefs = context.getSharedPreferences("showtime_device_prefs", Context.MODE_PRIVATE)
        val uid = prefs.getString("persistent_user_uuid", "").orEmpty()
        val defaultCinephile =
            if (uid.isNotBlank()) "Cinephile #${kotlin.math.abs(uid.hashCode() % 900) + 100}" else "Cinephile"
        prefs.getString("user_display_name", null)
            ?.takeIf {
                it.isNotBlank() && !it.equals(
                    "Me",
                    ignoreCase = true
                ) && !it.equals("Friend", ignoreCase = true)
            }
            ?: ownerName.takeIf {
                it.isNotBlank() && !it.equals(
                    "Me",
                    ignoreCase = true
                ) && !it.equals("Friend", ignoreCase = true)
            }
            ?: defaultCinephile
    }
    var activeCuratorName by remember(storedCuratorName) { mutableStateOf(storedCuratorName) }

    val effectiveOwnerName = activeCuratorName.ifBlank {
        ownerName.takeIf { it.isNotBlank() && !it.equals("Me", ignoreCase = true) }
            ?: storedCuratorName
    }

    val shareChooserTitle = stringResource(R.string.secret_share_chooser_title)
    val linkCopiedMsg = stringResource(R.string.secret_share_link_copied)
    val linkCreatingMsg = stringResource(R.string.secret_share_creating)
    val linkRevokedMsg = stringResource(R.string.secret_share_revoked_success)
    val saveSuccessMsg = stringResource(R.string.receipt_saved_success)
    val saveFailedMsg = stringResource(R.string.receipt_save_failed)

    var pendingCopyOnReady by remember { mutableStateOf(false) }
    var pendingShareOnReady by remember { mutableStateOf(false) }

    LaunchedEffect(initialShareCode) {
        if (initialShareCode != null) {
            viewModel.initSecretShare(shareCode = initialShareCode)
        } else if (uiState.shareCode == null) {
            viewModel.generateSecretLink(
                title = title,
                description = description,
                items = items,
                ownerName = effectiveOwnerName,
                customListId = customListId
            )
        }
    }

    LaunchedEffect(uiState.shareCode) {
        val code = uiState.shareCode ?: return@LaunchedEffect
        val url = ShareMediaUtils.buildSecretListUrl(code)
        if (pendingCopyOnReady) {
            pendingCopyOnReady = false
            copyToClipboard(
                composeClipboardManager = clipboardManager,
                text = url
            )
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                Toast.makeText(context.applicationContext, linkCopiedMsg, Toast.LENGTH_SHORT).show()
            }
        }
        if (pendingShareOnReady) {
            pendingShareOnReady = false
            val shareText = ShareMediaUtils.buildFormattedSecretListMarkdown(
                title = title,
                description = description,
                authorName = effectiveOwnerName,
                shareCode = code,
                itemTitlesWithRating = items.map { it.title to it.voteAvg }
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(shareIntent, shareChooserTitle))
        }
    }

    ShowTimeBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.secret_share_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.secret_share_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showInfoSheet = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = stringResource(R.string.secret_share_info_learn_more),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.close),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Format Selection Chips
            SecretShareFormatSelector(
                selectedFormat = uiState.selectedFormat,
                onSelectFormat = viewModel::selectFormat
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Theme Selection Carousel
            SecretShareThemeSelector(
                selectedTheme = uiState.selectedTheme,
                isLuxuryUnlocked = uiState.isProActive || uiState.isPassActive,
                onSelectTheme = viewModel::selectTheme
            )

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
                    ownerName = effectiveOwnerName,
                    itemCount = items.size,
                    averageRating = avgRating,
                    posters = previewPosters,
                    theme = uiState.selectedTheme,
                    format = uiState.selectedFormat,
                    showWatermark = !uiState.isWatermarkFree
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Curator Display Name Card ("Sharing as")
            SecretShareCuratorCard(
                curatorName = effectiveOwnerName,
                onChangeNameClick = { showEditNameDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Collaborative Co-Curators Toggle
            SecretShareCollaborativeToggleCard(
                isCollaborative = uiState.isCollaborative,
                onToggleCollaborative = viewModel::setCollaborative
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Link Actions Row (Copy Link & Share Link)
            SecretShareLinkActionsRow(
                isCreatingLink = uiState.isCreatingLink,
                isPendingCopy = pendingCopyOnReady,
                isPendingShare = pendingShareOnReady,
                onCopyLinkClick = {
                    val existingCode = uiState.shareCode
                    if (existingCode != null) {
                        val url = ShareMediaUtils.buildSecretListUrl(existingCode)
                        copyToClipboard(
                            composeClipboardManager = clipboardManager,
                            text = url
                        )
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                            Toast.makeText(
                                context.applicationContext,
                                linkCopiedMsg,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context.applicationContext,
                            linkCreatingMsg,
                            Toast.LENGTH_SHORT
                        ).show()
                        pendingCopyOnReady = true
                        viewModel.generateSecretLink(
                            title = title,
                            description = description,
                            items = items,
                            ownerName = effectiveOwnerName,
                            customListId = customListId
                        )
                    }
                },
                onShareLinkClick = {
                    val existingCode = uiState.shareCode
                    if (existingCode != null) {
                        val shareText = ShareMediaUtils.buildFormattedSecretListMarkdown(
                            title = title,
                            description = description,
                            authorName = effectiveOwnerName,
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
                        Toast.makeText(
                            context.applicationContext,
                            linkCreatingMsg,
                            Toast.LENGTH_SHORT
                        ).show()
                        pendingShareOnReady = true
                        viewModel.generateSecretLink(
                            title = title,
                            description = description,
                            items = items,
                            ownerName = effectiveOwnerName,
                            customListId = customListId
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Story Card Image Actions Row
            SecretShareImageActionsRow(
                isExportingImage = uiState.isExportingImage,
                onShareImageClick = {
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
                onSaveImageClick = {
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
            )

            // Revoke Secret Link Action Button
            if (uiState.shareCode != null) {
                Spacer(modifier = Modifier.height(4.dp))
                SecretShareRevokeButton(
                    onRevokeClick = { showRevokeConfirmDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Dialogs
        if (showInfoSheet) {
            SecretShareInfoBottomSheet(
                onDismissRequest = { showInfoSheet = false }
            )
        }

        if (showEditNameDialog) {
            SecretShareEditNameDialog(
                initialName = activeCuratorName,
                onDismissRequest = { showEditNameDialog = false },
                onSaveName = { updatedName ->
                    activeCuratorName = updatedName
                    context.getSharedPreferences("showtime_device_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putString("user_display_name", updatedName)
                        .apply()
                }
            )
        }

        if (showRevokeConfirmDialog) {
            SecretShareRevokeConfirmDialog(
                onDismissRequest = { showRevokeConfirmDialog = false },
                onConfirmRevoke = {
                    viewModel.revokeSecretShare(customListId = customListId) {
                        Toast.makeText(
                            context.applicationContext,
                            linkRevokedMsg,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        if (uiState.isGateOpen) {
            SecretShareLuxuryGateDialog(
                isProPaymentEnabled = uiState.isProPaymentEnabled,
                onDismissRequest = viewModel::closeGate,
                onWatchAd = {
                    val activity = context.findActivity()
                    if (activity != null) {
                        viewModel.unlockThemesWithRewardedAd(activity)
                    }
                },
                onOpenProPaywall = {
                    viewModel.closeGate()
                    onDismissRequest()
                    onOpenProPaywall()
                }
            )
        }
    }
}
