package com.ssverma.shared.ads.gate.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ads.R

/**
 * Dumb layout composable for a feature gate's content. Contains zero business logic;
 * all state is pre-resolved and passed in as parameters.
 *
 * @param title Resolved gate title.
 * @param description Resolved gate description.
 * @param rewardActionLabel Resolved label for the rewarded ad CTA button.
 * @param icon Leading icon displayed inside a circular surface.
 * @param isAdLoading Whether the rewarded ad is currently loading (shows spinner, disables CTA).
 * @param isProPaymentEnabled Whether the "Upgrade to Pro" button should be shown.
 * @param onWatchAdClick Callback when the user taps the rewarded ad CTA.
 * @param onUpgradeProClick Callback when the user taps the "Upgrade to Pro" CTA.
 * @param onDismissRequest Callback when the user taps "Cancel".
 */
@Composable
internal fun ShowTimeFeatureGateContent(
    title: String,
    description: String,
    rewardActionLabel: String,
    icon: ImageVector,
    isAdLoading: Boolean,
    isProPaymentEnabled: Boolean,
    onWatchAdClick: () -> Unit,
    onUpgradeProClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
            .padding(bottom = MaterialTheme.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GateIcon(icon = icon)

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        GateTitle(title = title)

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

        GateDescription(description = description)

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        GateActions(
            rewardActionLabel = rewardActionLabel,
            isAdLoading = isAdLoading,
            isProPaymentEnabled = isProPaymentEnabled,
            onWatchAdClick = onWatchAdClick,
            onUpgradeProClick = onUpgradeProClick,
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
private fun GateIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier.size(52.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun GateTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
private fun GateDescription(
    description: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
private fun GateActions(
    rewardActionLabel: String,
    isAdLoading: Boolean,
    isProPaymentEnabled: Boolean,
    onWatchAdClick: () -> Unit,
    onUpgradeProClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        if (isProPaymentEnabled) {
            ProUpgradeButton(onClick = onUpgradeProClick)

            RewardedAdButton(
                label = rewardActionLabel,
                isAdLoading = isAdLoading,
                isPrimary = false,
                onClick = onWatchAdClick
            )
        } else {
            RewardedAdButton(
                label = rewardActionLabel,
                isAdLoading = isAdLoading,
                isPrimary = true,
                onClick = onWatchAdClick
            )
        }

        CancelButton(onClick = onDismissRequest)
    }
}

@Composable
private fun ProUpgradeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = stringResource(R.string.gate_pro_icon_cd),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
        Text(
            text = stringResource(R.string.gate_upgrade_to_pro),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RewardedAdButton(
    label: String,
    isAdLoading: Boolean,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonModifier = modifier
        .fillMaxWidth()
        .height(50.dp)

    if (isPrimary) {
        Button(
            onClick = onClick,
            enabled = !isAdLoading,
            shape = MaterialTheme.shapes.large,
            modifier = buttonModifier
        ) {
            RewardedAdButtonContent(
                label = label,
                isAdLoading = isAdLoading,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = !isAdLoading,
            shape = MaterialTheme.shapes.large,
            modifier = buttonModifier
        ) {
            RewardedAdButtonContent(
                label = label,
                isAdLoading = isAdLoading,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RewardedAdButtonContent(
    label: String,
    isAdLoading: Boolean,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier
) {
    if (isAdLoading) {
        ShowTimeLoadingIndicator(
            modifier = modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
    } else {
        Icon(
            imageVector = Icons.Rounded.PlayCircle,
            contentDescription = stringResource(R.string.gate_watch_ad_icon_cd),
            modifier = modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
    }
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = fontWeight
    )
}

@Composable
private fun CancelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = onClick) {
            Text(
                text = stringResource(android.R.string.cancel),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
