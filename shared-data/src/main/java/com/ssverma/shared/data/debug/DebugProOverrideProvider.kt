package com.ssverma.shared.data.debug

import com.ssverma.core.billing.model.DebugProOverride
import com.ssverma.core.billing.model.ProOverrideProvider
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugProOverrideProvider @Inject constructor(
    private val debugConfigManager: DebugConfigManager
) : ProOverrideProvider {
    override val proOverride: StateFlow<DebugProOverride> = debugConfigManager.proOverride
}
