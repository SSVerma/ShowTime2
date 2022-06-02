package com.ssverma.showtime.domain.defaults.person

import com.ssverma.core.domain.model.PersonDetailAppendable

object PersonDefaults {

    fun personAppendable(): List<PersonDetailAppendable> {
        return listOf(
            PersonDetailAppendable.CombinedCredits,
            PersonDetailAppendable.Images,
        )
    }
}