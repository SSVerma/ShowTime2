package com.ssverma.core.networking

import com.ssverma.core.networking.config.AdditionalServiceConfig
import com.ssverma.core.networking.service.ServiceEnvironment

interface RestClient {
    fun <T> createService(
        environment: ServiceEnvironment<T>,
        serviceConfig: AdditionalServiceConfig? = null
    ): T
}