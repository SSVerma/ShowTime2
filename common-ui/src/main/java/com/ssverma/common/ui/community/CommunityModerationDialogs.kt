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
import com.ssverma.shared.domain.model.community.CommunityReportReason
import com.ssverma.shared.ui.R

@Composable
fun ReportCommunityListDialog(
    onConfirmReport: (CommunityReportReason) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reasons = remember {
        listOf(
            CommunityReportReason.InappropriateOrSexual to R.string.report_reason_inappropriate_sexual,
            CommunityReportReason.HateOrHarassment to R.string.report_reason_hate_harassment,
            CommunityReportReason.SpamOrCommercial to R.string.report_reason_spam_misleading,
            CommunityReportReason.SpoilersWithoutWarning to R.string.report_reason_violence_harm,
            CommunityReportReason.Other to R.string.report_reason_other
        )
    }
    var selectedReason by remember { mutableStateOf(reasons.first().first) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.report_collection_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Text(
                    text = stringResource(id = R.string.report_collection_dialog_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(height = 6.dp))
                reasons.forEach { (reasonEnum, stringResId) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reasonEnum }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedReason == reasonEnum,
                            onClick = { selectedReason = reasonEnum }
                        )
                        Spacer(modifier = Modifier.width(width = 8.dp))
                        Text(
                            text = stringResource(id = stringResId),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(size = 20.dp),
        confirmButton = {
            TextButton(
                onClick = { onConfirmReport(selectedReason) }
            ) {
                Text(
                    text = stringResource(id = R.string.report_collection_submit),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        modifier = modifier
    )
}

@Composable
fun BlockAuthorConfirmationDialog(
    authorName: String,
    onConfirmBlock: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.block_creator_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.block_creator_dialog_desc, authorName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        shape = RoundedCornerShape(size = 20.dp),
        confirmButton = {
            TextButton(onClick = onConfirmBlock) {
                Text(
                    text = stringResource(id = R.string.block_creator_confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        modifier = modifier
    )
}

@Composable
fun SensitiveContentConfirmationDialog(
    matchedKeyword: String,
    onConfirmPublish: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.sensitive_theme_confirm_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.sensitive_theme_confirm_desc, matchedKeyword),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        shape = RoundedCornerShape(size = 20.dp),
        confirmButton = {
            TextButton(onClick = onConfirmPublish) {
                Text(
                    text = stringResource(id = R.string.sensitive_theme_confirm_action),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        modifier = modifier
    )
}

@Composable
fun CommunityGuidelinesAgreementDialog(
    onAgree: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.community_guidelines_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.community_guidelines_dialog_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        shape = RoundedCornerShape(size = 20.dp),
        confirmButton = {
            TextButton(onClick = onAgree) {
                Text(
                    text = stringResource(id = R.string.community_guidelines_agree_action),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        modifier = modifier
    )
}

