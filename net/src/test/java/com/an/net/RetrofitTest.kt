package com.an.net

import com.an.net.okhttp.api.UseCaseApi
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class RetrofitTest {

    lateinit var mockWebServer: MockWebServer
    lateinit var httpClient: OkHttpClient
    lateinit var retrofit: Retrofit

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        httpClient = OkHttpClient()
            .newBuilder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder().build()
        retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @After
    fun close() {
        mockWebServer.shutdown()
    }

    @Test
    fun addition_isCorrect() {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(
                """
{
    "code": 0,
    "message": "success",
    "data": {
        "title": "recommend product",
        "list": [
            {
                "id": 1,
                "url": "http://www.google.com"
            },
            {
                "id": 2,
                "url": "http://www.baidu.com"
            },
            {
                "id": 3,
                "url": "http://www.SoSo.com"
            }
        ]
    }
}
            """.trimIndent()
            )
        mockWebServer.enqueue(response)
        runBlocking {
            val result = retrofit.create(UseCaseApi::class.java).getRecommendProducts()
            println(result)
        }
    }
}