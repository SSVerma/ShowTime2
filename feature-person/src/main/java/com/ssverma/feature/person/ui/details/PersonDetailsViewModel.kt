package com.ssverma.feature.person.ui.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.feature.person.domain.model.PersonDetailsConfig
import com.ssverma.feature.person.domain.usecase.PersonDetailsUseCase
import com.ssverma.feature.person.ui.common.PersonDetailUiState
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.ImageShot
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PersonDetailsViewModel.Factory::class)
class PersonDetailsViewModel @AssistedInject constructor(
    @Assisted val personId: Int,
    private val personDetailsUseCase: PersonDetailsUseCase
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(personId: Int): PersonDetailsViewModel
    }

    var imageShots by mutableStateOf<List<ImageShot>>(emptyList())

    var personDetailUiState by mutableStateOf<PersonDetailUiState>(UiState.Idle)
        private set

    init {
        fetchPersonDetails()
    }

    fun fetchPersonDetails(coroutineScope: CoroutineScope = viewModelScope) {
        personDetailUiState = UiState.Loading

        coroutineScope.launch {
            val personDetailsConfig = PersonDetailsConfig(personId = personId)
            val result = personDetailsUseCase(personDetailsConfig)

            personDetailUiState = when (result) {
                is Result.Error -> {
                    UiState.Error(result.error)
                }

                is Result.Success -> {
                    imageShots = result.data.imageShots
                    UiState.Success(result.data)
                }
            }
        }
    }
}
