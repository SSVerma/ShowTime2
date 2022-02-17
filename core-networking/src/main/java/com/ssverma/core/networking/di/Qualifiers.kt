package com.ssverma.core.networking.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class CoreNetworking

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ClientOverride
