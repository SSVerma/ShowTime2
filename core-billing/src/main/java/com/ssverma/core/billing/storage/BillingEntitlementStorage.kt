package com.ssverma.core.billing.storage

import android.content.Context
import com.ssverma.core.billing.model.ProStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingEntitlementStorage @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _cachedProStatus = MutableStateFlow(readCachedStatus())
    val cachedProStatus: StateFlow<ProStatus> = _cachedProStatus.asStateFlow()

    private fun readCachedStatus(): ProStatus {
        val isPro = prefs.getBoolean(KEY_IS_PRO_ACTIVE, false)
        if (!isPro) return ProStatus.Inactive

        val productId = prefs.getString(KEY_PRODUCT_ID, null) ?: return ProStatus.Inactive
        val purchaseToken = prefs.getString(KEY_PURCHASE_TOKEN, "") ?: ""

        return ProStatus.Active(
            productId = productId,
            purchaseToken = purchaseToken
        )
    }

    fun saveEntitlement(
        productId: String,
        purchaseToken: String
    ) {
        prefs.edit()
            .putBoolean(KEY_IS_PRO_ACTIVE, true)
            .putString(KEY_PRODUCT_ID, productId)
            .putString(KEY_PURCHASE_TOKEN, purchaseToken)
            .putLong(KEY_LAST_VERIFIED, System.currentTimeMillis())
            .apply()

        _cachedProStatus.value = ProStatus.Active(
            productId = productId,
            purchaseToken = purchaseToken
        )
    }

    fun clearEntitlement() {
        prefs.edit()
            .clear()
            .apply()

        _cachedProStatus.value = ProStatus.Inactive
    }

    fun getInitialProStatus(): ProStatus = _cachedProStatus.value

    companion object {
        private const val PREFS_NAME = "showtime_billing_entitlements"
        private const val KEY_IS_PRO_ACTIVE = "key_is_pro_active"
        private const val KEY_PRODUCT_ID = "key_product_id"
        private const val KEY_PURCHASE_TOKEN = "key_purchase_token"
        private const val KEY_LAST_VERIFIED = "key_last_verified"
    }
}
