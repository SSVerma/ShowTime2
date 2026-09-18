package com.ssverma.feature.tv.ui.details.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.runtime.Composable
import com.ssverma.feature.tv.ui.details.TvProviderActionPayload
import com.ssverma.shared.ads.gate.FeatureGateConfig
import com.ssverma.shared.ads.gate.FeaturePassPolicy
import com.ssverma.shared.ads.gate.GatePresentationStyle
import com.ssverma.shared.ads.gate.ShowTimeFeatureGate
import com.ssverma.shared.ads.quota.PassKey
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.repository.AffiliateRepository
import com.ssverma.shared.ui.component.diary.LogAndRateDialog
import com.ssverma.shared.ui.component.notification.NotificationPermissionDialogs
import com.ssverma.shared.ui.component.notification.NotificationPermissionHandler
import com.ssverma.shared.ui.component.reminder.AiringReminderSheet
import com.ssverma.shared.ui.component.section.WhereToWatchActionBottomSheet
import com.ssverma.shared.ui.R as SharedR

val AiringReminderPassKey = PassKey("airing_reminders")

val AiringReminderGateConfig = FeatureGateConfig(
    titleRes = SharedR.string.reminder_quota_title,
    descriptionRes = SharedR.string.reminder_quota_desc,
    rewardActionLabelRes = SharedR.string.reminder_quota_reward_label,
    icon = Icons.Rounded.Lock,
    presentationStyle = GatePresentationStyle.BottomSheet,
    passPolicy = FeaturePassPolicy.ConsumableSlot(
        passKey = AiringReminderPassKey,
        slotsGranted = 1
    )
)

val CommunityCommentsPassKey = PassKey("community_comments")

val CommunityCommentsGateConfig = FeatureGateConfig(
    titleRes = SharedR.string.discussion_quota_title,
    descriptionRes = SharedR.string.discussion_quota_desc,
    rewardActionLabelRes = SharedR.string.discussion_quota_reward_label,
    icon = Icons.Rounded.Lock,
    presentationStyle = GatePresentationStyle.BottomSheet,
    passPolicy = FeaturePassPolicy.ConsumableSlot(
        passKey = CommunityCommentsPassKey,
        slotsGranted = 3
    )
)

@Composable
fun TvShowDetailsOverlays(
    tvShow: TvShow,
    notificationPermissionHandler: NotificationPermissionHandler,
    showLogDialog: Boolean,
    diaryEntries: List<DiaryEntry>,
    onDismissLogDialog: () -> Unit,
    onSaveDiaryEntry: (DiaryEntry, Boolean) -> Unit,
    selectedProviderPayload: TvProviderActionPayload?,
    watchProviderRegion: String,
    affiliateRepository: AffiliateRepository,
    onDismissProviderAction: () -> Unit,
    onBrowseWatchHub: (ProviderInfo) -> Unit,
    activeGateConfig: FeatureGateConfig?,
    isAdLoading: Boolean,
    isProPaymentEnabled: Boolean,
    onWatchAd: () -> Unit,
    onUpgradeProClick: () -> Unit,
    onDismissQuotaGate: () -> Unit,
    isReminderSheetVisible: Boolean,
    hasReminder: Boolean,
    reminderLeadDays: Int,
    reminderNotificationHour: Int,
    reminderNotificationMinute: Int,
    onScheduleReminder: (leadDays: Int, hour: Int, minute: Int) -> Unit,
    onRemoveReminder: () -> Unit,
    onDismissReminderSheet: () -> Unit
) {
    NotificationPermissionDialogs(handler = notificationPermissionHandler)

    if (showLogDialog) {
        val wasExisting = diaryEntries.isNotEmpty()
        LogAndRateDialog(
            mediaId = tvShow.id,
            mediaType = MediaType.Tv,
            title = tvShow.title,
            posterImageUrl = tvShow.posterImageUrl,
            backdropImageUrl = tvShow.backdropImageUrl,
            releaseDate = tvShow.firstAirDate?.toString().orEmpty(),
            tmdbRating = tvShow.voteAvg,
            existingEntry = diaryEntries.firstOrNull(),
            onDismiss = onDismissLogDialog,
            onSave = { entry ->
                onDismissLogDialog()
                onSaveDiaryEntry(entry, wasExisting)
            }
        )
    }

    selectedProviderPayload?.let { payload ->
        val currentWatchProvider = tvShow.watchProviders[watchProviderRegion]
        WhereToWatchActionBottomSheet(
            provider = payload.provider,
            mediaTitle = tvShow.title,
            categoryName = payload.category,
            watchProviderLink = currentWatchProvider?.link,
            region = watchProviderRegion,
            affiliateRepository = affiliateRepository,
            onDismissRequest = onDismissProviderAction,
            onBrowseHubClick = onBrowseWatchHub
        )
    }

    if (activeGateConfig != null) {
        ShowTimeFeatureGate(
            config = activeGateConfig,
            isAdLoading = isAdLoading,
            isProPaymentEnabled = isProPaymentEnabled,
            onWatchAdClick = onWatchAd,
            onUpgradeProClick = onUpgradeProClick,
            onDismissRequest = onDismissQuotaGate
        )
    }

    if (isReminderSheetVisible) {
        val nextEpisode = tvShow.nextEpisodeToAir
        val airDate = nextEpisode?.airDate
        if (airDate != null) {
            val epSubtitle = "S${nextEpisode.seasonNumber}E${nextEpisode.episodeNumber}" +
                    if (!nextEpisode.title.isNullOrBlank()) " · ${nextEpisode.title}" else ""
            AiringReminderSheet(
                mediaTitle = tvShow.title,
                episodeSubtitle = epSubtitle,
                airDate = airDate,
                hasReminder = hasReminder,
                initialLeadDays = reminderLeadDays,
                initialHour = reminderNotificationHour,
                initialMinute = reminderNotificationMinute,
                onConfirm = onScheduleReminder,
                onRemove = onRemoveReminder,
                onDismissRequest = onDismissReminderSheet
            )
        }
    }
}
