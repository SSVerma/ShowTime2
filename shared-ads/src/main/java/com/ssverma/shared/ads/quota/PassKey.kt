package com.ssverma.shared.ads.quota

import androidx.compose.runtime.Immutable

/**
 * A strongly-typed identifier representing a unique reward pass capability.
 *
 * Feature modules define their own [PassKey] constants locally (e.g. `val TraktSyncPassKey = PassKey("trakt_sync")`),
 * keeping `:shared-ads` completely decoupled from specific vertical feature implementations.
 */
@Immutable
@JvmInline
value class PassKey(val value: String) {
    override fun toString(): String = value
}
