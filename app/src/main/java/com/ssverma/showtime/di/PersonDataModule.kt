package com.ssverma.showtime.di

import com.ssverma.api.service.tmdb.response.RemoteImageShot
import com.ssverma.api.service.tmdb.response.RemotePerson
import com.ssverma.core.domain.model.ImageShot
import com.ssverma.shared.data.mapper.ImageShotsMapper
import com.ssverma.shared.data.mapper.ListMapper
import com.ssverma.shared.data.mapper.Mapper
import com.ssverma.showtime.data.mapper.PersonMapper
import com.ssverma.showtime.data.mapper.PersonsMapper
import com.ssverma.showtime.data.remote.DefaultPersonRemoteDataSource
import com.ssverma.showtime.data.remote.PersonRemoteDataSource
import com.ssverma.showtime.data.repository.DefaultPersonRepository
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