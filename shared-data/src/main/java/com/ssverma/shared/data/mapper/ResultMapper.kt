package com.ssverma.shared.data.mapper

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.core.networking.adapter.ApiResponse

suspend fun <RemoteSuccess, RemoteError, DomainSuccess, FeatureFailure> ApiResponse<RemoteSuccess, RemoteError>.asDomainResult(
    mapFeatureFailure: (ApiResponse.Error.ClientError<RemoteError>) -> Failure<FeatureFailure> = {
        Failure.CoreFailure.UnexpectedFailure
    },
    mapRemoteToDomain: suspend (ApiResponse.Success<RemoteSuccess>) -> DomainSuccess
): Result<DomainSuccess, Failure<FeatureFailure>> {
    return when (this) {
        is ApiResponse.Error.ClientError -> {
            Result.Error(mapFeatureFailure(this))
        }
        is ApiResponse.Error.UnexpectedError -> {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        is ApiResponse.Error.NetworkError -> {
            Result.Error(Failure.CoreFailure.NetworkFailure)
        }
        is ApiResponse.Error.ServerError -> {
            Result.Error(Failure.CoreFailure.ServiceFailure)
        }
        is ApiResponse.Success -> {
            Result.Success(data = mapRemoteToDomain(this))
        }
    }
}

suspend fun <RemoteSuccess, RemoteError, DomainSuccess> ApiResponse<RemoteSuccess, RemoteError>.asDomainResult(
    mapRemoteToDomain: suspend (ApiResponse.Success<RemoteSuccess>) -> DomainSuccess
): Result<DomainSuccess, Failure.CoreFailure> {
    return when (this) {
        is ApiResponse.Error.ClientError,
        is ApiResponse.Error.UnexpectedError -> {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        is ApiResponse.Error.NetworkError -> {
            Result.Error(Failure.CoreFailure.NetworkFailure)
        }
        is ApiResponse.Error.ServerError -> {
            Result.Error(Failure.CoreFailure.ServiceFailure)
        }
        is ApiResponse.Success -> {
            Result.Success(data = mapRemoteToDomain(this))
        }
    }
}