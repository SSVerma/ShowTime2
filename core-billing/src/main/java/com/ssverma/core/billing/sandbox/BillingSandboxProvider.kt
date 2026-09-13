package com.ssverma.core.billing.sandbox

import com.ssverma.core.billing.model.BillingProduct

interface BillingSandboxProvider {
    fun getSandboxProducts(): List<BillingProduct>
    fun isSandboxEnabled(): Boolean
}
