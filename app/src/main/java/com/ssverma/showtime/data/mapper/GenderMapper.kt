package com.ssverma.showtime.data.mapper

import com.ssverma.showtime.domain.model.Gender
import javax.inject.Inject

class GenderMapper @Inject constructor() : Mapper<Int, Gender>() {
    override suspend fun map(input: Int): Gender {
        return when (input) {
            1 -> Gender.Female
            2 -> Gender.Male
            else -> Gender.Unknown
        }
    }
}