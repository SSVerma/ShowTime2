package com.ssverma.shared.ads.gate

import androidx.compose.runtime.Immutable
import com.ssverma.shared.ads.quota.PassKey
import java.util.concurrent.TimeUnit

/**
 * Defines how a reward pass is structured when granted through a feature gate.
 *
 * Each policy maps to a [PassKey] and determines whether the reward is
 * time-limited, consumable (slot-based), or a one-shot action unlock.
 */
@Immutable
sealed interface FeaturePassPolicy {

    /** The underlying [PassKey] to grant via [RewardManager]. */
    val passKey: PassKey

    /**
     * A time-limited pass that expires after [durationMs] (with presentation label [durationLabel]).
     */
    @Immutable
    data class TimedPass(
        override val passKey: PassKey,
        val durationMs: Long = TimeUnit.HOURS.toMillis(24),
        val durationLabel: String = "24h"
    ) : FeaturePassPolicy

    /**
     * A consumable slot pass that grants [slotsGranted] additional slots
     * (e.g. extra reminder slots, extra custom list slots).
     */
    @Immutable
    data class ConsumableSlot(
        override val passKey: PassKey,
        val slotsGranted: Int = 1
    ) : FeaturePassPolicy

    /**
     * A one-shot action unlock (e.g. cinema game revive, manual backup).
     */
    @Immutable
    data class ActionUnlock(
        override val passKey: PassKey
    ) : FeaturePassPolicy
}

