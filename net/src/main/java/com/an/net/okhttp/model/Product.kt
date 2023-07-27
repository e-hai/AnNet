package com.an.net.okhttp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 每个属性必须加上默认值，避免服务器返回json字段时抛出异常
 * **/
@JsonClass(generateAdapter = true)
data class Product(

    @Json(name = "id")
    val id: Int = -1,

    @Json(name = "url")
    val photo: String = ""
)