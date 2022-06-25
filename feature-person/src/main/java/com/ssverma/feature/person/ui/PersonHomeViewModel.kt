package com.ssverma.feature.person.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.shared.domain.model.person.Person
import com.ssverma.feature.person.domain.usecase.PopularPersonsPaginatedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class PersonHomeViewModel @Inject constructor(
    popularPersonsUseCase: PopularPersonsPaginatedUseCase
) : ViewModel() {
    val popularPersons: Flow<PagingData<Person>> = popularPersonsUseCase()
        .cachedIn(viewModelScope)
}