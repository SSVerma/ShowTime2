package com.ssverma.showtime.di

import com.ssverma.api.service.tmdb.response.RemoteImageShot
import com.ssverma.api.service.tmdb.response.RemotePerson
import com.ssverma.showtime.data.mapper.*
import com.ssverma.showtime.data.remote.DefaultPersonRemoteDataSource
import com.ssverma.showtime.data.remote.PersonRemoteDataSource
import com.ssverma.showtime.data.repository.DefaultPersonRepository
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.domain.repository.PersonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PersonDataModule {

    @Binds
    abstract fun providePersonMapper(
        personMapper: PersonMapper
    ): Mapper<RemotePerson, Person>

    @Binds
    abstract fun providePersonsMapper(
        personsMapper: PersonsMapper
    ): ListMapper<RemotePerson, Person>

    @Binds
    abstract fun provideImageShotsMapper(
        imageShotsMapper: ImageShotsMapper
    ): ListMapper<RemoteImageShot, ImageShot>

    @Binds
    abstract fun providePersonRemoteDataSource(
        defaultPersonRemoteDataSource: DefaultPersonRemoteDataSource
    ): PersonRemoteDataSource

    @Binds
    abstract fun providePersonRepository(
        defaultPersonRepository: DefaultPersonRepository
    ): PersonRepository
}