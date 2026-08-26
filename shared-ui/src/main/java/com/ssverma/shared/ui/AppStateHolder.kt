package com.ssverma.shared.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.di.AppScoped
import com.ssverma.shared.data.repository.BackupRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.ConfigurationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateHolder @Inject constructor(
    private val appConfigRepository: AppConfigRepository,
    private val configurationRepository: ConfigurationRepository,
    private val billingRepository: BillingRepository,
    private val backupRepository: BackupRepository,
    @AppScoped private val coroutineScope: CoroutineScope
) {
    val isProActive: StateFlow<Boolean> = billingRepository.isProActive

    val googleUser: StateFlow<GoogleUser?> = backupRepository.googleUser

    val appTheme: StateFlow<AppTheme> = appConfigRepository.appTheme
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.System
        )

    val isDynamicColorEnabled: StateFlow<Boolean> = appConfigRepository.isDynamicColorEnabled
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isAppInfoDismissed: StateFlow<Boolean> = appConfigRepository.isAppInfoBottomSheetDismissed
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val watchProviderRegion: StateFlow<String> = appConfigRepository.watchProviderRegion
    val contentLanguage: StateFlow<String> = appConfigRepository.contentLanguage

    private val _availableRegions = MutableStateFlow<List<WatchProviderRegion>>(emptyList())
    val availableRegions: StateFlow<List<WatchProviderRegion>> = _availableRegions.asStateFlow()

    private val _availableLanguages = MutableStateFlow<List<Language>>(emptyList())
    val availableLanguages: StateFlow<List<Language>> = _availableLanguages.asStateFlow()

    private val _availableProducts =
        MutableStateFlow<List<com.ssverma.core.billing.model.BillingProduct>>(emptyList())
    val availableProducts: StateFlow<List<com.ssverma.core.billing.model.BillingProduct>> =
        _availableProducts.asStateFlow()

    init {
        coroutineScope.launch {
            _availableProducts.value = billingRepository.getAvailableProducts()
        }
        coroutineScope.launch {
            when (val result = configurationRepository.fetchCountries()) {
                is Result.Success -> _availableRegions.value = result.data
                else -> Unit
            }
        }
        coroutineScope.launch {
            when (val result = configurationRepository.fetchLanguages()) {
                is Result.Success -> _availableLanguages.value = result.data
                else -> Unit
            }
        }
    }

    fun updateWatchProviderRegion(regionCode: String) {
        coroutineScope.launch {
            appConfigRepository.updateWatchProviderRegion(regionCode)
        }
    }

    fun updateContentLanguage(languageCode: String) {
        coroutineScope.launch {
            appConfigRepository.updateContentLanguage(languageCode)
        }
    }

    fun updateAppTheme(theme: AppTheme) {
        if (theme == AppTheme.OledMidnight && !isProActive.value) {
            return
        }
        coroutineScope.launch {
            appConfigRepository.updateAppTheme(theme)
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        coroutineScope.launch {
            appConfigRepository.updateDynamicColor(enabled)
        }
    }

    fun onDismissAppInfo(dontShowAgain: Boolean) {
        if (dontShowAgain) {
            coroutineScope.launch {
                appConfigRepository.dismissAppInfoBottomSheet()
            }
        }
    }

    fun purchaseProduct(
        activity: android.app.Activity,
        product: com.ssverma.core.billing.model.BillingProduct
    ) {
        coroutineScope.launch {
            billingRepository.purchaseProduct(activity, product)
        }
    }

    suspend fun restorePurchases(): Boolean {
        return billingRepository.restorePurchases()
    }
}

val LocalAppStateHolder = staticCompositionLocalOf<AppStateHolder> {
    error("No AppStateHolder provided")
}

val LocalAppInfoTrigger = staticCompositionLocalOf {
    { } // No-op default
}
