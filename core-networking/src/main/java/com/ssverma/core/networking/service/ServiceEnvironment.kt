package com.ssverma.core.networking.service

/**
 * Mandatory information required by retrofit to create
 * service's implementation.
 *
 * Example:
 *
 *  To create a given service 'MyService' against endpoint 'http://localhost:8080'
 *
 *  interface MyService {
 *      @GET("/myData")
 *      fun getMyData(): MyData
 *  }
 *
 *  [baseUrl] http://localhost:8080
 *  [serviceClass] MyService::class.java
 *
 */
interface ServiceEnvironment<T> {
    val baseUrl: String
    val serviceClass: Class<T>
}