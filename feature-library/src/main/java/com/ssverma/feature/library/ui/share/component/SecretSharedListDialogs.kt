package com.ssverma.feature.library.ui.share.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ssverma.feature.library.R

@Composable
fun SecretSharedListAddAllConfirmDialog(
    itemCount: Int,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(text = stringResource(R.string.secret_share_add_all_confirm_title)) },
        text = {
            Text(
                text = stringResource(
                    R.string.secret_share_add_all_confirm_desc,
                    itemCount
                )
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(R.string.secret_share_add_all_confirm_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SecretSharedListCloneConfirmDialog(
    listTitle: String,
    itemCount: Int,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(text = stringResource(R.string.secret_share_clone_confirm_title)) },
        text = {
            Text(
                text = stringResource(
                    R.string.secret_share_clone_confirm_desc,
                    listTitle,
                    itemCount
                )
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(R.string.secret_share_clone_confirm_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SecretSharedListRemoveItemConfirmDialog(
    itemTitle: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(text = stringResource(R.string.secret_share_remove_item_confirm_title)) },
        text = {
            Text(
                text = stringResource(
                    R.string.secret_share_remove_item_confirm_desc,
                    itemTitle
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = stringResource(R.string.secret_share_remove_item_confirm_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
