package com.ssverma.shared.ads.gate

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.ssverma.core.ui.UiText
import com.ssverma.core.ui.asUiText

/**
 * Configuration data class for a [ShowTimeFeatureGate].
 *
 * Encapsulates textual content via Compose-stable [UiText] (supporting both static
 * string resources and dynamic runtime strings), visual presentation style, leading icon,
 * and reward pass semantics.
 *
 * @param title Gate title represented as a [UiText].
 * @param description Gate description represented as a [UiText].
 * @param rewardActionLabel Rewarded ad CTA label represented as a [UiText].
 * @param icon Leading icon displayed in the gate header.
 * @param presentationStyle Visual presentation style ([GatePresentationStyle.BottomSheet] or [GatePresentationStyle.Dialog]).
 * @param passPolicy Optional [FeaturePassPolicy] describing the reward pass semantics.
 */
@Immutable
data class FeatureGateConfig(
    val title: UiText,
    val description: UiText,
    val rewardActionLabel: UiText,
    val icon: ImageVector = Icons.Rounded.Lock,
    val presentationStyle: GatePresentationStyle = GatePresentationStyle.BottomSheet,
    val passPolicy: FeaturePassPolicy? = null
) {
    /**
     * Convenience secondary constructor allowing callers to pass raw [StringRes] IDs directly.
     */
    constructor(
        @StringRes titleRes: Int,
        @StringRes descriptionRes: Int,
        @StringRes rewardActionLabelRes: Int,
        icon: ImageVector = Icons.Rounded.Lock,
        presentationStyle: GatePresentationStyle = GatePresentationStyle.BottomSheet,
        passPolicy: FeaturePassPolicy? = null
    ) : this(
        title = titleRes.asUiText(),
        description = descriptionRes.asUiText(),
        rewardActionLabel = rewardActionLabelRes.asUiText(),
        icon = icon,
        presentationStyle = presentationStyle,
        passPolicy = passPolicy
    )
}

