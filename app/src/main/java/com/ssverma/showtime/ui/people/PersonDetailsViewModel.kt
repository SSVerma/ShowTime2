package com.ssverma.showtime.ui.people

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.domain.Result
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.domain.model.person.PersonDetailsConfig
import com.ssverma.showtime.domain.usecase.person.PersonDetailsUseCase
import com.ssverma.showtime.navigation.AppDestination
import com.ssverma.core.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val personDetailsUseCase: PersonDetailsUseCase
) : ViewModel() {

    private val personId = savedStateHandle.get<Int>(AppDestination.PersonDetails.PersonId) ?: 0

    private val _imageShots = mutableStateOf<List<ImageShot>>(emptyList())
    val imageShots: State<List<ImageShot>> get() = _imageShots

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
                    _imageShots.value = result.data.imageShots
                    UiState.Success(result.data)
                }
            }
        }
    }
}