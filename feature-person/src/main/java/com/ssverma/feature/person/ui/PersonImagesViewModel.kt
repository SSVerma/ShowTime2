package com.ssverma.feature.person.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ssverma.feature.person.domain.usecase.PersonImagesPaginatedUseCase
import com.ssverma.feature.person.navigation.PersonImageShotsDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PersonImagesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    personImagesUseCase: PersonImagesPaginatedUseCase
) : ViewModel() {

    private val personId = savedStateHandle.get<Int>(PersonImageShotsDestination.PersonId) ?: 0

    val personImages = personImagesUseCase(personId)
        .cachedIn(viewModelScope)
}