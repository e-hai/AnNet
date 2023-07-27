package com.an.net.okhttp.api

import com.an.net.okhttp.Constant
import com.an.net.okhttp.model.ApiResponse
import com.an.net.okhttp.model.Recommend
import retrofit2.http.GET

interface UseCaseApi {

    @GET(Constant.URL_USE_CASE)
    suspend fun getRecommendProducts(): ApiResponse<Recommend>
}
