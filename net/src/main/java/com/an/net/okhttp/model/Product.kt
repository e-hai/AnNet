package com.an.net.okhttp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Product(

    @Json(name = "id")
    val id: Int = -1,

    @Json(name = "url")
    val photo: String = ""
)