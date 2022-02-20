package com.ssverma.core.networking.config

/**
 * All the configurations which a service can use to update its behavior.
 * @see [AdditionalServiceConfig] which allows client modifiable
 * service configs.
 */
internal interface ServiceConfig : RetrofitConfig, OkHttpConfig