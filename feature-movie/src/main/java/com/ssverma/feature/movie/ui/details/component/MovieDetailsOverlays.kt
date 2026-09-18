package com.ssverma.feature.movie.ui.details.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.runtime.Composable
import com.ssverma.shared.ads.gate.FeatureGateConfig
import com.ssverma.shared.ads.gate.FeaturePassPolicy
import com.ssverma.shared.ads.gate.GatePresentationStyle
import com.ssverma.shared.ads.gate.ShowTimeFeatureGate
import com.ssverma.shared.ads.quota.PassKey
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.repository.AffiliateRepository
import com.ssverma.shared.ui.component.diary.LogAndRateDialog
import com.ssverma.shared.ui.component.notification.NotificationPermissionDialogs
import com.ssverma.shared.ui.component.notification.NotificationPermissionHandler
import com.ssverma.shared.ui.component.reminder.AiringReminderSheet
import com.ssverma.shared.ui.component.section.WhereToWatchActionBottomSheet
import com.ssverma.feature.movie.ui.details.ProviderActionPayload
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
fun MovieDetailsOverlays(
    movie: Movie,
    notificationPermissionHandler: NotificationPermissionHandler,
    showLogDialog: Boolean,
    diaryEntries: List<DiaryEntry>,
    onDismissLogDialog: () -> Unit,
    onSaveDiaryEntry: (DiaryEntry) -> Unit,
    selectedProviderPayload: ProviderActionPayload?,
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
        LogAndRateDialog(
            mediaId = movie.id,
            mediaType = MediaType.Movie,
            title = movie.title,
            posterImageUrl = movie.posterImageUrl,
            backdropImageUrl = movie.backdropImageUrl,
            releaseDate = movie.releaseDate?.toString().orEmpty(),
            tmdbRating = movie.voteAvg,
            existingEntry = diaryEntries.firstOrNull(),
            onDismiss = onDismissLogDialog,
            onSave = onSaveDiaryEntry
        )
    }

    selectedProviderPayload?.let { payload ->
        val currentWatchProvider = movie.watchProviders[watchProviderRegion]
        WhereToWatchActionBottomSheet(
            provider = payload.provider,
            mediaTitle = movie.title,
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
        val targetReleaseDate = movie.nextFutureReleaseDate ?: movie.releaseDate
        if (targetReleaseDate != null) {
            AiringReminderSheet(
                mediaTitle = movie.title,
                airDate = targetReleaseDate,
                hasReminder = hasReminder,
                isMovie = true,
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
