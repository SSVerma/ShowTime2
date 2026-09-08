package com.ssverma.feature.library.ui.backlog.component

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
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
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchShareTextIntent
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.feature.library.R
import com.ssverma.feature.library.util.ShareImageHelper
import com.ssverma.shared.domain.model.challenge.ChallengeProgress
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeShareExportBottomSheet(
    progress: ChallengeProgress,
    onDismissRequest: () -> Unit,
    shareText: String,
    modifier: Modifier = Modifier,
    userName: String? = null,
    sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val scrollState = rememberScrollState()
    var isExporting by remember { mutableStateOf(false) }

    val saveSuccess = stringResource(R.string.challenges_share_save_success)
    val saveFailed = stringResource(R.string.challenges_share_save_failed)
    val chooserTitle = stringResource(R.string.challenges_share_chooser)

    ShowTimeBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Header Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (progress.isCompleted) {
                        stringResource(R.string.challenges_share_header_completed)
                    } else {
                        stringResource(R.string.challenges_share_header_progress)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.challenges_share_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Card Preview & Capture Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
            ) {
                ChallengeShareCardView(
                    progress = progress,
                    userName = userName,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Export Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            isExporting = true
                            try {
                                if (scrollState.value > 0) {
                                    scrollState.scrollTo(0)
                                }
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                val success = ShareImageHelper.saveBitmapToGallery(
                                    context = context,
                                    bitmap = bitmap,
                                    title = "ShowTime_Challenge_${
                                        progress.challenge.title.replace(
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
                                Toast.makeText(context, saveFailed, Toast.LENGTH_SHORT).show()
                            } finally {
                                isExporting = false
                            }
                        }
                    },
                    enabled = !isExporting,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.challenges_share_save_btn),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isExporting = true
                            try {
                                if (scrollState.value > 0) {
                                    scrollState.scrollTo(0)
                                }
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                val success = ShareImageHelper.shareBitmap(
                                    context = context,
                                    bitmap = bitmap,
                                    chooserTitle = chooserTitle
                                )
                                if (!success) {
                                    Toast.makeText(context, saveFailed, Toast.LENGTH_SHORT).show()
                                }
                            } catch (_: Exception) {
                                Toast.makeText(context, saveFailed, Toast.LENGTH_SHORT).show()
                            } finally {
                                isExporting = false
                            }
                        }
                    },
                    enabled = !isExporting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1.3f)
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
                            text = stringResource(R.string.challenges_share_card_btn),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Fallback: Share as Text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = {
                        context.dispatchShareTextIntent(text = shareText)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TextFields,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.challenges_share_as_text_btn),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
