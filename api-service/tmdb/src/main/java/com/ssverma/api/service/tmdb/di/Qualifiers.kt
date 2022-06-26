package com.ssverma.api.service.tmdb.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TmdbServiceBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TmdbServiceApiReadAccessToken