package com.ssverma.feature.payment.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.payment.ui.ProPaywallScreen

fun EntryProviderScope<NavKey>.paymentEntries(
    navigator: Navigator
) {
    showTimeEntry<ProPaywallNavKey> {
        ProPaywallScreen(
            onBackPressed = {
                navigator.goBack()
            }
        )
    }
}
