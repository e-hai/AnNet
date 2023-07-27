package com.an.net.okhttp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Recommend(

    @Json(name = "title")
    val title: String = "",

    @Json(name = "list")
    val list: List<Product> = emptyList()
)
