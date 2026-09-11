package com.ssverma.feature.match.ui.component

import android.app.Activity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.ssverma.feature.match.R

@Composable
fun MatchQuotaModal(
    onUnlockPro: () -> Unit,
    onWatchRewardedAd: (Activity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.match_room_free_quota_exceeded),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = stringResource(R.string.match_room_unlock_with_pass))
        },
        confirmButton = {
            Button(onClick = onUnlockPro) {
                Text(stringResource(R.string.match_room_get_pro))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    if (activity != null) {
                        onWatchRewardedAd(activity)
                    }
                }
            ) {
                Text(stringResource(R.string.match_room_watch_video_pass))
            }
        },
        modifier = modifier
    )
}
