package com.ssverma.shared.ads.gate

import androidx.compose.runtime.Immutable
import com.ssverma.shared.ads.quota.PassKey
import java.util.concurrent.TimeUnit

/**
 * Standardized reward pass duration presets across ShowTime.
 */
object PassDurations {
    /** 2 hours: Ideal for single export sessions (Receipts, Wrapped story cards, Secret Share themes). */
    val SHORT_EXPORT_WINDOW_MS: Long = TimeUnit.HOURS.toMillis(2)

    /** 6 hours: Ideal for active browsing / sync sessions (Multi-Service Filter, Trakt Sync, Taste Profile). */
    val BROWSING_SESSION_MS: Long = TimeUnit.HOURS.toMillis(6)

    /** 30 minutes: Action window for single-action unlocks (Manual Backup, Match Room bonus deck). */
    val ACTION_UNLOCK_WINDOW_MS: Long = TimeUnit.MINUTES.toMillis(30)

    /** 24 hours: Default daily pass. */
    val DAILY_PASS_MS: Long = TimeUnit.HOURS.toMillis(24)
}

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
        val durationMs: Long = PassDurations.BROWSING_SESSION_MS,
        val durationLabel: String = "6h"
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

