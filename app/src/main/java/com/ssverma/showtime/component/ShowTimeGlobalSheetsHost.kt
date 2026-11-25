package com.ssverma.showtime.component

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.ssverma.common.ui.appinfo.AppInfoBottomSheet
import com.ssverma.common.ui.appinfo.OpenSourceLicensesBottomSheet
import com.ssverma.common.ui.paywall.ProPaywallBottomSheet
import com.ssverma.common.ui.state.AppStateHolder
import com.ssverma.common.ui.theme.ThemeSelectionBottomSheet
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.ui.util.openWebUrl
import com.ssverma.feature.account.navigation.BackupSyncNavKey
import com.ssverma.feature.library.navigation.BacklogChallengeNavKey
import com.ssverma.feature.library.navigation.CinemaDiaryNavKey
import com.ssverma.feature.library.navigation.CinemaReceiptNavKey
import com.ssverma.feature.library.navigation.CinephileWrappedNavKey
import com.ssverma.feature.library.navigation.TasteProfileNavKey
import com.ssverma.feature.match.navigation.MatchRoomNavKey
import com.ssverma.feature.movie.navigation.CinemaGameNavKey
import com.ssverma.feature.person.navigation.PersonHomeNavKey
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.utils.AppConfigConstants
import com.ssverma.shared.ui.component.LocalizationSettingsBottomSheet
import com.ssverma.showtime.feature.filter.navigation.UniversalDiscoveryNavKey
import com.ssverma.showtime.navigation.WhatsNewNavKey
import kotlinx.coroutines.launch

/**
 * Encapsulates navigation and preference actions exposed by both mobile Drawer and desktop Sidebar.
 */
@Immutable
data class ShowTimeMenuActions(
    val onOpenDiscovery: () -> Unit,
    val onOpenMovieMatch: () -> Unit,
    val onOpenPeople: () -> Unit,
    val onOpenCinemaDiary: () -> Unit,
    val onOpenTasteProfile: () -> Unit,
    val onOpenWrapped: () -> Unit,
    val onOpenBacklogChallenges: () -> Unit,
    val onOpenReceipt: () -> Unit,
    val onOpenCinemaGame: () -> Unit,
    val onOpenBackup: () -> Unit,
    val onOpenPro: () -> Unit,
    val onOpenTheme: () -> Unit,
    val onOpenLocalization: () -> Unit,
    val onOpenPrivacy: () -> Unit,
    val onOpenTerms: () -> Unit,
    val onOpenLicenses: () -> Unit,
    val onOpenAbout: () -> Unit,
    val onOpenWhatsNew: () -> Unit,
    val onReplaySplash: () -> Unit,
    val onOpenKeyboardShortcuts: () -> Unit
)

/**
 * Mutable state holder managing root-level modal overlays across phone and desktop shells.
 */
@Stable
class ShowTimeGlobalSheetState {
    var showManualAppInfoSheet by mutableStateOf(false)
    var showLicensesSheet by mutableStateOf(false)
    var showThemeSelectionSheet by mutableStateOf(false)
    var showLocalizationSettingsSheet by mutableStateOf(false)
    var showProPaywallSheet by mutableStateOf(false)
    var showKeyboardShortcutsDialog by mutableStateOf(false)

    val isAnyOverlayOpen: Boolean
        get() = showManualAppInfoSheet ||
                showLicensesSheet ||
                showThemeSelectionSheet ||
                showLocalizationSettingsSheet ||
                showProPaywallSheet ||
                showKeyboardShortcutsDialog

    fun dismissAll(): Boolean {
        if (!isAnyOverlayOpen) return false
        showManualAppInfoSheet = false
        showLicensesSheet = false
        showThemeSelectionSheet = false
        showLocalizationSettingsSheet = false
        showProPaywallSheet = false
        showKeyboardShortcutsDialog = false
        return true
    }
}

@Composable
fun rememberShowTimeGlobalSheetState(): ShowTimeGlobalSheetState {
    return remember { ShowTimeGlobalSheetState() }
}

