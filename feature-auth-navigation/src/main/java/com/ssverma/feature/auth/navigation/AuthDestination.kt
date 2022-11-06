package com.ssverma.feature.auth.navigation

import com.ssverma.core.navigation.StandaloneResultDestination

object AuthDestination : StandaloneResultDestination("auth") {
    override val resultKey: String
        get() = "auth_succeeded"
}