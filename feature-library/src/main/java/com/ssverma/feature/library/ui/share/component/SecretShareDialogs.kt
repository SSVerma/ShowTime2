package com.ssverma.feature.library.ui.share.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import com.ssverma.feature.library.R
import com.ssverma.shared.ads.gate.FeatureGateConfig
import com.ssverma.shared.ads.gate.FeaturePassPolicy
import com.ssverma.shared.ads.gate.GatePresentationStyle
import com.ssverma.shared.ads.gate.ShowTimeFeatureGate
import com.ssverma.feature.library.ui.share.SecretShareThemesPassKey

@Composable
internal fun SecretShareEditNameDialog(
    initialName: String,
    onDismissRequest: () -> Unit,
    onSaveName: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var tempName by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.secret_share_enter_name_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.secret_share_enter_name_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.secret_share_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = tempName.trim()
                    if (trimmed.isNotBlank()) {
                        onSaveName(trimmed)
                    }
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(R.string.secret_share_save_name))
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
internal fun SecretShareRevokeConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirmRevoke: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(text = stringResource(R.string.secret_share_revoke_action)) },
        text = { Text(text = stringResource(R.string.secret_share_revoke_confirm)) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmRevoke()
                    onDismissRequest()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = stringResource(R.string.remove_item))
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
internal fun SecretShareLuxuryGateDialog(
    isProPaymentEnabled: Boolean,
    onDismissRequest: () -> Unit,
    onWatchAd: () -> Unit,
    onOpenProPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    ShowTimeFeatureGate(
        config = SecretShareThemesGateConfig,
        isAdLoading = false,
        isProPaymentEnabled = isProPaymentEnabled,
        onWatchAdClick = onWatchAd,
        onUpgradeProClick = onOpenProPaywall,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    )
}

private val SecretShareThemesGateConfig = FeatureGateConfig(
    titleRes = R.string.secret_share_gate_title,
    descriptionRes = R.string.secret_share_gate_desc,
    rewardActionLabelRes = R.string.secret_share_watch_ad_pass,
    icon = Icons.Rounded.Star,
    presentationStyle = GatePresentationStyle.Dialog,
    passPolicy = FeaturePassPolicy.TimedPass(
        passKey = SecretShareThemesPassKey
    )
)
