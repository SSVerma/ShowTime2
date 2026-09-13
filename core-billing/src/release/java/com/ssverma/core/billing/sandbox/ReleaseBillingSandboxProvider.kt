package com.ssverma.core.billing.sandbox

import com.ssverma.core.billing.model.BillingProduct
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReleaseBillingSandboxProvider @Inject constructor() : BillingSandboxProvider {

    override fun isSandboxEnabled(): Boolean = false

    override fun getSandboxProducts(): List<BillingProduct> = emptyList()
}
