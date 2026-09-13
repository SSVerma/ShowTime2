package com.ssverma.shared.domain.usecase.library

import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCustomListsUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    operator fun invoke(): Flow<List<CustomList>> {
        return libraryRepository.getCustomListsFlow()
    }
}
