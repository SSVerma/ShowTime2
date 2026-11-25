package com.ssverma.core.navigation.nav3

import androidx.navigation3.runtime.NavMetadataKey

object Nav3MetadataKeys {
    object NavKey : NavMetadataKey<androidx.navigation3.runtime.NavKey> {
        override fun toString(): String = "Nav3MetadataKeys.NavKey"
    }

    object TabKey : NavMetadataKey<androidx.navigation3.runtime.NavKey> {
        override fun toString(): String = "Nav3MetadataKeys.TabKey"
    }

    object TransitionStyle : NavMetadataKey<NavTransitionStyle> {
        override fun toString(): String = "Nav3MetadataKeys.TransitionStyle"
    }

    object AlwaysShowSplit : NavMetadataKey<Boolean> {
        override fun toString(): String = "Nav3MetadataKeys.AlwaysShowSplit"
    }

    object ListDetailParticipant : NavMetadataKey<Boolean> {
        override fun toString(): String = "Nav3MetadataKeys.ListDetailParticipant"
    }
}

enum class NavTransitionStyle {
    Default,
    TopReveal,
    PeerSequential,
    TabSwitch
}
