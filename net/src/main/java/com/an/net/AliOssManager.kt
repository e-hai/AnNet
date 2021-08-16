package com.an.net

import android.content.Context
import android.util.Log
import com.alibaba.sdk.android.oss.*
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider
import com.alibaba.sdk.android.oss.common.utils.OSSUtils
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.model.*
import com.an.net.model.Configuration
import com.an.net.model.SpeedResult
import com.an.net.model.SpeedState
import com.an.net.util.byteToMBit
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.*
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile


object AliOssManager {

    private const val TAG = "AliOssManager"
    private lateinit var oss: OSS
    private lateinit var conf: Configuration

    fun init(context: Context, conf: Configuration) {
        OSSLog.enableLog()
        this.conf = conf
        val endpoint = conf.endpoint
        val credentialProvider = object : OSSCustomSignerCredentialProvider() {
            override fun signContent(content: String?): String {
                return OSSUtils.sign(conf.accessId, conf.accessSecret, content)
            }
        }
        oss = OSSClient(context, endpoint, credentialProvider, null)
    }


    private var uploadTask: OSSAsyncTask<PutObjectResult>? = null

    fun testUploadSpeed(
        context: Context,
        objectName: String,
        fileSize: Long = 300 * 1024 * 1024,//上传文件大小 Byte
        interval: Long = 500,       //间隔时间 ms
        duration: Long = 15 * 1000, //最长时间 ms
        callBack: SpeedCallBack
    ) {

        Log.d(TAG, "upload start ")

        val file = File("${context.cacheDir.absolutePath}-$fileSize-test_speed").apply {
            RandomAccessFile(absolutePath, "rw").setLength(fileSize)
        }

        val put = PutObjectRequest(conf.bucketName, objectName, file.absolutePath)
        val startTime = System.currentTimeMillis()
        var lastTimeStamp = System.currentTimeMillis()
        put.setProgressCallback { _, currentSize, totalSize ->
            val nowTimeStamp = System.currentTimeMillis()
            if (interval < (nowTimeStamp - lastTimeStamp)) {
                val time = (nowTimeStamp - startTime).toDouble()
                val speed = currentSize / time * 1000
                lastTimeStamp = nowTimeStamp
                callBack(SpeedResult(SpeedState.LOADING, speed))
                Log.d(TAG, "upload current=$currentSize current=$totalSize fileSize=$speed")
            }

            if (duration < (nowTimeStamp - startTime)) {
                uploadTask?.cancel()
                Log.d(TAG, "upload time cancel")
            }
        }
        uploadTask = oss.asyncPutObject(put,
            object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                    callBack(SpeedResult(SpeedState.SUCCESS))
                    Log.d(TAG, "upload success")
                }

                override fun onFailure(
                    request: PutObjectRequest?,
                    clientException: ClientException?,
                    serviceException: ServiceException?
                ) {
                    callBack(SpeedResult(SpeedState.FAIL))
                    Log.d(TAG, "upload fail")
                }
            })
    }


    fun testDownSpeed(
        context: Context,
        url: String,
        interval: Long = 500,
        duration: Long = 15 * 1000, //最长时间 ms
        callBack: SpeedCallBack
    ) {
        var sink: BufferedSink? = null
        var source: BufferedSource? = null
        try {
            val destFile = File("${context.cacheDir}test-down-speed").apply { deleteOnExit() }
            val client = OkHttpClient()
            val request: Request = Request.Builder().url(url).build()
            val response: Response = client.newCall(request).execute()
            val body: ResponseBody = response.body ?: throw NullPointerException()

            source = body.source()
            sink = destFile.sink().buffer()
            val sinkBuffer: Buffer = sink.buffer
            val bufferSize = 1 * 1024L
            val startTime = System.currentTimeMillis()
            var lastTimeStamp = System.currentTimeMillis()
            var totalBytes = 0L
            while (source.read(sinkBuffer, bufferSize).also { totalBytes += it } != -1L) {
                sink.emit()
                val nowTimeStamp = System.currentTimeMillis()
                if (interval < (nowTimeStamp - lastTimeStamp)) {
                    val totalTime = (nowTimeStamp - startTime).toDouble()
                    val speed = totalBytes / totalTime * 1000
                    lastTimeStamp = nowTimeStamp
                    callBack(SpeedResult(SpeedState.LOADING, speed))
                    Log.d(TAG, "download total=$totalBytes speed=$speed")

                }
                if (duration < (nowTimeStamp - startTime)) {
                    break
                }
            }
            sink.flush()
            callBack(SpeedResult(SpeedState.SUCCESS))
        } catch (e: IOException) {
            e.printStackTrace()
            callBack(SpeedResult(SpeedState.FAIL))
        } finally {
            sink?.close()
            source?.close()
        }
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

