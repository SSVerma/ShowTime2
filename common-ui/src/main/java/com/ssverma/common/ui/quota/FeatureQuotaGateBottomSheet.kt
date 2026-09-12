package com.ssverma.common.ui.quota

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ssverma.shared.ui.component.quota.FeatureQuotaGateBottomSheet as SharedFeatureQuotaGateBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureQuotaGateBottomSheet(
    title: String,
    description: String,
    rewardActionLabel: String,
    onWatchAdClick: () -> Unit,
    onUpgradeProClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    isAdLoading: Boolean = false,
    isProPaymentEnabled: Boolean = true,
    icon: ImageVector = Icons.Rounded.Lock,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    SharedFeatureQuotaGateBottomSheet(
        title = title,
        description = description,
        rewardActionLabel = rewardActionLabel,
        onWatchAdClick = onWatchAdClick,
        onUpgradeProClick = onUpgradeProClick,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        isAdLoading = isAdLoading,
        isProPaymentEnabled = isProPaymentEnabled,
        icon = icon,
        sheetState = sheetState
    )
}
