package com.ssverma.common.ui.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.shared.ui.R

@Composable
fun DeleteThoughtConfirmationDialog(
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.delete_thought_dialog_title)) },
        text = { Text(text = stringResource(id = R.string.delete_thought_dialog_msg)) },
        shape = RoundedCornerShape(size = 20.dp),
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
                Text(
                    text = stringResource(id = R.string.delete_action),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel_action))
            }
        },
        modifier = modifier
    )
}

@Composable
fun ReportThoughtDialog(
    onConfirmReport: (reason: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reportReasons = listOf(
        stringResource(id = R.string.report_reason_spam),
        stringResource(id = R.string.report_reason_harassment),
        stringResource(id = R.string.report_reason_spoiler),
        stringResource(id = R.string.report_reason_inappropriate)
    )
    var selectedReason by remember { mutableStateOf(reportReasons.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.report_thought_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Text(
                    text = stringResource(id = R.string.report_thought_dialog_msg),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(height = 4.dp))
                reportReasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(width = 8.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(size = 20.dp),
        confirmButton = {
            TextButton(onClick = { onConfirmReport(selectedReason) }) {
                Text(text = stringResource(id = R.string.report_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel_action))
            }
        },
        modifier = modifier
    )
}
