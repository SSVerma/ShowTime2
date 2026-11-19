package com.ssverma.shared.domain.model.community

import kotlinx.coroutines.flow.StateFlow

enum class DebugRatingOverride {
    AUTO,
    FORCE_COLD_START,
    FORCE_BELOW_THRESHOLD,
    FORCE_ACTIVE_HIGH,
    FORCE_USER_RATED
}

interface RatingOverrideProvider {
    val ratingOverride: StateFlow<DebugRatingOverride>
    val ratingThreshold: StateFlow<Int>
}
