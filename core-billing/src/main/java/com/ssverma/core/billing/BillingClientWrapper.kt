package com.ssverma.core.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.billing.model.BillingState
import com.ssverma.core.billing.model.ProStatus
import com.ssverma.core.billing.model.ProductType
import com.ssverma.core.billing.model.PurchaseResult
import com.ssverma.core.billing.sandbox.BillingSandboxProvider
import com.ssverma.core.billing.storage.BillingEntitlementStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingClientWrapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val entitlementStorage: BillingEntitlementStorage,
    private val sandboxProvider: BillingSandboxProvider
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionMutex = Mutex()
    private var reconnectAttempts = 0

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Disconnected)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val _proStatus = MutableStateFlow<ProStatus>(
        entitlementStorage.getInitialProStatus().takeIf { it is ProStatus.Active }
            ?: ProStatus.Loading
    )
    val proStatus: StateFlow<ProStatus> = _proStatus.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>()
    val purchaseEvents: SharedFlow<PurchaseResult> = _purchaseEvents.asSharedFlow()

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(this)
            .enableAutoServiceReconnection()
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans()
                    .build()
            )
            .build()
    }

    init {
        startBillingConnection()
    }

    fun startBillingConnection(onConnected: (() -> Unit)? = null) {
        if (billingClient.isReady) {
            _billingState.value = BillingState.Connected
            onConnected?.invoke()
            return
        }

        _billingState.value = BillingState.Connecting
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _billingState.value = BillingState.Connected
                    reconnectAttempts = 0
                    scope.launch {
                        refreshPurchases()
                        onConnected?.invoke()
                    }
                } else {
                    _billingState.value = BillingState.Error(
                        responseCode = billingResult.responseCode,
                        message = billingResult.debugMessage
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingState.value = BillingState.Disconnected
                retryBillingServiceConnection()
            }
        })
    }

    private fun retryBillingServiceConnection() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            val delayMs = INITIAL_RECONNECT_DELAY_MS * (1L shl reconnectAttempts)
            reconnectAttempts++
            scope.launch {
                delay(delayMs)
                startBillingConnection()
            }
        }
    }

    private suspend fun ensureConnected(): Boolean = connectionMutex.withLock {
        if (billingClient.isReady) return@withLock true

        suspendCancellableCoroutine { continuation ->
            startBillingConnection {
                if (continuation.isActive) {
                    continuation.resume(billingClient.isReady)
                }
            }
        }
    }

    suspend fun queryAvailableProducts(): List<BillingProduct> {
        val allProducts = mutableListOf<BillingProduct>()

        try {
            if (ensureConnected()) {
                val subsProductList = BillingConstants.SUBS_SKUS.map { sku ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(sku)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
                if (subsProductList.isNotEmpty()) {
                    val subsParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(subsProductList)
                        .build()
                    val subsResult = billingClient.queryProductDetails(subsParams)
                    if (subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        subsResult.productDetailsList?.mapNotNull { details ->
                            mapProductDetailsToBillingProduct(details)
                        }?.let { allProducts.addAll(it) }
                    }
                }
            }
        } catch (e: Exception) {
            // Graceful fallback to default catalog
        }

        return if (allProducts.isNotEmpty()) {
            allProducts
        } else {
            sandboxProvider.getSandboxProducts()
        }
    }

    private fun mapProductDetailsToBillingProduct(details: ProductDetails): BillingProduct? {
        val offer = details.subscriptionOfferDetails?.firstOrNull() ?: return null
        val phases = offer.pricingPhases.pricingPhaseList
        val freeTrialPhase = phases.firstOrNull { it.priceAmountMicros == 0L }
        val recurringPhase = phases.firstOrNull {
            it.recurrenceMode == ProductDetails.RecurrenceMode.INFINITE_RECURRING
        } ?: phases.lastOrNull() ?: return null

        return BillingProduct(
            id = details.productId,
            name = details.name,
            description = details.description,
            formattedPrice = recurringPhase.formattedPrice,
            priceAmountMicros = recurringPhase.priceAmountMicros,
            priceCurrencyCode = recurringPhase.priceCurrencyCode,
            productType = ProductType.SUBS,
            billingPeriod = recurringPhase.billingPeriod,
            freeTrialPeriod = freeTrialPhase?.billingPeriod,
            rawProductDetails = details
        )
    }

    suspend fun launchBillingFlow(
        activity: Activity,
        product: BillingProduct,
        obfuscatedAccountId: String? = null
    ): BillingResult {
        if (!ensureConnected()) {
            val disconnected = BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
                .setDebugMessage("Google Play Billing service disconnected")
                .build()
            _purchaseEvents.emit(
                PurchaseResult.Error(
                    responseCode = disconnected.responseCode,
                    message = disconnected.debugMessage
                )
            )
            return disconnected
        }

        val rawDetails = product.rawProductDetails
        if (rawDetails == null) {
            val unavailable = BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE)
                .setDebugMessage("Product is currently unavailable on Google Play")
                .build()
            _purchaseEvents.emit(
                PurchaseResult.Error(
                    responseCode = unavailable.responseCode,
                    message = unavailable.debugMessage
                )
            )
            return unavailable
        }

        val selectedOfferToken = rawDetails.subscriptionOfferDetails
            ?.firstOrNull()?.offerToken.orEmpty()
        val currentPro = _proStatus.value
        val productDetailsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(rawDetails)
            .setOfferToken(selectedOfferToken)

        if (currentPro is ProStatus.Active && currentPro.productId != product.id) {
            val replacementParams =
                BillingFlowParams.ProductDetailsParams.SubscriptionProductReplacementParams.newBuilder()
                    .setOldProductId(currentPro.productId)
                    .setReplacementMode(
                        BillingFlowParams.ProductDetailsParams.SubscriptionProductReplacementParams.ReplacementMode.WITH_TIME_PRORATION
                    )
                    .build()
            productDetailsBuilder.setSubscriptionProductReplacementParams(replacementParams)
        }

        val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsBuilder.build()))

        // Support seamless upgrade/downgrade when user already holds an active subscription
        if (currentPro is ProStatus.Active) {
            val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(currentPro.purchaseToken)
                .build()
            billingFlowParamsBuilder.setSubscriptionUpdateParams(updateParams)
        }

        obfuscatedAccountId?.let {
            billingFlowParamsBuilder.setObfuscatedAccountId(it)
        }

        return try {
            val billingResult =
                billingClient.launchBillingFlow(activity, billingFlowParamsBuilder.build())
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                _purchaseEvents.emit(
                    PurchaseResult.Error(
                        responseCode = billingResult.responseCode,
                        message = billingResult.debugMessage
                    )
                )
            }
            billingResult
        } catch (e: Exception) {
            val errorResult = BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                .setDebugMessage(e.message ?: "Failed to launch billing flow")
                .build()
            _purchaseEvents.emit(
                PurchaseResult.Error(
                    responseCode = errorResult.responseCode,
                    message = errorResult.debugMessage
                )
            )
            errorResult
        }
    }

    /**
     * Builds an Intent to deep link directly to Google Play's subscription management page.
     * As recommended by Google: https://developer.android.com/google/play/billing/subscriptions#deep-link
     */
    fun createManageSubscriptionIntent(sku: String? = null): Intent {
        val uriString = if (sku != null) {
            "https://play.google.com/store/account/subscriptions?sku=$sku&package=${context.packageName}"
        } else {
            "https://play.google.com/store/account/subscriptions"
        }
        return Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    scope.launch {
                        processPurchases(purchases)
                    }
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                scope.launch {
                    _purchaseEvents.emit(PurchaseResult.UserCancelled)
                }
            }

            else -> {
                scope.launch {
                    _purchaseEvents.emit(
                        PurchaseResult.Error(
                            responseCode = billingResult.responseCode,
                            message = billingResult.debugMessage
                        )
                    )
                }
            }
        }
    }

    suspend fun refreshPurchases(): Boolean {
        if (!ensureConnected()) return false

        val subsPurchasesResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        return processPurchases(subsPurchasesResult.purchasesList)
    }

    private suspend fun processPurchases(purchases: List<Purchase>): Boolean {
        var hasActivePro = false

        for (purchase in purchases) {
            val isSuspended = purchase.isSuspended
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !isSuspended) {
                if (!purchase.isAcknowledged) {
                    val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(acknowledgeParams)
                }

                val productId = purchase.products.firstOrNull()
                if (productId in BillingConstants.SUBS_SKUS) {
                    hasActivePro = true
                    val activeStatus = ProStatus.Active(
                        productId = productId ?: BillingConstants.SKU_PRO_YEARLY,
                        purchaseToken = purchase.purchaseToken
                    )
                    _proStatus.value = activeStatus
                    entitlementStorage.saveEntitlement(
                        productId = activeStatus.productId,
                        purchaseToken = activeStatus.purchaseToken
                    )
                    _purchaseEvents.emit(
                        PurchaseResult.Success(
                            purchaseToken = purchase.purchaseToken,
                            productId = productId ?: ""
                        )
                    )
                    break
                }
            }
        }

        if (!hasActivePro) {
            entitlementStorage.clearEntitlement()
            _proStatus.value = ProStatus.Inactive
        }

        return hasActivePro
    }

    companion object {
        private const val MAX_RECONNECT_ATTEMPTS = 3
        private const val INITIAL_RECONNECT_DELAY_MS = 1000L
    }
}
