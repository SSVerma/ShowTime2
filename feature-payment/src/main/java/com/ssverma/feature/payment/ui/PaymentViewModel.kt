package com.ssverma.feature.payment.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.ccm.AppConfigProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentUiState(
    val products: List<BillingProduct> = emptyList(),
    val isProActive: Boolean = false,
    val isRestoring: Boolean = false,
    val isPaywallRemoteEnabled: Boolean = true
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val appConfigProvider: AppConfigProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

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
    }

    fun purchaseProduct(activity: Activity, product: BillingProduct) {
        viewModelScope.launch {
            billingRepository.purchaseProduct(activity, product)
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true) }
            billingRepository.restorePurchases()
            _uiState.update { it.copy(isRestoring = false) }
        }
    }

    companion object {
        private const val KEY_CONFIG_SHOW_PRO_PAYWALL = "show_pro_paywall_enabled"
    }
}
