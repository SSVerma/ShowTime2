package com.ssverma.feature.person.ui

import com.ssverma.core.ui.UiState
import com.ssverma.feature.person.domain.failure.PersonFailure
import com.ssverma.shared.domain.model.person.Person

typealias PersonDetailUiState = UiState<Person, PersonFailure>