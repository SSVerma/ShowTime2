package com.ssverma.showtime.analytics

import com.ssverma.core.analytics.AnalyticsDispatcher
import com.ssverma.shared.domain.repository.AppConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsSyncManager @Inject constructor(
    private val appConfigRepository: AppConfigRepository,
    private val analyticsDispatcher: AnalyticsDispatcher,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun startSync() {
        appConfigRepository.isAnalyticsEnabled
            .onEach { enabled ->
                analyticsDispatcher.setAnalyticsEnabled(enabled)
            }
            .launchIn(scope)
    }
}
