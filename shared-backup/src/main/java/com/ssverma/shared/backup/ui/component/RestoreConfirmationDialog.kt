package com.ssverma.shared.backup.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.shared.backup.R

@Composable
fun RestoreConfirmationDialog(
    metadata: BackupMetadata,
    localItemCount: Int,
    onConfirmRestore: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val origin = metadata.deviceName.ifBlank { metadata.formattedDate }
    val totalCloudCount = metadata.favoritesCount + metadata.watchlistCount +
            metadata.historyCount + metadata.customListsCount + metadata.diaryEntriesCount +
            metadata.showProgressCount

    val message = if (localItemCount == 0) {
        stringResource(
            R.string.shared_backup_restore_confirm_msg_empty,
            origin,
            totalCloudCount
        )
    } else {
        stringResource(
            R.string.shared_backup_restore_confirm_msg_merge,
            totalCloudCount,
            origin
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Rounded.CloudSync,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.shared_backup_restore_confirm_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            Button(
                onClick = onConfirmRestore
            ) {
                Text(text = stringResource(R.string.shared_backup_restore_confirm_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.shared_backup_restore_dismiss_action))
            }
        },
        modifier = modifier
    )
}
