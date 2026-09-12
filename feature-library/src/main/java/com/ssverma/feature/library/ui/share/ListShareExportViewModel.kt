package com.ssverma.feature.library.ui.share

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.billing.BillingRepository
import com.ssverma.shared.ads.quota.PassKey
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.library.ListShareCardFormat
import com.ssverma.shared.domain.model.library.ListShareTheme
import com.ssverma.shared.domain.model.library.SecretSharedList
import com.ssverma.shared.domain.model.library.SecretSharedListItem
import com.ssverma.shared.domain.repository.LibraryRepository
import com.ssverma.shared.domain.repository.SecretSharedListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListShareExportUiState(
    val selectedTheme: ListShareTheme = ListShareTheme.CLASSIC_SHOWTIME,
    val selectedFormat: ListShareCardFormat = ListShareCardFormat.STORY_9_16,
    val isCollaborative: Boolean = false,
    val isCreatingLink: Boolean = false,
    val shareCode: String? = null,
    val secretSharedList: SecretSharedList? = null,
    val isProActive: Boolean = false,
    val isPassActive: Boolean = false,
    val isWatermarkFree: Boolean = false,
    val isProPaymentEnabled: Boolean = true,
    val isExportingImage: Boolean = false,
    val isGateOpen: Boolean = false,
    val pendingTheme: ListShareTheme? = null,
    val errorMessage: String? = null
)

val SecretShareThemesPassKey = PassKey("list_share_themes")

@HiltViewModel
class ListShareExportViewModel @Inject constructor(
    private val secretSharedListRepository: SecretSharedListRepository,
    private val libraryRepository: LibraryRepository,
    private val billingRepository: BillingRepository,
    private val rewardManager: RewardManager,
    private val rewardedAdManager: RewardedAdManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListShareExportUiState())
    val uiState: StateFlow<ListShareExportUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            billingRepository.isProActive.collectLatest { isPro ->
                _uiState.update { current ->
                    val unlocked = isPro || current.isPassActive
                    current.copy(
                        isProActive = isPro,
                        isWatermarkFree = unlocked
                    )
                }
            }
        }

        viewModelScope.launch {
            billingRepository.isBillingEnabled.collectLatest { isEnabled ->
                _uiState.update { it.copy(isProPaymentEnabled = isEnabled) }
            }
        }

        viewModelScope.launch {
            rewardManager.isPassActive(SecretShareThemesPassKey).collectLatest { passUnlocked ->
                _uiState.update { current ->
                    val unlocked = current.isProActive || passUnlocked
                    current.copy(
                        isPassActive = passUnlocked,
                        isWatermarkFree = unlocked
                    )
                }
            }
        }

        rewardedAdManager.loadAd()
    }

    fun selectTheme(theme: ListShareTheme) {
        if (theme == ListShareTheme.CLASSIC_SHOWTIME || _uiState.value.isProActive || _uiState.value.isPassActive) {
            _uiState.update {
                it.copy(
                    selectedTheme = theme,
                    isGateOpen = false,
                    pendingTheme = null
                )
            }
        } else {
            _uiState.update { it.copy(isGateOpen = true, pendingTheme = theme) }
            rewardedAdManager.loadAd()
        }
    }

    fun selectFormat(format: ListShareCardFormat) {
        _uiState.update { it.copy(selectedFormat = format) }
    }

    fun initSecretShare(shareCode: String?) {
        if (shareCode != null && _uiState.value.shareCode == null) {
            _uiState.update { it.copy(shareCode = shareCode) }
            viewModelScope.launch {
                secretSharedListRepository.observeSecretSharedList(shareCode)
                    .collectLatest { list ->
                        if (list != null) {
                            _uiState.update {
                                it.copy(
                                    secretSharedList = list,
                                    isCollaborative = list.isCollaborative
                                )
                            }
                        }
                    }
            }
        }
    }

    fun setCollaborative(allow: Boolean) {
        _uiState.update { it.copy(isCollaborative = allow) }
        val code = _uiState.value.shareCode
        if (code != null) {
            viewModelScope.launch {
                secretSharedListRepository.updateCollaborativeStatus(code, allow)
            }
        }
    }

    fun setExportingImage(isExporting: Boolean) {
        _uiState.update { it.copy(isExportingImage = isExporting) }
    }

    fun closeGate() {
        _uiState.update { it.copy(isGateOpen = false, pendingTheme = null) }
    }

    fun generateSecretLink(
        title: String,
        description: String?,
        items: List<SecretSharedListItem>,
        ownerName: String,
        customListId: String? = null,
        onSuccess: (String) -> Unit = {}
    ) {
        val existing = _uiState.value.shareCode
        if (existing != null) {
            onSuccess(existing)
            return
        }
        if (_uiState.value.isCreatingLink) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingLink = true, errorMessage = null) }
            val sanitizedOwnerName =
                ownerName.takeIf { it.isNotBlank() && !it.equals("Me", ignoreCase = true) }
                    ?: "Friend"
            val result = secretSharedListRepository.createSecretShare(
                title = title,
                description = description,
                items = items,
                isCollaborative = _uiState.value.isCollaborative,
                ownerName = sanitizedOwnerName
            )
            when (result) {
                is Result.Success -> {
                    val code = result.data.shareCode
                    if (customListId != null) {
                        libraryRepository.updateCustomListSecretShareCode(customListId, code)
                    }
                    _uiState.update {
                        it.copy(
                            isCreatingLink = false,
                            shareCode = code,
                            secretSharedList = result.data
                        )
                    }
                    onSuccess(code)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isCreatingLink = false,
                            errorMessage = "Failed to generate secret share link"
                        )
                    }
                }
            }
        }
    }

    fun revokeSecretShare(customListId: String? = null, onRevoked: () -> Unit) {
        val code = _uiState.value.shareCode ?: return
        viewModelScope.launch {
            secretSharedListRepository.revokeSecretShare(code)
            if (customListId != null) {
                libraryRepository.updateCustomListSecretShareCode(customListId, null)
            }
            _uiState.update { it.copy(shareCode = null, secretSharedList = null) }
            onRevoked()
        }
    }

    fun unlockThemesWithRewardedAd(activity: Activity) {
        rewardedAdManager.showRewardedAdIfReady(activity) {
            viewModelScope.launch {
                rewardManager.grantTimedPass(SecretShareThemesPassKey)
                val pending = _uiState.value.pendingTheme ?: ListShareTheme.VINTAGE_35MM
                _uiState.update {
                    it.copy(
                        isPassActive = true,
                        isWatermarkFree = true,
                        isGateOpen = false,
                        selectedTheme = pending,
                        pendingTheme = null
                    )
                }
            }
        }
    }
}
