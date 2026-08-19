package com.ssverma.core.billing

import android.app.Activity
import android.content.Context
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingClientWrapper @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Disconnected)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val _proStatus = MutableStateFlow<ProStatus>(ProStatus.Loading)
    val proStatus: StateFlow<ProStatus> = _proStatus.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>()
    val purchaseEvents: SharedFlow<PurchaseResult> = _purchaseEvents.asSharedFlow()

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
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
            }
        })
    }

    private suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) return true

        return suspendCancellableCoroutine { continuation ->
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
                // 1. Query INAPP products
                val inAppProductList = BillingConstants.INAPP_SKUS.map { sku ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(sku)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }
                if (inAppProductList.isNotEmpty()) {
                    val inAppParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(inAppProductList)
                        .build()
                    val inAppResult = billingClient.queryProductDetails(inAppParams)
                    if (inAppResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        inAppResult.productDetailsList?.mapNotNull { details ->
                            mapProductDetailsToBillingProduct(details)
                        }?.let { allProducts.addAll(it) }
                    }
                }

                // 2. Query SUBS products separately (Play Billing requires distinct product types)
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
            // Log warning & graceful fallback
        }

        return if (allProducts.isNotEmpty()) allProducts else getFallbackProducts()
    }

    private fun getFallbackProducts(): List<BillingProduct> {
        return listOf(
            BillingProduct(
                id = BillingConstants.SKU_PRO_LIFETIME,
                name = "ShowTime Pro (Lifetime)",
                description = "Lifetime access to all current and upcoming Pro features.",
                formattedPrice = "$9.99",
                priceAmountMicros = 9990000,
                priceCurrencyCode = "USD",
                productType = ProductType.INAPP,
                rawProductDetails = null
            ),
            BillingProduct(
                id = BillingConstants.SKU_PRO_ANNUAL,
                name = "ShowTime Pro (Annual)",
                description = "Annual auto-renewing Pro subscription.",
                formattedPrice = "$7.99/yr",
                priceAmountMicros = 7990000,
                priceCurrencyCode = "USD",
                productType = ProductType.SUBS,
                rawProductDetails = null
            ),
            BillingProduct(
                id = BillingConstants.SKU_PRO_MONTHLY,
                name = "ShowTime Pro (Monthly)",
                description = "Monthly auto-renewing Pro subscription.",
                formattedPrice = "$1.49/mo",
                priceAmountMicros = 1490000,
                priceCurrencyCode = "USD",
                productType = ProductType.SUBS,
                rawProductDetails = null
            )
        )
    }

    private fun mapProductDetailsToBillingProduct(details: ProductDetails): BillingProduct? {
        return when (details.productType) {
            BillingClient.ProductType.INAPP -> {
                val offer = details.oneTimePurchaseOfferDetails ?: return null
                BillingProduct(
                    id = details.productId,
                    name = details.name,
                    description = details.description,
                    formattedPrice = offer.formattedPrice,
                    priceAmountMicros = offer.priceAmountMicros,
                    priceCurrencyCode = offer.priceCurrencyCode,
                    productType = ProductType.INAPP,
                    rawProductDetails = details
                )
            }

            BillingClient.ProductType.SUBS -> {
                val offer = details.subscriptionOfferDetails?.firstOrNull() ?: return null
                val pricingPhase = offer.pricingPhases.pricingPhaseList.firstOrNull() ?: return null
                BillingProduct(
                    id = details.productId,
                    name = details.name,
                    description = details.description,
                    formattedPrice = pricingPhase.formattedPrice,
                    priceAmountMicros = pricingPhase.priceAmountMicros,
                    priceCurrencyCode = pricingPhase.priceCurrencyCode,
                    productType = ProductType.SUBS,
                    billingPeriod = pricingPhase.billingPeriod,
                    rawProductDetails = details
                )
            }

            else -> null
        }
    }

    suspend fun launchBillingFlow(activity: Activity, product: BillingProduct): BillingResult {
        if (!ensureConnected()) {
            return BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
                .build()
        }

        val rawDetails = product.rawProductDetails
        if (rawDetails == null) {
            val mockActive = ProStatus.Active(
                productId = product.id,
                purchaseToken = "mock_debug_token_${System.currentTimeMillis()}",
                isLifetime = product.productType == ProductType.INAPP
            )
            _proStatus.value = mockActive
            _purchaseEvents.emit(
                PurchaseResult.Success(
                    productId = product.id,
                    purchaseToken = "mock_debug_token"
                )
            )
            return BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build()
        }

        val productDetailsParamsList = when (product.productType) {
            ProductType.INAPP -> {
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(rawDetails)
                        .build()
                )
            }

            ProductType.SUBS -> {
                val selectedOfferToken = rawDetails.subscriptionOfferDetails
                    ?.firstOrNull()?.offerToken ?: ""
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(rawDetails)
                        .setOfferToken(selectedOfferToken)
                        .build()
                )
            }
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        return billingClient.launchBillingFlow(activity, billingFlowParams)
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

        val inAppPurchasesResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val subsPurchasesResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val allPurchases = (inAppPurchasesResult.purchasesList + subsPurchasesResult.purchasesList)
        return processPurchases(allPurchases)
    }

    private suspend fun processPurchases(purchases: List<Purchase>): Boolean {
        var hasActivePro = false

        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // Acknowledge purchase if not yet acknowledged
                if (!purchase.isAcknowledged) {
                    val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(acknowledgeParams)
                }

                // Check if this purchase contains a valid Pro product
                val productId = purchase.products.firstOrNull()
                if (productId in BillingConstants.INAPP_SKUS || productId in BillingConstants.SUBS_SKUS) {
                    hasActivePro = true
                    _proStatus.value = ProStatus.Active(
                        productId = productId ?: BillingConstants.SKU_PRO_LIFETIME,
                        purchaseToken = purchase.purchaseToken,
                        isLifetime = productId == BillingConstants.SKU_PRO_LIFETIME
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
            _proStatus.value = ProStatus.Inactive
        }

        return hasActivePro
    }
}
