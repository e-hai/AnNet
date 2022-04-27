package com.an.net.alioss

import android.content.Context
import android.net.Uri
import android.util.Log
import com.alibaba.sdk.android.oss.*
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider
import com.alibaba.sdk.android.oss.common.utils.OSSUtils
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.model.*
import com.an.net.wifi.model.SpeedResult
import com.an.net.wifi.model.SpeedState
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.*
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile


class AliOssManager(context: Context, val conf: Configuration) {
    companion object {
        private const val TAG = "AliOssManager"
    }

    private val oss: OSS

    init {
        OSSLog.enableLog()
        val endpoint = conf.endpoint
        val credentialProvider = object : OSSCustomSignerCredentialProvider() {
            override fun signContent(content: String?): String {
                return OSSUtils.sign(conf.accessId, conf.accessSecret, content)
            }
        }
        // 配置类如果不设置，会有默认配置。
        val clientConfiguration = ClientConfiguration()
        clientConfiguration.connectionTimeout = 15 * 1000 // 连接超时，默认15秒。
        clientConfiguration.socketTimeout = 15 * 1000 // socket超时，默认15秒。
        clientConfiguration.maxConcurrentRequest = 5 // 最大并发请求数，默认5个。
        clientConfiguration.maxErrorRetry = 2 // 失败后最大重试次数，默认2次。
        oss = OSSClient(context, endpoint, credentialProvider, clientConfiguration)
    }

    fun asyncPutObject(
        objectName: String,
        fileUri: Uri,
        progressCallback: OSSProgressCallback<PutObjectRequest>,
        completedCallback: OSSCompletedCallback<PutObjectRequest, PutObjectResult>
    ): OSSAsyncTask<PutObjectResult> {
        val put = PutObjectRequest(conf.bucketName, objectName, fileUri)
        put.progressCallback = progressCallback
        return oss.asyncPutObject(put, completedCallback)
    }

    fun putObject(
        objectName: String,
        fileUri: Uri
    ): AliOssResult {
        val put = PutObjectRequest(conf.bucketName, objectName, fileUri)
        oss.putObject(put)
        val fileUrl = getFileUrl(objectName)
        return AliOssResult(objectName, fileUrl)
    }

    fun delFile(objectName: String): DeleteObjectResult {
        val del = DeleteObjectRequest(conf.bucketName, objectName)
        return oss.deleteObject(del)
    }

    fun getFileUrl(objectName: String): String {
        return conf.host + objectName
    }

}

typealias SpeedCallBack = (SpeedResult) -> Unit

data class AliOssResult(val objectName: String, val fileUrl: String)


