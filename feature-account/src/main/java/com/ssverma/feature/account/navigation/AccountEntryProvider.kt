package com.ssverma.feature.account.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.account.ui.backup.BackupSyncScreen
import com.ssverma.feature.account.ui.profile.ProfileScreen
import com.ssverma.feature.account.ui.trakt.TraktSyncScreen
import com.ssverma.feature.auth.navigation.AuthNavKey
import com.ssverma.shared.ui.LocalAppInfoTrigger

fun EntryProviderScope<NavKey>.accountEntries(
    navigator: Navigator
) {
    showTimeEntry<ProfileNavKey> {
        val appInfoTrigger = LocalAppInfoTrigger.current
        ProfileScreen(
            onBackPressed = {
                navigator.goBack()
            },
            onLoginClick = {
                navigator.navigate(AuthNavKey)
            },
            onOpenBackup = {
                navigator.navigate(BackupSyncNavKey)
            },
            onOpenTrakt = {
                navigator.navigate(TraktSyncNavKey)
            },
            onOpenAbout = {
                appInfoTrigger.invoke()
            }
        )
    }

    showTimeEntry<BackupSyncNavKey> {
        BackupSyncScreen(
            onBackPressed = {
                navigator.goBack()
            }
        )
    }

    showTimeEntry<TraktSyncNavKey> {
        TraktSyncScreen(
            onBackPressed = {
                navigator.goBack()
            }
        )
    }
}
