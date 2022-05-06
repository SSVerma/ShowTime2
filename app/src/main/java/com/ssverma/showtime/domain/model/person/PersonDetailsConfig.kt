package com.ssverma.showtime.domain.model.person

import com.ssverma.showtime.domain.defaults.person.PersonDefaults
import com.ssverma.showtime.domain.model.PersonDetailAppendable

data class PersonDetailsConfig(
    val personId: Int,
    val appendable: List<PersonDetailAppendable> = PersonDefaults.personAppendable()
)