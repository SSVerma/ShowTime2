package com.ssverma.core.billing

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.billing.model.BillingState
import com.ssverma.core.billing.model.ProStatus
import com.ssverma.core.billing.model.PurchaseResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

interface BillingRepository {
    val proStatus: StateFlow<ProStatus>
    val isProActive: StateFlow<Boolean>
    val billingState: StateFlow<BillingState>
    val purchaseEvents: Flow<PurchaseResult>

    suspend fun getAvailableProducts(): List<BillingProduct>
    suspend fun purchaseProduct(activity: Activity, product: BillingProduct): Boolean
    suspend fun restorePurchases(): Boolean
}

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val billingClientWrapper: BillingClientWrapper
) : BillingRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val proStatus: StateFlow<ProStatus> = billingClientWrapper.proStatus

    override val isProActive: StateFlow<Boolean> = proStatus
        .map { it is ProStatus.Active }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    override val billingState: StateFlow<BillingState> = billingClientWrapper.billingState

    override val purchaseEvents: Flow<PurchaseResult> = billingClientWrapper.purchaseEvents

    override suspend fun getAvailableProducts(): List<BillingProduct> {
        return billingClientWrapper.queryAvailableProducts()
    }

    override suspend fun purchaseProduct(activity: Activity, product: BillingProduct): Boolean {
        val result = billingClientWrapper.launchBillingFlow(activity, product)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    override suspend fun restorePurchases(): Boolean {
        return billingClientWrapper.refreshPurchases()
    }
}
