package com.ssverma.showtime.domain.mapper

import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.Gender

object GenderMapper : Mapper<Int, Gender>() {
    override suspend fun map(input: Int): Gender {
        return when (input) {
            1 -> Gender.Female(R.string.female)
            2 -> Gender.Male(R.string.male)
            else -> Gender.Unknown
        }
    }
}