package com.ssverma.feature.payment.ui

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.analytics.Analytics
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.billing.model.PurchaseResult
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.feature.payment.analytics.PaymentAnalyticsEvent
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

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val appConfigProvider: AppConfigProvider,
    private val analytics: Analytics
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _restoreEvents = MutableSharedFlow<RestoreEvent>()
    val restoreEvents: SharedFlow<RestoreEvent> = _restoreEvents.asSharedFlow()

    private val _purchaseUiEvents = MutableSharedFlow<PurchaseUiEvent>()
    val purchaseUiEvents: SharedFlow<PurchaseUiEvent> = _purchaseUiEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                appConfigProvider.observeBoolean(KEY_CONFIG_SHOW_PRO_PAYWALL, true),
                billingRepository.isBillingEnabled
            ) { isPaywallEnabled, isBillingEnabled ->
                isPaywallEnabled && isBillingEnabled
            }.collectLatest { isEnabled ->
                _uiState.update { it.copy(isPaywallRemoteEnabled = isEnabled) }
            }
        }

        viewModelScope.launch {
            billingRepository.isBillingEnabled.collectLatest { isEnabled ->
                if (isEnabled) {
                    val products = billingRepository.getAvailableProducts()
                    _uiState.update { it.copy(products = products) }
                } else {
                    _uiState.update { it.copy(products = emptyList()) }
                }
            }
        }

        viewModelScope.launch {
            billingRepository.isProActive.collectLatest { isPro ->
                _uiState.update { it.copy(isProActive = isPro) }
            }
        }

        viewModelScope.launch {
            billingRepository.purchaseEvents.collect { event ->
                when (event) {
                    is PurchaseResult.Success -> {
                        analytics.logEvent(PaymentAnalyticsEvent.PurchaseCompleted())
                        _uiState.update { it.copy(isPurchasing = false, errorMessage = null) }
                        _purchaseUiEvents.emit(PurchaseUiEvent.Success)
                    }

                    is PurchaseResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isPurchasing = false,
                                errorMessage = event.message
                            )
                        }
                        _purchaseUiEvents.emit(PurchaseUiEvent.Error(event.message))
                    }

                    is PurchaseResult.UserCancelled -> {
                        _uiState.update { it.copy(isPurchasing = false, errorMessage = null) }
                    }
                }
            }
        }
    }

    fun purchaseProduct(activity: Activity, product: BillingProduct) {
        analytics.logEvent(
            PaymentAnalyticsEvent.PurchaseInitiated(
                sku = product.id,
                price = product.formattedPrice
            )
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, errorMessage = null) }
            val launched =
                billingRepository.purchaseProduct(activity = activity, product = product)
            if (!launched) {
                _uiState.update {
                    it.copy(
                        isPurchasing = false,
                        errorMessage = "Purchase could not be completed. Please try again."
                    )
                }
                _purchaseUiEvents.emit(PurchaseUiEvent.Error())
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true) }
            val hasActivePro = billingRepository.restorePurchases()
            analytics.logEvent(PaymentAnalyticsEvent.RestorePurchases(success = hasActivePro))
            _uiState.update { it.copy(isRestoring = false) }
            _restoreEvents.emit(
                if (hasActivePro) RestoreEvent.Success else RestoreEvent.NotFound
            )
        }
    }

    fun getManageSubscriptionsIntent(sku: String? = null): Intent {
        return billingRepository.createManageSubscriptionIntent(sku)
    }

    companion object {
        private const val KEY_CONFIG_SHOW_PRO_PAYWALL = "show_pro_paywall_enabled"
    }
}
