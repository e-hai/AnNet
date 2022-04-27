package com.an.net.alioss

data class Configuration(
    val endpoint: String,
    val accessId: String,
    val accessSecret: String,
    val host: String,
    val bucketName: String
)