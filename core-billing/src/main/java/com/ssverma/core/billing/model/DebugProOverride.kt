package com.ssverma.core.billing.model

import kotlinx.coroutines.flow.StateFlow

enum class DebugProOverride {
    AUTO,
    FORCE_ACTIVE,
    FORCE_INACTIVE
}

interface ProOverrideProvider {
    val proOverride: StateFlow<DebugProOverride>
}
