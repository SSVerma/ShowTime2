package com.ssverma.feature.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ssverma.feature.auth.domain.AuthManager
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface AuthEntryPoint {
    val authManager: AuthManager
}

private lateinit var authEntryPoint: AuthEntryPoint

@Composable
private fun requireAuthEntryPoint(): AuthEntryPoint {
    if (!::authEntryPoint.isInitialized) {
        authEntryPoint = EntryPoints.get(
            LocalContext.current.applicationContext,
            AuthEntryPoint::class.java
        )
    }
    return authEntryPoint
}

@Composable
fun authManager() = requireAuthEntryPoint().authManager