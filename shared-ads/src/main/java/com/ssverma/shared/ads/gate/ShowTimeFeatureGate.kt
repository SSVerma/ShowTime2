package com.ssverma.shared.ads.gate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.ui.asString
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ads.R
import com.ssverma.shared.ads.gate.component.ShowTimeFeatureGateContent

/**
 * Unified, config-driven feature gate composable.
 *
 * Orchestrates the presentation of a monetization gate, including:
 * - **Ad preloading**: Triggers [rewardedAdManager].[loadAd] as soon as the gate appears.
 * - **Presentation routing**: Renders as a [ShowTimeBottomSheet] or [AlertDialog] based on
 *   [FeatureGateConfig.presentationStyle].
 * - **Dumb content delegation**: All layout is delegated to [ShowTimeFeatureGateContent].
 *
 * @param config The [FeatureGateConfig] describing copy, icon, presentation, and pass policy.
 * @param isAdLoading Whether a rewarded ad is currently loading (shows loading spinner on CTA).
 * @param isProPaymentEnabled Whether the "Upgrade to Pro" CTA should be shown.
 * @param onWatchAdClick Callback when the user taps the rewarded ad CTA.
 * @param onUpgradeProClick Callback when the user taps "Upgrade to Pro".
 * @param onDismissRequest Callback when the gate is dismissed.
 * @param rewardedAdManager Optional [RewardedAdManager] for ad preloading. When provided,
 *   [loadAd] is called in a [LaunchedEffect] when the gate first appears.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowTimeFeatureGate(
    config: FeatureGateConfig,
    isAdLoading: Boolean,
    isProPaymentEnabled: Boolean,
    onWatchAdClick: () -> Unit,
    onUpgradeProClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    rewardedAdManager: RewardedAdManager? = null
) {
    // Preload rewarded ad as soon as the gate appears
    LaunchedEffect(Unit) {
        rewardedAdManager?.loadAd()
    }

    val resolvedTitle = config.title.asString()
    val resolvedDescription = config.description.asString()
    val resolvedActionLabel = config.rewardActionLabel.asString()

    when (config.presentationStyle) {
        GatePresentationStyle.BottomSheet -> {
            BottomSheetGate(
                title = resolvedTitle,
                description = resolvedDescription,
                rewardActionLabel = resolvedActionLabel,
                icon = config.icon,
                isAdLoading = isAdLoading,
                isProPaymentEnabled = isProPaymentEnabled,
                onWatchAdClick = onWatchAdClick,
                onUpgradeProClick = onUpgradeProClick,
                onDismissRequest = onDismissRequest,
                modifier = modifier
            )
        }

        GatePresentationStyle.Dialog -> {
            DialogGate(
                title = resolvedTitle,
                description = resolvedDescription,
                rewardActionLabel = resolvedActionLabel,
                icon = config.icon,
                isAdLoading = isAdLoading,
                isProPaymentEnabled = isProPaymentEnabled,
                onWatchAdClick = onWatchAdClick,
                onUpgradeProClick = onUpgradeProClick,
                onDismissRequest = onDismissRequest,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetGate(
    title: String,
    description: String,
    rewardActionLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isAdLoading: Boolean,
    isProPaymentEnabled: Boolean,
    onWatchAdClick: () -> Unit,
    onUpgradeProClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    ShowTimeBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        ShowTimeFeatureGateContent(
            title = title,
            description = description,
            rewardActionLabel = rewardActionLabel,
            icon = icon,
            isAdLoading = isAdLoading,
            isProPaymentEnabled = isProPaymentEnabled,
            onWatchAdClick = onWatchAdClick,
            onUpgradeProClick = onUpgradeProClick,
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
private fun DialogGate(
    title: String,
    description: String,
    rewardActionLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isAdLoading: Boolean,
    isProPaymentEnabled: Boolean,
    onWatchAdClick: () -> Unit,
    onUpgradeProClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = { Text(text = title) },
        text = { Text(text = description) },
        confirmButton = {
            DialogGateActions(
                rewardActionLabel = rewardActionLabel,
                isAdLoading = isAdLoading,
                isProPaymentEnabled = isProPaymentEnabled,
                onWatchAdClick = onWatchAdClick,
                onUpgradeProClick = {
                    onDismissRequest()
                    onUpgradeProClick()
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun DialogGateActions(
    rewardActionLabel: String,
    isAdLoading: Boolean,
    isProPaymentEnabled: Boolean,
    onWatchAdClick: () -> Unit,
    onUpgradeProClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Button(
            onClick = onWatchAdClick,
            enabled = !isAdLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isAdLoading) {
                ShowTimeLoadingIndicator(modifier = Modifier.size(18.dp))
            } else {
                Icon(
                    imageVector = Icons.Rounded.PlayCircle,
                    contentDescription = stringResource(R.string.gate_watch_ad_icon_cd),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(text = rewardActionLabel)
        }

        if (isProPaymentEnabled) {
            OutlinedButton(
                onClick = onUpgradeProClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.gate_upgrade_to_pro))
            }
        }
    }
}
