package com.ssverma.feature.match.ui.component

import android.app.Activity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.People
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ssverma.feature.match.R
import com.ssverma.shared.ads.gate.FeatureGateConfig
import com.ssverma.shared.ads.gate.FeaturePassPolicy
import com.ssverma.shared.ads.gate.GatePresentationStyle
import com.ssverma.shared.ads.gate.ShowTimeFeatureGate
import com.ssverma.shared.ads.quota.PassKey

@Composable
fun MatchQuotaModal(
    onUnlockPro: () -> Unit,
    onWatchRewardedAd: (Activity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isProPaymentEnabled: Boolean = true
) {
    val context = LocalContext.current
    val activity = context as? Activity

    ShowTimeFeatureGate(
        config = MatchRoomGateConfig,
        isAdLoading = false,
        isProPaymentEnabled = isProPaymentEnabled,
        onWatchAdClick = {
            if (activity != null) {
                onWatchRewardedAd(activity)
            }
        },
        onUpgradeProClick = onUnlockPro,
        onDismissRequest = onDismiss,
        modifier = modifier
    )
}

val MatchRoomPassKey = PassKey("match_room")

private val MatchRoomGateConfig = FeatureGateConfig(
    titleRes = R.string.match_room_free_quota_exceeded,
    descriptionRes = R.string.match_room_unlock_with_pass,
    rewardActionLabelRes = R.string.match_room_watch_video_pass,
    icon = Icons.Rounded.People,
    presentationStyle = GatePresentationStyle.BottomSheet,
    passPolicy = FeaturePassPolicy.TimedPass(MatchRoomPassKey)
)


