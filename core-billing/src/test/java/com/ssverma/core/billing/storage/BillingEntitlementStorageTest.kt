package com.ssverma.core.billing.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.billing.BillingConstants
import com.ssverma.core.billing.model.ProStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class BillingEntitlementStorageTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val mockPrefs: SharedPreferences = mockk(relaxed = true)
    private val mockEditor: SharedPreferences.Editor = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { mockContext.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putBoolean(any(), any()) } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.putLong(any(), any()) } returns mockEditor
        every { mockEditor.clear() } returns mockEditor
    }

    @Test
    fun `initial state reflects cached active Pro when preferences contain valid entitlement`() {
        every { mockPrefs.getBoolean("key_is_pro_active", false) } returns true
        every {
            mockPrefs.getString(
                "key_product_id",
                null
            )
        } returns BillingConstants.SKU_PRO_YEARLY
        every { mockPrefs.getString("key_purchase_token", "") } returns "cached_token_123"

        val storage = BillingEntitlementStorage(context = mockContext)

        val initialStatus = storage.getInitialProStatus()
        assertThat(initialStatus).isInstanceOf(ProStatus.Active::class.java)
        val active = initialStatus as ProStatus.Active
        assertThat(active.productId).isEqualTo(BillingConstants.SKU_PRO_YEARLY)
        assertThat(active.purchaseToken).isEqualTo("cached_token_123")
    }

    @Test
    fun `initial state is Inactive when preferences are empty`() {
        every { mockPrefs.getBoolean("key_is_pro_active", false) } returns false

        val storage = BillingEntitlementStorage(context = mockContext)

        val initialStatus = storage.getInitialProStatus()
        assertThat(initialStatus).isEqualTo(ProStatus.Inactive)
    }

    @Test
    fun `saveEntitlement updates preferences and updates cachedProStatus`() {
        every { mockPrefs.getBoolean("key_is_pro_active", false) } returns false

        val storage = BillingEntitlementStorage(context = mockContext)
        assertThat(storage.cachedProStatus.value).isEqualTo(ProStatus.Inactive)

        storage.saveEntitlement(
            productId = BillingConstants.SKU_PRO_YEARLY,
            purchaseToken = "new_sub_token"
        )

        verify { mockEditor.putBoolean("key_is_pro_active", true) }
        verify { mockEditor.putString("key_product_id", BillingConstants.SKU_PRO_YEARLY) }
        verify { mockEditor.putString("key_purchase_token", "new_sub_token") }
        verify { mockEditor.apply() }

        val status = storage.cachedProStatus.value
        assertThat(status).isInstanceOf(ProStatus.Active::class.java)
        val active = status as ProStatus.Active
        assertThat(active.productId).isEqualTo(BillingConstants.SKU_PRO_YEARLY)
        assertThat(active.purchaseToken).isEqualTo("new_sub_token")
    }

    @Test
    fun `clearEntitlement clears preferences and updates cachedProStatus to Inactive`() {
        every { mockPrefs.getBoolean("key_is_pro_active", false) } returns true
        every {
            mockPrefs.getString(
                "key_product_id",
                null
            )
        } returns BillingConstants.SKU_PRO_YEARLY
        every { mockPrefs.getString("key_purchase_token", "") } returns "cached_token"

        val storage = BillingEntitlementStorage(context = mockContext)
        assertThat(storage.cachedProStatus.value).isInstanceOf(ProStatus.Active::class.java)

        storage.clearEntitlement()

        verify { mockEditor.clear() }
        verify { mockEditor.apply() }
        assertThat(storage.cachedProStatus.value).isEqualTo(ProStatus.Inactive)
    }
}
