package com.ssverma.feature.community.ui.discussions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ssverma.common.ui.community.DiscussionsScreenContent
import com.ssverma.common.ui.paywall.ProPaywallBottomSheet
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.ui.asString
import com.ssverma.core.ui.util.findActivity
import com.ssverma.feature.community.analytics.CommunityAnalyticsScreenName
import com.ssverma.shared.ads.gate.FeatureGateConfig
import com.ssverma.shared.ads.gate.FeaturePassPolicy
import com.ssverma.shared.ads.gate.GatePresentationStyle
import com.ssverma.shared.ads.gate.ShowTimeFeatureGate
import com.ssverma.shared.ads.quota.PassKey
import com.ssverma.shared.ui.R as SharedR

private val CommunityCommentsPassKey = PassKey("community_comments")

private val CommunityCommentsGateConfig = FeatureGateConfig(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionsScreen(
    viewModel: DiscussionsViewModel,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    TrackScreenView(screenName = CommunityAnalyticsScreenName.COMMUNITY_DISCUSSIONS)

    val context = LocalContext.current
    val activity = context.findActivity()

    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    DiscussionsScreenContent(
        comments = uiState.comments,
        isLoading = uiState.isLoading,
        isLoadingMore = uiState.isLoadingMore,
        canLoadMore = uiState.canLoadMore,
        onLoadMore = viewModel::loadMoreComments,
        selectedFilter = selectedFilter,
        onFilterSelected = viewModel::onFilterSelected,
        mediaTitle = viewModel.title,
        posterImageUrl = viewModel.poster,
        onBackPressed = onBackPressed,
        onPostComment = viewModel::postComment,
        onToggleUpvote = viewModel::toggleCommentUpvote,
        onEditComment = viewModel::editComment,
        onReportComment = viewModel::reportComment,
        onDeleteComment = viewModel::deleteComment,
        modifier = modifier
    )

    if (uiState.isQuotaGateVisible) {
        ShowTimeFeatureGate(
            config = CommunityCommentsGateConfig,
            isAdLoading = uiState.isAdLoading,
            isProPaymentEnabled = uiState.isProPaymentEnabled,
            onWatchAdClick = {
                activity?.let { viewModel.watchAdForCommentPass(it) }
            },
            onUpgradeProClick = viewModel::openPaywall,
            onDismissRequest = viewModel::dismissQuotaGate
        )
    }

    if (uiState.isPaywallVisible) {
        ProPaywallBottomSheet(
            products = uiState.availableProducts,
            isProActive = uiState.isProActive,
            isRestoring = uiState.isRestoringPurchases,
            isPurchasing = uiState.isPurchasingProduct,
            errorMessage = uiState.paywallErrorMessage?.asString(),
            onPurchaseClick = { act, product -> viewModel.purchaseProduct(act, product) },
            onRestoreClick = viewModel::restorePurchases,
            onDismissRequest = viewModel::dismissPaywall
        )
    }
}
