package com.ssverma.feature.auth.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.auth.ui.auth.AuthScreen

fun EntryProviderScope<NavKey>.authEntries(
    navigator: Navigator
) {
    showTimeEntry<AuthNavKey> {
        AuthScreen(
            onAuthSessionEstablished = {
                navigator.goBack(result = true)
            },
            onBackPressed = {
                navigator.goBack(result = false)
            }
        )
    }
}
