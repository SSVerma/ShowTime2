package com.ssverma.feature.community.ui.discussions

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.common.ui.R as CommonR
import com.ssverma.common.ui.community.CommentUiModel
import com.ssverma.common.ui.community.toUiModel
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.analytics.Analytics
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.billing.model.PurchaseResult
import com.ssverma.core.ui.UiText
import com.ssverma.feature.community.analytics.DiscussionAnalyticsEvent
import com.ssverma.shared.analytics.asAnalyticsValue
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.DeleteCommentParams
import com.ssverma.shared.domain.model.community.DiscussionTarget
import com.ssverma.shared.domain.model.community.EditCommentArgs
import com.ssverma.shared.domain.model.community.EditCommentParams
import com.ssverma.shared.domain.model.community.PostCommentArgs
import com.ssverma.shared.domain.model.community.PostCommentParams
import com.ssverma.shared.domain.model.community.ReportCommentArgs
import com.ssverma.shared.domain.model.community.ReportCommentParams
import com.ssverma.shared.domain.model.community.ThreadFilter
import com.ssverma.shared.domain.model.community.ToggleCommentUpvoteParams
import com.ssverma.shared.domain.usecase.community.DeleteCommentUseCase
import com.ssverma.shared.domain.usecase.community.EditCommentUseCase
import com.ssverma.shared.domain.usecase.community.FilterAndSortCommentsUseCase
import com.ssverma.shared.domain.usecase.community.GetDiscussionsUseCase
import com.ssverma.shared.domain.usecase.community.PostCommentResult
import com.ssverma.shared.domain.usecase.community.PostCommentUseCase
import com.ssverma.shared.domain.usecase.community.ReportCommentUseCase
import com.ssverma.shared.domain.usecase.community.ToggleCommentUpvoteUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DiscussionsViewModel.Factory::class)
class DiscussionsViewModel @AssistedInject constructor(
    private val getDiscussionsUseCase: GetDiscussionsUseCase,
    private val postCommentUseCase: PostCommentUseCase,
    private val editCommentUseCase: EditCommentUseCase,
    private val reportCommentUseCase: ReportCommentUseCase,
    private val toggleCommentUpvoteUseCase: ToggleCommentUpvoteUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val filterAndSortCommentsUseCase: FilterAndSortCommentsUseCase,
    private val billingRepository: BillingRepository,
    private val rewardManager: RewardManager,
    private val rewardedAdManager: RewardedAdManager,
    private val analytics: Analytics,
    @param:ApplicationContext private val context: Context,
    @Assisted("discussionTarget") private val discussionTarget: DiscussionTarget,
    @Assisted("mediaTitle") private val mediaTitle: String?,
    @Assisted("posterImageUrl") private val posterImageUrl: String?,
    @Assisted("backdropImageUrl") private val backdropImageUrl: String?
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("discussionTarget") discussionTarget: DiscussionTarget,
            @Assisted("mediaTitle") mediaTitle: String?,
            @Assisted("posterImageUrl") posterImageUrl: String? = null,
            @Assisted("backdropImageUrl") backdropImageUrl: String? = null
        ): DiscussionsViewModel
    }

    val title: String? = mediaTitle
    val poster: String? = posterImageUrl

    private val _selectedFilter = MutableStateFlow(ThreadFilter.ALL)
    val selectedFilter: StateFlow<ThreadFilter> = _selectedFilter.asStateFlow()

    private val _locallyReportedCommentIds = MutableStateFlow<Set<String>>(emptySet())
    private val _uiState = MutableStateFlow(DiscussionsUiState())
    val uiState: StateFlow<DiscussionsUiState> = _uiState.asStateFlow()

    private var pendingCommentParams: PostCommentParams? = null

    val discussions: StateFlow<List<Comment>> = getDiscussionsUseCase(discussionTarget).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        rewardedAdManager.loadAd()

        viewModelScope.launch {
            billingRepository.isProActive.collectLatest { isPro ->
                _uiState.update { it.copy(isProActive = isPro) }
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(availableProducts = billingRepository.getAvailableProducts()) }
        }

        viewModelScope.launch {
            billingRepository.purchaseEvents.collect { event ->
                when (event) {
                    is PurchaseResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isPurchasingProduct = false,
                                isPaywallVisible = false,
                                paywallErrorMessage = null
                            )
                        }
                    }

                    is PurchaseResult.Error -> {
                        val errorText = if (!event.message.isNullOrBlank()) {
                            UiText.DynamicText(event.message)
                        } else {
                            UiText.StaticText(CommonR.string.purchase_failed)
                        }
                        _uiState.update {
                            it.copy(
                                isPurchasingProduct = false,
                                paywallErrorMessage = errorText
                            )
                        }
                    }

                    is PurchaseResult.UserCancelled -> {
                        _uiState.update {
                            it.copy(
                                isPurchasingProduct = false,
                                paywallErrorMessage = null
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            combine(
                getDiscussionsUseCase(discussionTarget),
                _selectedFilter,
                _locallyReportedCommentIds
            ) { rawComments, filter, reportedIds ->
                filterAndSortCommentsUseCase(
                    comments = rawComments,
                    filter = filter,
                    excludedCommentIds = reportedIds
                ).map { comment ->
                    comment.toUiModel(context = context)
                }
            }.collectLatest { models ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        comments = models
                    )
                }
            }
        }
    }

    val uiComments: StateFlow<List<CommentUiModel>> = uiState.map { it.comments }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onFilterSelected(filter: ThreadFilter) {
        analytics.logEvent(
            DiscussionAnalyticsEvent.FilterSelected(filter = filter.name)
        )
        _selectedFilter.value = filter
    }

    fun postComment(
        content: String,
        isSpoiler: Boolean,
        parentId: String? = null,
        replyToAuthorName: String? = null
    ) {
        val params = PostCommentParams(
            target = discussionTarget,
            content = content,
            isSpoiler = isSpoiler,
            parentId = parentId,
            replyToAuthorName = replyToAuthorName,
            mediaTitle = mediaTitle,
            posterImageUrl = posterImageUrl,
            backdropImageUrl = backdropImageUrl
        )
        postCommentInternal(params)
    }

    fun postComment(args: PostCommentArgs) {
        postComment(
            content = args.content,
            isSpoiler = args.isSpoiler,
            parentId = args.parentId,
            replyToAuthorName = args.replyToAuthor
        )
    }

    private fun postCommentInternal(params: PostCommentParams) {
        viewModelScope.launch {
            val isPro = _uiState.value.isProActive
            when (postCommentUseCase(params, isProActive = isPro)) {
                is PostCommentResult.Success -> {
                    analytics.logEvent(
                        DiscussionAnalyticsEvent.CommentPosted(
                            targetType = discussionTarget.mediaType.asAnalyticsValue(),
                            targetId = discussionTarget.mediaId,
                            isSpoiler = params.isSpoiler,
                            isReply = params.parentId != null
                        )
                    )
                    pendingCommentParams = null
                }

                is PostCommentResult.QuotaExceeded -> {
                    pendingCommentParams = params
                    rewardedAdManager.loadAd()
                    _uiState.update { it.copy(isQuotaGateVisible = true) }
                }

                is PostCommentResult.Error -> {
                    // Handled gracefully
                }
            }
        }
    }

    fun dismissQuotaGate() {
        _uiState.update { it.copy(isQuotaGateVisible = false, isAdLoading = false) }
    }

    fun watchAdForCommentPass(activity: Activity) {
        _uiState.update { it.copy(isAdLoading = true) }
        rewardedAdManager.showRewardedAdIfReady(
            activity = activity,
            onAdDismissed = { _uiState.update { it.copy(isAdLoading = false) } }
        ) {
            viewModelScope.launch {
                rewardManager.grantCommentPass()
                _uiState.update { it.copy(isQuotaGateVisible = false, isAdLoading = false) }
                pendingCommentParams?.let { pending ->
                    postCommentInternal(pending)
                }
            }
        }
    }

    fun openPaywall() {
        _uiState.update {
            it.copy(
                isPaywallVisible = true,
                isQuotaGateVisible = false,
                paywallErrorMessage = null
            )
        }
        viewModelScope.launch {
            _uiState.update { it.copy(availableProducts = billingRepository.getAvailableProducts()) }
        }
    }

    fun dismissPaywall() {
        _uiState.update {
            it.copy(
                isPaywallVisible = false,
                isPurchasingProduct = false,
                paywallErrorMessage = null
            )
        }
    }

    fun purchaseProduct(activity: Activity, product: BillingProduct) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasingProduct = true, paywallErrorMessage = null) }
            val launched = billingRepository.purchaseProduct(activity, product)
            if (!launched) {
                _uiState.update {
                    it.copy(
                        isPurchasingProduct = false,
                        paywallErrorMessage = UiText.StaticText(CommonR.string.purchase_failed)
                    )
                }
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoringPurchases = true) }
            val success = billingRepository.restorePurchases()
            _uiState.update {
                it.copy(
                    isRestoringPurchases = false,
                    isPaywallVisible = if (success) false else it.isPaywallVisible,
                    paywallErrorMessage = if (!success) UiText.StaticText(CommonR.string.restore_not_found) else null
                )
            }
        }
    }

    fun editComment(
        commentId: String,
        newContent: String,
        isSpoiler: Boolean
    ) {
        viewModelScope.launch {
            editCommentUseCase(
                EditCommentParams(
                    target = discussionTarget,
                    commentId = commentId,
                    newContent = newContent,
                    isSpoiler = isSpoiler
                )
            )
        }
    }

    fun editComment(args: EditCommentArgs) {
        editComment(
            commentId = args.commentId,
            newContent = args.newContent,
            isSpoiler = args.isSpoiler
        )
    }

    fun reportComment(
        commentId: String,
        reason: String
    ) {
        analytics.logEvent(
            DiscussionAnalyticsEvent.CommentReported(
                targetType = discussionTarget.mediaType.asAnalyticsValue(),
                targetId = discussionTarget.mediaId,
                commentId = commentId,
                reason = reason
            )
        )
        _locallyReportedCommentIds.update { it + commentId }
        viewModelScope.launch {
            reportCommentUseCase(
                ReportCommentParams(
                    target = discussionTarget,
                    commentId = commentId,
                    reason = reason
                )
            )
        }
    }

    fun reportComment(args: ReportCommentArgs) {
        reportComment(
            commentId = args.commentId,
            reason = args.reason
        )
    }

    fun toggleCommentUpvote(commentId: String) {
        analytics.logEvent(
            DiscussionAnalyticsEvent.CommentUpvoted(
                targetType = discussionTarget.mediaType.asAnalyticsValue(),
                targetId = discussionTarget.mediaId,
                commentId = commentId
            )
        )
        viewModelScope.launch {
            toggleCommentUpvoteUseCase(
                ToggleCommentUpvoteParams(
                    target = discussionTarget,
                    commentId = commentId
                )
            )
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            deleteCommentUseCase(
                DeleteCommentParams(
                    target = discussionTarget,
                    commentId = commentId
                )
            )
        }
    }
}
