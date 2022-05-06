package com.ssverma.showtime.ui.people

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.domain.model.person.PersonDetailsConfig
import com.ssverma.showtime.domain.usecase.person.PersonDetailsUseCase
import com.ssverma.showtime.navigation.AppDestination
import com.ssverma.showtime.ui.FetchDataUiState
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

    var personDetailUiState by mutableStateOf<PersonDetailUiState>(FetchDataUiState.Idle)
        private set

    init {
        fetchPersonDetails()
    }

    fun fetchPersonDetails(coroutineScope: CoroutineScope = viewModelScope) {
        personDetailUiState = FetchDataUiState.Loading

        coroutineScope.launch {
            val personDetailsConfig = PersonDetailsConfig(personId = personId)
            val result = personDetailsUseCase(personDetailsConfig)

            personDetailUiState = when (result) {
                is DomainResult.Error -> {
                    FetchDataUiState.Error(result.error)
                }
                is DomainResult.Success -> {
                    _imageShots.value = result.data.imageShots
                    FetchDataUiState.Success(result.data)
                }
            }
        }
    }
}