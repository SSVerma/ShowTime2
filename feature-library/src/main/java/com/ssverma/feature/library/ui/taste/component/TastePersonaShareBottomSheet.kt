package com.ssverma.feature.library.ui.taste.component

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.navigation.dispatcher.IntentDispatcher
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.feature.library.R
import com.ssverma.feature.library.util.ShareImageHelper
import com.ssverma.shared.domain.model.stats.TasteProfileStats
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TastePersonaShareBottomSheet(
    stats: TasteProfileStats,
    isExporting: Boolean,
    onDismissRequest: () -> Unit,
    onSetExporting: (Boolean) -> Unit,
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

    val saveSuccess = stringResource(R.string.taste_share_save_success)
    val saveFailed = stringResource(R.string.taste_share_save_failed)
    val chooserTitle = stringResource(R.string.taste_share_chooser)

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
                .padding(bottom = 12.dp)
        ) {
            // Header Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.taste_share_card_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.taste_share_card_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                TastePersonaCardView(
                    stats = stats,
                    userName = userName,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                            onSetExporting(true)
                            try {
                                if (scrollState.value > 0) {
                                    scrollState.scrollTo(0)
                                }
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                val success = ShareImageHelper.saveBitmapToGallery(
                                    context = context,
                                    bitmap = bitmap,
                                    title = "ShowTime_Taste_Persona_${stats.persona.name}"
                                )
                                Toast.makeText(
                                    context,
                                    if (success) saveSuccess else saveFailed,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (_: Exception) {
                                Toast.makeText(context, saveFailed, Toast.LENGTH_SHORT).show()
                            } finally {
                                onSetExporting(false)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.taste_save_image_btn),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            onSetExporting(true)
                            try {
                                if (scrollState.value > 0) {
                                    scrollState.scrollTo(0)
                                }
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                ShareImageHelper.shareBitmap(
                                    context = context,
                                    bitmap = bitmap,
                                    chooserTitle = chooserTitle
                                )
                            } catch (_: Exception) {
                                Toast.makeText(context, saveFailed, Toast.LENGTH_SHORT).show()
                            } finally {
                                onSetExporting(false)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.taste_share_image_btn),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Secondary Option: Share Text Summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = {
                        with(IntentDispatcher) {
                            context.dispatchShareTextIntent(text = shareText)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TextFields,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.taste_share_text_btn),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (isExporting) {
            ShowTimeLoadingIndicator()
        }
    }
}
