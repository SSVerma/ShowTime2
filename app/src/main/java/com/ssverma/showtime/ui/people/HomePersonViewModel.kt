package com.ssverma.showtime.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.domain.usecase.person.PopularPersonsPaginatedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomePersonViewModel @Inject constructor(
    popularPersonsUseCase: PopularPersonsPaginatedUseCase
) : ViewModel() {
    val popularPersons: Flow<PagingData<Person>> = popularPersonsUseCase()
        .cachedIn(viewModelScope)
}