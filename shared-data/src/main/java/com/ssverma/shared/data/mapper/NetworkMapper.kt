package com.ssverma.shared.data.mapper

import com.ssverma.api.service.tmdb.response.RemoteNetwork
import com.ssverma.shared.domain.model.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun RemoteNetwork.asNetwork(): Network {
    return Network(
        id = id,
        name = name.orEmpty()
    )
}

suspend fun List<RemoteNetwork>.asNetworks() = withContext(Dispatchers.Default) {
    map { it.asNetwork() }
}
