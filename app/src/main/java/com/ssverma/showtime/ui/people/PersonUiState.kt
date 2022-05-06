package com.ssverma.showtime.ui.people

import com.ssverma.showtime.domain.failure.person.PersonFailure
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.ui.FetchDataUiState

typealias PersonDetailUiState = FetchDataUiState<Person, PersonFailure>