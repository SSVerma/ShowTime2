package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AppConfigRepository {
    val appTheme: Flow<AppTheme>

    val isDynamicColorEnabled: Flow<Boolean>

    val isAppInfoBottomSheetDismissed: Flow<Boolean>

    val watchProviderRegion: StateFlow<String>

    val isTranslationEnabled: StateFlow<Boolean>

    val contentLanguage: StateFlow<String>

    val preferredOriginalLanguage: StateFlow<String>

    val isAnalyticsEnabled: Flow<Boolean>

    val isNotificationsEnabled: Flow<Boolean>

    val userStreamingSubscriptions: Flow<Set<Int>>

    val reminderNotificationHour: Flow<Int>

    val reminderNotificationMinute: Flow<Int>

    val reminderLeadDays: Flow<Int>

    suspend fun updateAppTheme(theme: AppTheme)

    suspend fun updateDynamicColor(enabled: Boolean)

    suspend fun dismissAppInfoBottomSheet()

    suspend fun updateWatchProviderRegion(regionCode: String)

    suspend fun updateStreamingSubscriptions(providerIds: Set<Int>)

    suspend fun updateTranslationEnabled(enabled: Boolean)

    suspend fun updateContentLanguage(languageCode: String)

    suspend fun updatePreferredOriginalLanguage(languageCode: String)

    suspend fun updateAnalyticsEnabled(enabled: Boolean)

    suspend fun updateNotificationsEnabled(enabled: Boolean)

    val isReleaseRadarEnabled: Flow<Boolean>

    suspend fun updateReleaseRadarEnabled(enabled: Boolean)

    suspend fun updateReminderNotificationTime(hour: Int, minute: Int)

    suspend fun updateReminderLeadDays(leadDays: Int)

    /** Epoch millis when the notification permission shelf was last dismissed by the user. */
    val notificationShelfLastDismissedMs: Flow<Long>

    /** Record the current time as the last dismissal of the notification permission shelf. */
    suspend fun dismissNotificationShelf()

    /** Features that the user has tapped/explored, used to dynamically dismiss NEW badges. */
    val acknowledgedFeatures: Flow<Set<String>>

    /** Mark a feature as acknowledged by the user. */
    suspend fun acknowledgeFeature(featureId: String)

    /** Highest app versionCode the user has launched/seen, used for What's New & onboarding gating. */
    val lastSeenVersionCode: Flow<Int>

    /** Update the highest seen versionCode. */
    suspend fun updateLastSeenVersionCode(versionCode: Int)

    /** Campaign ID of the What's New tour the user has completed (e.g. "2.0.0"). */
    val lastSeenWhatsNewCampaign: Flow<String>

    /** Update the completed What's New campaign ID. */
    suspend fun updateLastSeenWhatsNewCampaign(campaignId: String)

    /** Remote kill-switch for What's New tour. */
    val isWhatsNewEnabled: Flow<Boolean>

    /** Current active campaign ID from Remote Config (defaults to "2.0.0"). */
    val whatsNewCampaignId: Flow<String>

    /** Optional filter for active features in the tour (comma-separated feature IDs). */
    val whatsNewFeatureFilter: Flow<String>

    /** Whether the user has completed or skipped the first-install onboarding journey. */
    val hasCompletedOnboarding: Flow<Boolean>

    /** Update the onboarding completion status. */
    suspend fun updateHasCompletedOnboarding(completed: Boolean)

    /** Genre IDs selected by the user during onboarding to seed personalized recommendations. */
    val userSeededGenres: Flow<Set<Int>>

    /** Save seeded genre IDs chosen by the user. */
    suspend fun updateSeededGenres(genreIds: Set<Int>)
}
