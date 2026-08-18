package com.ssverma.core.testing.fakes

import android.app.Activity
import com.ssverma.core.billing.BillingConstants
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.billing.model.BillingState
import com.ssverma.core.billing.model.ProStatus
import com.ssverma.core.billing.model.ProductType
import com.ssverma.core.billing.model.PurchaseResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeBillingRepository(
    initialProActive: Boolean = false
) : BillingRepository {

    private val _proStatus = MutableStateFlow<ProStatus>(
        if (initialProActive) {
            ProStatus.Active(
                productId = BillingConstants.SKU_PRO_LIFETIME,
                purchaseToken = "fake_purchase_token",
                isLifetime = true
            )
        } else {
            ProStatus.Inactive
        }
    )
    override val proStatus: StateFlow<ProStatus> = _proStatus.asStateFlow()

    private val _isProActive = MutableStateFlow(initialProActive)
    override val isProActive: StateFlow<Boolean> = _isProActive.asStateFlow()

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Connected)
    override val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>(extraBufferCapacity = 64)
    override val purchaseEvents: Flow<PurchaseResult> = _purchaseEvents.asSharedFlow()

    private var availableProducts: List<BillingProduct> = listOf(
        BillingProduct(
            id = BillingConstants.SKU_PRO_LIFETIME,
            name = "ShowTime Pro (Lifetime)",
            description = "One-time purchase for permanent ad-free access.",
            formattedPrice = "$4.99",
            priceAmountMicros = 4990000,
            priceCurrencyCode = "USD",
            productType = ProductType.INAPP
        ),
        BillingProduct(
            id = BillingConstants.SKU_PRO_ANNUAL,
            name = "ShowTime Pro (Annual)",
            description = "Annual auto-renewing Pro subscription.",
            formattedPrice = "$7.99/yr",
            priceAmountMicros = 7990000,
            priceCurrencyCode = "USD",
            productType = ProductType.SUBS
        ),
        BillingProduct(
            id = BillingConstants.SKU_PRO_MONTHLY,
            name = "ShowTime Pro (Monthly)",
            description = "Monthly auto-renewing Pro subscription.",
            formattedPrice = "$1.49/mo",
            priceAmountMicros = 1490000,
            priceCurrencyCode = "USD",
            productType = ProductType.SUBS
        )
    )

    var purchaseSuccessToReturn: Boolean = true
    var restoreSuccessToReturn: Boolean = true

    fun setProActive(active: Boolean, productId: String = BillingConstants.SKU_PRO_LIFETIME) {
        _isProActive.value = active
        _proStatus.value = if (active) {
            ProStatus.Active(
                productId = productId,
                purchaseToken = "test_token_${System.currentTimeMillis()}",
                isLifetime = productId == BillingConstants.SKU_PRO_LIFETIME
            )
        } else {
            ProStatus.Inactive
        }
    }

    fun setBillingState(state: BillingState) {
        _billingState.value = state
    }

    fun setAvailableProducts(products: List<BillingProduct>) {
        availableProducts = products
    }

    suspend fun emitPurchaseResult(result: PurchaseResult) {
        _purchaseEvents.emit(result)
    }

    override suspend fun getAvailableProducts(): List<BillingProduct> {
        return availableProducts
    }

    override suspend fun purchaseProduct(activity: Activity, product: BillingProduct): Boolean {
        if (purchaseSuccessToReturn) {
            setProActive(active = true, productId = product.id)
            _purchaseEvents.emit(
                PurchaseResult.Success(
                    purchaseToken = "fake_token",
                    productId = product.id
                )
            )
        }
        return purchaseSuccessToReturn
    }

    override suspend fun restorePurchases(): Boolean {
        if (restoreSuccessToReturn) {
            setProActive(active = true)
        }
        return restoreSuccessToReturn
    }
}
