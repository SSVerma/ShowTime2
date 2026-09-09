package com.ssverma.shared.data.mapper

import com.ssverma.api.service.tmdb.response.RemoteCompany
import com.ssverma.shared.domain.model.Company
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun RemoteCompany.asCompany(): Company {
    return Company(
        id = id,
        name = name.orEmpty()
    )
}

suspend fun List<RemoteCompany>.asCompanies() = withContext(Dispatchers.Default) {
    map { it.asCompany() }
}
