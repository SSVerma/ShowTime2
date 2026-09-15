package com.ssverma.feature.tv.ui.details.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.ui.util.findActivity
import com.ssverma.feature.tv.ui.details.TvShowDetailsViewModel
import com.ssverma.shared.ads.gate.FeatureGateConfig
import com.ssverma.shared.ads.gate.FeaturePassPolicy
import com.ssverma.shared.ads.gate.GatePresentationStyle
import com.ssverma.shared.ads.gate.ShowTimeFeatureGate
import com.ssverma.shared.ads.quota.PassKey
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.ui.component.diary.LogAndRateDialog
import com.ssverma.shared.ui.component.notification.NotificationPermissionDialogs
import com.ssverma.shared.ui.component.notification.NotificationPermissionHandler
import com.ssverma.shared.ui.component.section.WhereToWatchActionBottomSheet
import com.ssverma.shared.ui.R as SharedR

private val AiringReminderPassKey = PassKey("airing_reminders")

private val AiringReminderGateConfig = FeatureGateConfig(
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

@Composable
fun TvShowDetailsOverlays(
    tvShow: TvShow,
    viewModel: TvShowDetailsViewModel,
    watchProviderRegion: String,
    diaryEntries: List<DiaryEntry>,
    showLogDialog: Boolean,
    onDismissLogDialog: () -> Unit,
    onSaveDiaryEntry: (DiaryEntry, Boolean) -> Unit,
    notificationPermissionHandler: NotificationPermissionHandler,
    openWatchHub: (ProviderInfo) -> Unit,
    openProPaywall: () -> Unit
) {
    val context = LocalContext.current
    val selectedProviderPayload =
        viewModel.selectedProviderForAction.collectAsStateWithLifecycle().value
    val isQuotaGateVisible = viewModel.isQuotaGateVisible.collectAsStateWithLifecycle().value
    val isAdLoading = viewModel.isAdLoading.collectAsStateWithLifecycle().value
    val isBillingEnabled =
        viewModel.billingRepository.isBillingEnabled.collectAsStateWithLifecycle().value

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
                viewModel.saveDiaryEntry(entry)
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
            affiliateRepository = viewModel.affiliateRepository,
            onDismissRequest = viewModel::dismissProviderAction,
            onBrowseHubClick = { provider ->
                openWatchHub(provider)
            }
        )
    }

    if (isQuotaGateVisible) {
        ShowTimeFeatureGate(
            config = AiringReminderGateConfig,
            isAdLoading = isAdLoading,
            isProPaymentEnabled = isBillingEnabled,
            onWatchAdClick = {
                val activity = context.findActivity()
                if (activity != null) {
                    viewModel.onWatchAdForReminderPass(activity, tvShow)
                }
            },
            onUpgradeProClick = {
                viewModel.dismissQuotaGate()
                openProPaywall()
            },
            onDismissRequest = { viewModel.dismissQuotaGate() }
        )
    }
}
