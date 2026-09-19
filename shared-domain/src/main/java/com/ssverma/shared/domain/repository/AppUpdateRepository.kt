package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.AppUpdateStatus
import kotlinx.coroutines.flow.Flow

interface AppUpdateRepository {
    fun observeAppUpdateStatus(currentVersionCode: Long): Flow<AppUpdateStatus>
    suspend fun checkAppUpdateStatus(currentVersionCode: Long): AppUpdateStatus
    suspend fun dismissSoftUpdate(versionCode: Long)
    suspend fun isSoftUpdateDismissed(versionCode: Long): Boolean
}
