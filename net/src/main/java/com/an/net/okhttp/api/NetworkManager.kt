package com.an.net.okhttp.api

import android.app.Application
import com.alibaba.sdk.android.oss_android_sdk.BuildConfig
import com.an.net.okhttp.Constant
import com.squareup.moshi.Moshi
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class NetworkManager(context: Application) {

    private val okHttpClient: OkHttpClient
    private val retrofit: Retrofit


    init {
        val cache = Cache(context.cacheDir, 10 * 1024 * 1024)
        okHttpClient = createOkHttpClient(cache)
        retrofit = createRetrofit(okHttpClient)
    }


    private fun createOkHttpClient(cache: Cache): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            setLevel(
                if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY else
                    HttpLoggingInterceptor.Level.NONE
            )
        }
        return OkHttpClient()
            .newBuilder()
            .addInterceptor(loggingInterceptor)
            .cache(cache)
            .build()
    }

    private fun createRetrofit(client: OkHttpClient): Retrofit {
        val moshi = Moshi.Builder().build()
        return Retrofit.Builder()
            .baseUrl(Constant.HOST)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }


    fun useCaseApi(): UseCaseApi {
        return retrofit.create(UseCaseApi::class.java)
    }

    companion object {
        @Volatile
        private var INSTANCE: NetworkManager? = null

        fun getInstance(application: Application): NetworkManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkManager(application).also { INSTANCE = it }
            }
    }
}