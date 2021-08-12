package com.an.net.model

data class Configuration(
    val endpoint: String,
    val accessId: String,
    val accessSecret: String,
    val host: String,
    val bucketName: String
)