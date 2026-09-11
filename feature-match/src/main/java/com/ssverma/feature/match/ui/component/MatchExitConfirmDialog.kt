package com.ssverma.feature.match.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.ssverma.feature.match.R

@Composable
fun MatchExitConfirmDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.match_room_exit_session_dialog_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.match_room_exit_session_dialog_message)
            )
        },
        confirmButton = {
            Button(onClick = onConfirmExit) {
                Text(stringResource(R.string.match_room_exit_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.match_room_exit_dialog_cancel))
            }
        },
        modifier = modifier
    )
}
