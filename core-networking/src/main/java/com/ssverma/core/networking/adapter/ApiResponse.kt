package com.ssverma.core.networking.adapter

import com.ssverma.core.networking.adapter.ApiResponse.Error
import com.ssverma.core.networking.adapter.ApiResponse.Success
import okhttp3.Headers
import java.net.ConnectException

/**
 * Generic api response holder class.
 *
 * @param S: Type of response body expected in case of [Success]
 * @param E: Type of error body expected in case of [Error.ClientError]
 *
 * Usage:
 *
 *  interface MyService {
 *      @GET("/user")
 *      suspend fun getUser(): ApiResponse<User, ValidationError>
 *  }
 *
 */
sealed class ApiResponse<out S, out E> {

    /**
     * Called for response with [200, 300) http code.
     */
    data class Success<T>(
        val body: T,
        val payload: ResponsePayload
    ) : ApiResponse<T, Nothing>()

    /**
     * Called for response with http code except [200, 300).
     */
    sealed class Error<E> : ApiResponse<Nothing, E>() {

        /**
         * Called for response with [400, 500) http code.
         */
        data class ClientError<E>(
            val body: E?,
            val payload: ResponsePayload
        ) : Error<E>()

        /**
         * Called for response with [500, 600) http code.
         */
        data class ServerError(
            val payload: ResponsePayload
        ) : Error<Nothing>()

        /**
         * Called for network errors occurred while making network request.
         */
        data class NetworkError(
            val connectionException: ConnectException
        ) : Error<Nothing>()

        /**
         * Called for any other unexpected / unknown errors.
         */
        data class UnexpectedError(
            val throwable: Throwable
        ) : Error<Nothing>()
    }

}

data class ResponsePayload(
    val httpCode: Int,
    val statusMessage: String?,
    val headers: Headers,
    val row: okhttp3.Response,
)