package com.ssverma.core.networking.adapter

import com.google.common.truth.Truth.assertThat
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.MalformedJsonException
import com.ssverma.core.networking.enqueueResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.net.HttpURLConnection

@ExperimentalCoroutinesApi
class ApiResponseCallAdaptorFactoryTest {

    private lateinit var mockWebServer: MockWebServer

    private lateinit var fakeService: FakeApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        fakeService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addCallAdapterFactory(ApiResponseCallAdaptorFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FakeApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `verify api response success returns correct type`() = runTest {
        mockWebServer.enqueueResponse(
            fileName = "fake-user-api-response-200.json",
            mockResponseBuilder = {
                addHeader("FakeKey : FakeValue")
            }
        )

        val response = fakeService.getFakeUser()

        assertThat(response).isInstanceOf(ApiResponse.Success::class.java)

        val succeededResponse = response as ApiResponse.Success<FakeUser>

        assertThat(succeededResponse.body).isInstanceOf(FakeUser::class.java)
        assertThat(succeededResponse.body.id).isEqualTo(1232)
        assertThat(succeededResponse.body.name).isEqualTo("SS Verma")
        assertThat(succeededResponse.payload.httpCode).isEqualTo(HttpURLConnection.HTTP_OK)
        assertThat(succeededResponse.payload.statusMessage).isEqualTo("OK")
        assertThat(succeededResponse.payload.headers["FakeKey"]).isEqualTo("FakeValue")
    }

    @Test
    fun `verify api response client error returns correct type`() = runTest {
        mockWebServer.enqueueResponse(
            fileName = "fake-user-api-response-400.json",
            httpCode = HttpURLConnection.HTTP_BAD_REQUEST
        )

        val response = fakeService.getFakeUser()

        assertThat(response).isInstanceOf(ApiResponse.Error.ClientError::class.java)

        val errorResponse = response as ApiResponse.Error.ClientError<FakeUserErrorBody>

        assertThat(errorResponse.body).isNotNull()
        assertThat(errorResponse.body).isInstanceOf(FakeUserErrorBody::class.java)
        assertThat(errorResponse.body?.userId).isEqualTo(11235)
        assertThat(errorResponse.body?.message).isEqualTo("Invalid user detail")
        assertThat(errorResponse.payload.httpCode).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST)
    }

    @Test
    fun `verify api response client error body parsing fail returns null error body`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(404)
        )

        val response = fakeService.getFakeUser()
        assertThat(response).isInstanceOf(ApiResponse.Error.ClientError::class.java)

        val errorResponse = response as ApiResponse.Error.ClientError<FakeUserErrorBody>
        assertThat(errorResponse.body).isNull()
    }

    @Test
    fun `verify api response server error returns correct type`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
        )

        val response = fakeService.getFakeUser()
        assertThat(response).isInstanceOf(ApiResponse.Error.ServerError::class.java)

        val errorResponse = response as ApiResponse.Error.ServerError
        assertThat(errorResponse.payload.httpCode).isEqualTo(HttpURLConnection.HTTP_INTERNAL_ERROR)
    }

    @Test
    fun `verify api response network failure returns correct type`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        )

        val response = fakeService.getFakeUser()
        assertThat(response).isInstanceOf(ApiResponse.Error.NetworkError::class.java)

        val errorResponse = response as ApiResponse.Error.NetworkError
        assertThat(errorResponse.connectionException).hasCauseThat()
    }

    @Test
    fun `verify api response invalid http code returns correct type`() = runTest {
        mockWebServer.enqueueResponse(
            fileName = "fake-user-api-response-200.json",
            httpCode = 602
        )

        val response = fakeService.getFakeUser()
        assertThat(response).isInstanceOf(ApiResponse.Error.UnexpectedError::class.java)

        val errorResponse = response as ApiResponse.Error.UnexpectedError
        assertThat(errorResponse.throwable).hasCauseThat()
    }

    @Test
    fun `verify api response null response body returns correct type`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
        )

        val response = fakeService.getFakeUser()
        assertThat(response).isInstanceOf(ApiResponse.Error.UnexpectedError::class.java)

        val errorResponse = response as ApiResponse.Error.UnexpectedError
        assertThat(errorResponse.throwable).hasCauseThat()
    }

    @Test
    fun `verify api response malformed json body returns correct type`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setBody("{ Malformed json")
        )

        val response = fakeService.getFakeUser()
        assertThat(response).isInstanceOf(ApiResponse.Error.UnexpectedError::class.java)

        val errorResponse = response as ApiResponse.Error.UnexpectedError
        assertThat(errorResponse.throwable).isInstanceOf(MalformedJsonException::class.java)
    }

    @Test
    fun `verify api response invalid return type throws exception`() {
        mockWebServer.enqueueResponse(
            fileName = "fake-user-api-response-200.json"
        )

        val response = fakeService.getNonSuspendUser().execute().body()
        val succeededResponse = response as ApiResponse.Success<FakeUser>
        assertThat(succeededResponse.payload.httpCode).isEqualTo(HttpURLConnection.HTTP_OK)
    }
}

private data class FakeUser(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

private data class FakeUserErrorBody(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("message")
    val message: String
)

private interface FakeApiService {
    @GET("/fakeUser")
    suspend fun getFakeUser(): ApiResponse<FakeUser, FakeUserErrorBody>

    @GET("/fakeNonSuspendUser")
    fun getNonSuspendUser(): Call<ApiResponse<FakeUser, FakeUserErrorBody>>
}