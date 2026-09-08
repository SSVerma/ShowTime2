package com.ssverma.core.ui.modifier

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity

/**
 * A [NestedScrollConnection] that consumes unconsumed vertical scroll deltas
 * and velocity from child scrollables (such as [androidx.compose.foundation.lazy.LazyColumn]
 * or [androidx.compose.foundation.verticalScroll]) before they can propagate to parent
 * bottom sheet containers (e.g. [androidx.compose.material3.ModalBottomSheet]).
 *
 * This completely eliminates the jitter, stuttering, and vertical sheet fluctuation
 * that occurs in Material 3 Compose ModalBottomSheet during scroll gestures.
 */
class SheetFluctuationFreeNestedScrollConnection : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return if (available.y != 0f) Offset(x = 0f, y = available.y) else Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        return if (available.y != 0f) Velocity(x = 0f, y = available.y) else Velocity.Zero
    }
}

/**
 * Reusable modifier to prevent bottom sheet fluctuation/jitter on inner scroll gestures.
 */
fun Modifier.preventSheetFluctuation(): Modifier = composed {
    val connection = remember { SheetFluctuationFreeNestedScrollConnection() }
    this.nestedScroll(connection)
}
