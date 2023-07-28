package com.an.net.okhttp.model

import com.squareup.moshi.JsonClass

/**
 * 每个属性必须加上默认值，避免服务器返回json字段时抛出异常
 * **/
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T
)