@Composable
fun rememberShowTimeMenuActions(
    navigator: Navigator,
    context: Context,
    sheetState: ShowTimeGlobalSheetState,
    onReplaySplash: () -> Unit,
    onBeforeNavigate: () -> Unit = {}
): ShowTimeMenuActions {
    return remember(navigator, context, sheetState, onReplaySplash, onBeforeNavigate) {
        ShowTimeMenuActions(
            onOpenDiscovery = {
                onBeforeNavigate()
                navigator.navigate(UniversalDiscoveryNavKey())
            },
            onOpenMovieMatch = {
                onBeforeNavigate()
                navigator.navigate(MatchRoomNavKey())
            },
            onOpenPeople = {
                onBeforeNavigate()
                navigator.navigate(PersonHomeNavKey)
            },
            onOpenCinemaDiary = {
                onBeforeNavigate()
                navigator.navigate(CinemaDiaryNavKey)
            },
            onOpenTasteProfile = {
                onBeforeNavigate()
                navigator.navigate(TasteProfileNavKey)
            },
            onOpenWrapped = {
                onBeforeNavigate()
                navigator.navigate(CinephileWrappedNavKey)
            },
            onOpenBacklogChallenges = {
                onBeforeNavigate()
                navigator.navigate(BacklogChallengeNavKey)
            },
            onOpenReceipt = {
                onBeforeNavigate()
                navigator.navigate(CinemaReceiptNavKey)
            },
            onOpenCinemaGame = {
                onBeforeNavigate()
                navigator.navigate(CinemaGameNavKey)
            },
            onOpenBackup = {
                onBeforeNavigate()
                navigator.navigate(BackupSyncNavKey)
            },
            onOpenPro = {
                onBeforeNavigate()
                sheetState.showProPaywallSheet = true
            },
            onOpenTheme = {
                onBeforeNavigate()
                sheetState.showThemeSelectionSheet = true
            },
            onOpenLocalization = {
                onBeforeNavigate()
                sheetState.showLocalizationSettingsSheet = true
            },
            onOpenPrivacy = {
                onBeforeNavigate()
                context.openWebUrl(
                    url = AppConfigConstants.PRIVACY_POLICY_URL,
                    onError = { sheetState.showManualAppInfoSheet = true }
                )
            },
            onOpenTerms = {
                onBeforeNavigate()
                context.openWebUrl(
                    url = AppConfigConstants.TERMS_OF_SERVICE_URL,
                    onError = { sheetState.showManualAppInfoSheet = true }
                )
            },
            onOpenLicenses = {
                onBeforeNavigate()
                sheetState.showLicensesSheet = true
            },
            onOpenAbout = {
                onBeforeNavigate()
                sheetState.showManualAppInfoSheet = true
            },
            onOpenWhatsNew = {
                onBeforeNavigate()
                navigator.navigate(WhatsNewNavKey)
            },
            onReplaySplash = {
                onBeforeNavigate()
                onReplaySplash()
            },
            onOpenKeyboardShortcuts = {
                onBeforeNavigate()
                sheetState.showKeyboardShortcutsDialog = true
            }
        )
    }
}

/**
 * Renders app-wide modal sheets and dialogs (adaptive to centered cards on Medium/Large viewports).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowTimeGlobalSheetsHost(
    sheetState: ShowTimeGlobalSheetState,
    appStateHolder: AppStateHolder,
    appTheme: AppTheme,
    isDynamicColorEnabled: Boolean,
    isProActive: Boolean
) {
    val coroutineScope = rememberCoroutineScope()

    if (sheetState.showManualAppInfoSheet) {
        AppInfoBottomSheet(
            showDontShowAgain = false,
            onDismissRequest = { sheetState.showManualAppInfoSheet = false },
            onOpenLicenses = {
                sheetState.showManualAppInfoSheet = false
                sheetState.showLicensesSheet = true
            }
        )
    }

    if (sheetState.showLicensesSheet) {
        OpenSourceLicensesBottomSheet(
            onDismissRequest = { sheetState.showLicensesSheet = false }
        )
    }

    if (sheetState.showThemeSelectionSheet) {
        ThemeSelectionBottomSheet(
            currentTheme = appTheme,
            isDynamicColorEnabled = isDynamicColorEnabled,
            isProActive = isProActive,
            onThemeSelected = { newTheme ->
                appStateHolder.updateAppTheme(newTheme)
            },
            onDynamicColorToggled = { enabled ->
                appStateHolder.updateDynamicColor(enabled)
            },
            onUpgradeToPro = {
                sheetState.showThemeSelectionSheet = false
                sheetState.showProPaywallSheet = true
            },
            onDismissRequest = { sheetState.showThemeSelectionSheet = false }
        )
    }

    if (sheetState.showLocalizationSettingsSheet) {
        LocalizationSettingsBottomSheet(
            onDismissRequest = { sheetState.showLocalizationSettingsSheet = false }
        )
    }

    if (sheetState.showProPaywallSheet) {
        val availableProducts by appStateHolder.availableProducts.collectAsState()
        var isRestoring by remember { mutableStateOf(false) }

        ProPaywallBottomSheet(
            products = availableProducts,
            isProActive = isProActive,
            isRestoring = isRestoring,
            onPurchaseClick = { act, product ->
                appStateHolder.purchaseProduct(activity = act, product = product)
            },
            onRestoreClick = {
                coroutineScope.launch {
                    isRestoring = true
                    appStateHolder.restorePurchases()
                    isRestoring = false
                }
            },
            onDismissRequest = { sheetState.showProPaywallSheet = false }
        )
    }

    if (sheetState.showKeyboardShortcutsDialog) {
        KeyboardShortcutsDialog(
            onDismissRequest = { sheetState.showKeyboardShortcutsDialog = false }
        )
    }
}
