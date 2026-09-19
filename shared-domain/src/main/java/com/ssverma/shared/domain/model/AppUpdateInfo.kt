package com.ssverma.shared.domain.model

sealed interface AppUpdateStatus {
    data object UpToDate : AppUpdateStatus

    data class SoftUpdate(
        val currentVersionCode: Long,
        val latestVersionCode: Long,
        val title: String? = null,
        val message: String? = null
    ) : AppUpdateStatus

    data class ForceUpdate(
        val currentVersionCode: Long,
        val minSupportedVersionCode: Long,
        val title: String? = null,
        val message: String? = null
    ) : AppUpdateStatus
}
