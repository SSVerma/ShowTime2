package com.ssverma.showtime.ui.people

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ssverma.showtime.data.repository.PersonRepository
import com.ssverma.showtime.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PersonImagesViewModel @Inject constructor(
    personRepository: PersonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId = savedStateHandle.get<Int>(AppDestination.PersonImages.PersonId) ?: 0

    val personImages = personRepository
        .fetchPersonImagesGradually(personId = personId)
        .cachedIn(viewModelScope)
}