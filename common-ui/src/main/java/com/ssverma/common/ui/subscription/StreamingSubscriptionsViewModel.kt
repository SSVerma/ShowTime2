package com.ssverma.common.ui.subscription

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.asSuccessOrErrorUiState
import com.ssverma.shared.ads.quota.PassKey
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.WatchProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StreamingSubscriptionsUiState(
    val providersState: UiState<List<ProviderInfo>, Failure.CoreFailure> = UiState.Loading,
    val selectedProviderIds: Set<Int> = emptySet(),
    val isProActive: Boolean = false,
    val isPassActive: Boolean = false,
    val searchQuery: String = "",
    val showMultiServiceGate: Boolean = false,
    val isProPaymentEnabled: Boolean = true
)

sealed interface StreamingSubscriptionsUiEffect {
    data object SubscriptionsSaved : StreamingSubscriptionsUiEffect
    data object ShowMultiServiceGate : StreamingSubscriptionsUiEffect
}

@HiltViewModel
class StreamingSubscriptionsViewModel @Inject constructor(
    private val watchProviderRepository: WatchProviderRepository,
    private val appConfigRepository: AppConfigRepository,
    private val billingRepository: BillingRepository,
    private val rewardManager: RewardManager,
    private val rewardedAdManager: RewardedAdManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreamingSubscriptionsUiState())
    val uiState: StateFlow<StreamingSubscriptionsUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<StreamingSubscriptionsUiEffect>()
    val uiEffect: SharedFlow<StreamingSubscriptionsUiEffect> = _uiEffect.asSharedFlow()

    private var pendingProviderToToggle: Int? = null

    init {
        viewModelScope.launch {
            combine(
                billingRepository.isProActive,
                rewardManager.isPassActive(MultiServiceFilterPassKey)
            ) { isPro, isPass ->
                isPro to isPass
            }.collectLatest { (isPro, isPass) ->
                _uiState.update { it.copy(isProActive = isPro, isPassActive = isPass) }
            }
        }

        viewModelScope.launch {
            billingRepository.isBillingEnabled.collectLatest { enabled ->
                _uiState.update { it.copy(isProPaymentEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            appConfigRepository.userStreamingSubscriptions.collectLatest { savedIds ->
                _uiState.update { current ->
                    if (current.selectedProviderIds.isEmpty()) {
                        current.copy(selectedProviderIds = savedIds)
                    } else {
                        current
                    }
                }
            }
        }

        loadProviders()
        rewardedAdManager.loadAd()
    }

    fun loadProviders() {
        viewModelScope.launch {
            _uiState.update { it.copy(providersState = UiState.Loading) }
            val result = watchProviderRepository.fetchAllMovieWatchProviders()
            _uiState.update { it.copy(providersState = result.asSuccessOrErrorUiState()) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleProvider(providerId: Int) {
        val current = _uiState.value
        val isSelected = current.selectedProviderIds.contains(providerId)

        if (isSelected) {
            _uiState.update { it.copy(selectedProviderIds = it.selectedProviderIds - providerId) }
        } else {
            val currentCount = current.selectedProviderIds.size
            val isMultiAllowed = current.isProActive || current.isPassActive

            if (currentCount == 0 || isMultiAllowed) {
                _uiState.update { it.copy(selectedProviderIds = it.selectedProviderIds + providerId) }
            } else {
                pendingProviderToToggle = providerId
                _uiState.update { it.copy(showMultiServiceGate = true) }
                rewardedAdManager.loadAd()
                viewModelScope.launch {
                    _uiEffect.emit(StreamingSubscriptionsUiEffect.ShowMultiServiceGate)
                }
            }
        }
    }

    fun dismissMultiServiceGate() {
        pendingProviderToToggle = null
        _uiState.update { it.copy(showMultiServiceGate = false) }
    }

    fun watchAdForMultiServicePass(activity: Activity) {
        rewardedAdManager.showRewardedAdIfReady(activity) {
            viewModelScope.launch {
                rewardManager.grantTimedPass(MultiServiceFilterPassKey)
                val pending = pendingProviderToToggle
                if (pending != null) {
                    _uiState.update {
                        it.copy(
                            selectedProviderIds = it.selectedProviderIds + pending,
                            showMultiServiceGate = false
                        )
                    }
                    pendingProviderToToggle = null
                } else {
                    _uiState.update { it.copy(showMultiServiceGate = false) }
                }
            }
        }
    }

    fun clearAll() {
        _uiState.update { it.copy(selectedProviderIds = emptySet()) }
    }

    fun saveSubscriptions() {
        viewModelScope.launch {
            appConfigRepository.updateStreamingSubscriptions(_uiState.value.selectedProviderIds)
            _uiEffect.emit(StreamingSubscriptionsUiEffect.SubscriptionsSaved)
        }
    }
}

private val MultiServiceFilterPassKey = PassKey("multi_service_filter")

