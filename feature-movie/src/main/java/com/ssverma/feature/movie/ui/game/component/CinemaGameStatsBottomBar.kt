package com.ssverma.feature.movie.ui.game.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.core.ui.util.ShareImageHelper
import com.ssverma.feature.movie.R
import kotlinx.coroutines.launch

@Composable
fun CinemaGameStatsBottomBar(
    graphicsLayer: GraphicsLayer,
    shareableText: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSharingImage by remember { mutableStateOf(false) }
    var isSavingToGallery by remember { mutableStateOf(false) }

    val shareFailedMsg = stringResource(id = R.string.cinema_stats_share_failed)
    val savedToGalleryMsg = stringResource(id = R.string.cinema_stats_saved_to_gallery)
    val saveFailedMsg = stringResource(id = R.string.cinema_stats_save_failed)
    val textCopiedMsg = stringResource(id = R.string.cinema_stats_text_copied)

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.smallMedium
                    )
            ) {
                // Primary Action: Share Graphic Card
                Button(
                    onClick = {
                        if (isSharingImage) return@Button
                        isSharingImage = true
                        coroutineScope.launch {
                            try {
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                val success = ShareImageHelper.shareBitmap(
                                    context = context,
                                    bitmap = bitmap,
                                    chooserTitle = "Share Cinema Challenge Result"
                                )
                                if (!success) {
                                    Toast.makeText(context, shareFailedMsg, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } catch (_: Exception) {
                                Toast.makeText(context, shareFailedMsg, Toast.LENGTH_SHORT).show()
                            } finally {
                                isSharingImage = false
                            }
                        }
                    },
                    enabled = !isSharingImage,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isSharingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Text(
                            text = stringResource(id = R.string.cinema_stats_share_graphic_card),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                // Secondary Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isSavingToGallery) return@OutlinedButton
                            isSavingToGallery = true
                            coroutineScope.launch {
                                try {
                                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                    val success = ShareImageHelper.saveBitmapToGallery(
                                        context = context,
                                        bitmap = bitmap,
                                        title = "ShowTime_Cinema_Challenge"
                                    )
                                    val msg = if (success) savedToGalleryMsg else saveFailedMsg
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSavingToGallery = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Text(text = stringResource(id = R.string.cinema_stats_save_gallery))
                    }

                    if (!shareableText.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText(
                                    "ShowTime Cinema Challenge",
                                    shareableText
                                )
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, textCopiedMsg, Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                            Text(text = stringResource(id = R.string.cinema_stats_copy_text))
                        }
                    }
                }
            }
        }
    }
}
