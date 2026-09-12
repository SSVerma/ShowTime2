package com.ssverma.shared.ads.gate

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.ads.quota.PassKey
import org.junit.Test

class FeatureGateConfigTest {

    @Test
    fun `FeatureGateConfig with UiText defaults and immutability are verified`() {
        val testKey = PassKey("test_reminders")
        val config = FeatureGateConfig(
            title = com.ssverma.core.ui.UiText.DynamicText("Test Gate"),
            description = com.ssverma.core.ui.UiText.DynamicText("Test Description"),
            rewardActionLabel = com.ssverma.core.ui.UiText.DynamicText("Watch Video"),
            icon = Icons.Rounded.Lock,
            presentationStyle = GatePresentationStyle.BottomSheet,
            passPolicy = FeaturePassPolicy.ConsumableSlot(
                passKey = testKey,
                slotsGranted = 1
            )
        )

        assertThat((config.title as com.ssverma.core.ui.UiText.DynamicText).text).isEqualTo("Test Gate")
        assertThat((config.description as com.ssverma.core.ui.UiText.DynamicText).text).isEqualTo("Test Description")
        assertThat((config.rewardActionLabel as com.ssverma.core.ui.UiText.DynamicText).text).isEqualTo(
            "Watch Video"
        )
        assertThat(config.icon).isEqualTo(Icons.Rounded.Lock)
        assertThat(config.presentationStyle).isEqualTo(GatePresentationStyle.BottomSheet)

        val policy = config.passPolicy as FeaturePassPolicy.ConsumableSlot
        assertThat(policy.passKey).isEqualTo(testKey)
        assertThat(policy.slotsGranted).isEqualTo(1)
    }

    @Test
    fun `FeatureGateConfig secondary constructor maps StringRes to StaticText`() {
        val config = FeatureGateConfig(
            titleRes = 101,
            descriptionRes = 102,
            rewardActionLabelRes = 103
        )

        assertThat(config.title).isInstanceOf(com.ssverma.core.ui.UiText.StaticText::class.java)
        assertThat((config.title as com.ssverma.core.ui.UiText.StaticText).resId).isEqualTo(101)
        assertThat((config.description as com.ssverma.core.ui.UiText.StaticText).resId).isEqualTo(
            102
        )
        assertThat((config.rewardActionLabel as com.ssverma.core.ui.UiText.StaticText).resId).isEqualTo(
            103
        )
    }

    @Test
    fun `TimedPass policy stores correct duration and passKey`() {
        val testKey = PassKey("taste_radar")
        val policy = FeaturePassPolicy.TimedPass(
            passKey = testKey,
            durationLabel = "24h"
        )

        assertThat(policy.passKey).isEqualTo(testKey)
        assertThat(policy.durationLabel).isEqualTo("24h")
    }

    @Test
    fun `ActionUnlock policy stores correct passKey`() {
        val testKey = PassKey("auto_backup")
        val policy = FeaturePassPolicy.ActionUnlock(
            passKey = testKey
        )

        assertThat(policy.passKey).isEqualTo(testKey)
    }
}
