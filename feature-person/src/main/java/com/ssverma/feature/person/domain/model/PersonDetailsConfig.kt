package com.ssverma.feature.person.domain.model

import com.ssverma.core.domain.model.PersonDetailAppendable
import com.ssverma.feature.person.domain.defaults.PersonDefaults

data class PersonDetailsConfig(
    val personId: Int,
    val appendable: List<PersonDetailAppendable> = PersonDefaults.personAppendable()
)