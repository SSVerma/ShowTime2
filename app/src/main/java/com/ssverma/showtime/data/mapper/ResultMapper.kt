package com.ssverma.showtime.data.mapper

import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.failure.Failure

suspend fun <RemoteSuccess, RemoteError, DomainSuccess, FeatureFailure> ApiResponse<RemoteSuccess, RemoteError>.asDomainResult(
    mapFeatureFailure: (ApiResponse.Error.ClientError<RemoteError>) -> Failure<FeatureFailure> = {
        Failure.CoreFailure.UnexpectedFailure
    },
    mapRemoteToDomain: suspend (ApiResponse.Success<RemoteSuccess>) -> DomainSuccess
): DomainResult<DomainSuccess, Failure<FeatureFailure>> {
    return when (this) {
        is ApiResponse.Error.ClientError -> {
            DomainResult.Error(mapFeatureFailure(this))
        }
        is ApiResponse.Error.UnexpectedError -> {
            DomainResult.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        is ApiResponse.Error.NetworkError -> {
            DomainResult.Error(Failure.CoreFailure.NetworkFailure)
        }
        is ApiResponse.Error.ServerError -> {
            DomainResult.Error(Failure.CoreFailure.ServiceFailure)
        }
        is ApiResponse.Success -> {
            DomainResult.Success(data = mapRemoteToDomain(this))
        }
    }
}

suspend fun <RemoteSuccess, RemoteError, DomainSuccess> ApiResponse<RemoteSuccess, RemoteError>.asDomainResult(
    mapRemoteToDomain: suspend (ApiResponse.Success<RemoteSuccess>) -> DomainSuccess
): DomainResult<DomainSuccess, Failure.CoreFailure> {
    return when (this) {
        is ApiResponse.Error.ClientError,
        is ApiResponse.Error.UnexpectedError -> {
            DomainResult.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        is ApiResponse.Error.NetworkError -> {
            DomainResult.Error(Failure.CoreFailure.NetworkFailure)
        }
        is ApiResponse.Error.ServerError -> {
            DomainResult.Error(Failure.CoreFailure.ServiceFailure)
        }
        is ApiResponse.Success -> {
            DomainResult.Success(data = mapRemoteToDomain(this))
        }
    }
}