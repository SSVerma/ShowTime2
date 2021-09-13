package com.ssverma.showtime.ui.people

import androidx.lifecycle.*
import com.ssverma.showtime.api.AppendableQueryMap
import com.ssverma.showtime.api.QueryMultiValue
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.data.repository.PersonRepository
import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

val personDetailsAppendable = QueryMultiValue.andBuilder()
    .and(TmdbApiTiedConstants.PersonDetailsAppendableResponseTypes.Credits)
    .and(TmdbApiTiedConstants.PersonDetailsAppendableResponseTypes.Images)

@HiltViewModel
class PersonViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId = savedStateHandle.get<Int>(AppDestination.PersonDetails.PersonId) ?: 0

    private val _liveImageShots = MediatorLiveData<List<ImageShot>>()
    val imageShots: LiveData<List<ImageShot>> get() = _liveImageShots

    val livePersonDetails: LiveData<Result<Person>> = liveData {
        personRepository.fetchPersonDetails(
            personId = personId,
            queryMap = AppendableQueryMap.of(appendToResponse = personDetailsAppendable)
        ).collect {
            emit(it)

            if (it is Result.Success) {
                viewModelScope.launch {
                    val imageShots = it.data.imageShots
                    _liveImageShots.postValue(imageShots)
                }
            }
        }
    }
}