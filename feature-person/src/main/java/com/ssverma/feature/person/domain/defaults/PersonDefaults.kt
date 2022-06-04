package com.ssverma.feature.person.domain.defaults

import com.ssverma.shared.domain.model.PersonDetailAppendable

object PersonDefaults {

    fun personAppendable(): List<PersonDetailAppendable> {
        return listOf(
            PersonDetailAppendable.CombinedCredits,
            PersonDetailAppendable.Images,
        )
    }
}