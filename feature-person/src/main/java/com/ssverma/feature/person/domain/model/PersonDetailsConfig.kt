package com.ssverma.feature.person.domain.model

import com.ssverma.feature.person.domain.defaults.PersonDefaults
import com.ssverma.shared.domain.model.PersonDetailAppendable

data class PersonDetailsConfig(
    val personId: Int,
    val appendable: List<PersonDetailAppendable> = PersonDefaults.personAppendable()
)