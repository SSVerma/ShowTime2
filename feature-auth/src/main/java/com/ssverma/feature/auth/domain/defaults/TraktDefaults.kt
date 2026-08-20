package com.ssverma.feature.auth.domain.defaults

object TraktDefaults {
    const val BaseUrl = "https://api.trakt.tv/"
    const val ActivationUrl = "https://trakt.tv/activate"
    const val ApiVersion = "2"
    
    // Default Open-Source Client ID for ShowTime (Safe for public repos as Device Code only uses client_id)
    const val DefaultClientId = "38848a60debb2652b41295b9588ebbf45b14f6bdf6a22f77ffbcad5ce29aaeb5"
    
    const val HeaderApiVersion = "trakt-api-version"
    const val HeaderApiKey = "trakt-api-key"
}
