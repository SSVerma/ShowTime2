package com.ssverma.feature.person.ui.shots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ssverma.feature.person.domain.usecase.PersonImagesPaginatedUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = PersonImagesViewModel.Factory::class)
class PersonImagesViewModel @AssistedInject constructor(
    personImagesUseCase: PersonImagesPaginatedUseCase,
    @Assisted private val personId: Int
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(personId: Int): PersonImagesViewModel
    }

    val personImages = personImagesUseCase(personId)
        .cachedIn(viewModelScope)
}
