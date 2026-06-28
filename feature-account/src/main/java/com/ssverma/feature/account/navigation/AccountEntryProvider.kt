package com.ssverma.feature.account.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.account.ui.profile.ProfileScreen
import com.ssverma.feature.auth.ui.auth.AuthScreenContainer

fun EntryProviderScope<NavKey>.accountEntries(
    navigator: Navigator
) {
    showTimeEntry<ProfileNavKey> {
        AuthScreenContainer(
            onBackPressed = {
                navigator.goBack()
            }
        ) {
            ProfileScreen(
                onBackPressed = {
                    navigator.goBack()
                }
            )
        }
    }
}
