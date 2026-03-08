package com.ssverma.feature.person.ui.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.feature.person.domain.model.PersonDetailsConfig
import com.ssverma.feature.person.domain.usecase.PersonDetailsUseCase
import com.ssverma.feature.person.navigation.PersonDetailDestination
import com.ssverma.feature.person.ui.common.PersonDetailUiState
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.ImageShot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val personDetailsUseCase: PersonDetailsUseCase
) : ViewModel() {

    private val personId = savedStateHandle.get<Int>(PersonDetailDestination.PersonId) ?: 0

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
