package com.an.net

import android.content.Context
import android.net.TrafficStats
import android.util.Log
import com.alibaba.sdk.android.oss.*
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider
import com.alibaba.sdk.android.oss.common.utils.OSSUtils
import com.alibaba.sdk.android.oss.model.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.*
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.lang.Exception


object AliOssManager {

    private const val TAG = "AliOssManager"
    private const val UNIT_BYTES = 1024 * 1024L  //MB
    private const val UNIT_TIME = 1000           //S
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

    fun testUploadSpeed(
        context: Context,
        objectName: String,
        fileSize: Long = 10,
        callBack: SpeedCallBack
    ) {
        try {
            val file = File("${context.cacheDir.absolutePath}-$fileSize-test_speed")
            if (!file.exists()) {
                RandomAccessFile(file.absolutePath, "rw").apply {
                    setLength(fileSize * UNIT_BYTES)
                }
            }
            val put = PutObjectRequest(conf.bucketName, objectName, file.absolutePath)
            var lastTimeStamp = System.currentTimeMillis()
            var lastTotalBytes = TrafficStats.getTotalTxBytes()
            put.setProgressCallback { _, _, _ ->
                val nowTotalBytes = TrafficStats.getTotalTxBytes()
                val nowTimeStamp = System.currentTimeMillis()
                if (nowTotalBytes != lastTotalBytes && nowTimeStamp != lastTimeStamp) {
                    val bytes = (nowTotalBytes - lastTotalBytes).toDouble() / UNIT_BYTES
                    val time = (nowTimeStamp - lastTimeStamp).toDouble() / UNIT_TIME
                    val speed = bytes / time
                    lastTotalBytes = nowTotalBytes
                    lastTimeStamp = nowTimeStamp
                    Log.d(TAG, "put bytes=$bytes time=$time speed=$speed ")
                }
            }
            oss.putObject(put)
        } catch (e: Exception) {
            e.printStackTrace()
            callBack(SpeedResult(SpeedResult.State.FAIL))
        }
        callBack(SpeedResult(SpeedResult.State.SUCCESS))
    }

    fun testDownSpeed(objectName: String, callBack: SpeedCallBack) {
        try {
            val down = GetObjectRequest(conf.bucketName, objectName)
            var lastTimeStamp = System.currentTimeMillis()
            var lastTotalBytes = TrafficStats.getTotalRxBytes()
            down.setProgressListener { _, _, _ ->
                val nowTotalBytes = TrafficStats.getTotalRxBytes()
                val nowTimeStamp = System.currentTimeMillis()
                if (nowTotalBytes != lastTotalBytes && nowTimeStamp != lastTimeStamp) {
                    val bytes = (nowTotalBytes - lastTotalBytes).toDouble() / UNIT_BYTES
                    val time = (nowTimeStamp - lastTimeStamp).toDouble() / UNIT_TIME
                    val speed = bytes / time
                    lastTotalBytes = nowTotalBytes
                    lastTimeStamp = nowTimeStamp
                    callBack(SpeedResult(SpeedResult.State.LOADING, speed))
                    Log.d(TAG, "get bytes=$bytes time=$time speed=$speed ")
                }
            }
            oss.getObject(down)
        } catch (e: Exception) {
            e.printStackTrace()
            callBack(SpeedResult(SpeedResult.State.FAIL))
        }
        callBack(SpeedResult(SpeedResult.State.SUCCESS))
    }

    fun testDownSpeed(
        context: Context,
        url: String,
        maxDownTime: Long = 15,
        callBack: SpeedCallBack
    ) {
        var sink: BufferedSink? = null
        var source: BufferedSource? = null
        try {
            val destFile = File("${context.cacheDir}test-down-speed")
            val client = OkHttpClient()
            val request: Request = Request.Builder().url(url).build()
            val response: Response = client.newCall(request).execute()
            val body: ResponseBody = response.body ?: throw NullPointerException()

            source = body.source()
            sink = destFile.sink().buffer()
            val sinkBuffer: Buffer = sink.buffer
            val bufferSize = 10 * 1024
            val startTime = System.currentTimeMillis()
            var lastTimeStamp = System.currentTimeMillis()
            var lastTotalBytes = TrafficStats.getTotalRxBytes()

            while (source.read(sinkBuffer, bufferSize.toLong()) != -1L) {
                if (maxDownTime * 1000 < System.currentTimeMillis() - startTime) {
                    break
                }
                sink.emit()
                val nowTotalBytes = TrafficStats.getTotalRxBytes()
                val nowTimeStamp = System.currentTimeMillis()
                if (nowTotalBytes != lastTotalBytes && nowTimeStamp != lastTimeStamp) {
                    val bytes = (nowTotalBytes - lastTotalBytes).toDouble() / UNIT_BYTES
                    val time = (nowTimeStamp - lastTimeStamp).toDouble() / UNIT_TIME
                    val speed = bytes / time
                    lastTotalBytes = nowTotalBytes
                    lastTimeStamp = nowTimeStamp
                    callBack(SpeedResult(SpeedResult.State.LOADING, speed))
                    Log.d(TAG, "get bytes=$bytes time=$time speed=$speed ")
                }
            }
            sink.flush()
            callBack(SpeedResult(SpeedResult.State.SUCCESS))
        } catch (e: IOException) {
            e.printStackTrace()
            callBack(SpeedResult(SpeedResult.State.FAIL))
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

data class Configuration(
    val endpoint: String,
    val accessId: String,
    val accessSecret: String,
    val host: String,
    val bucketName: String
)

data class SpeedResult(val state: State, val speed: Double = 0.0) {
    enum class State {
        SUCCESS, FAIL, LOADING
    }
}

typealias SpeedCallBack = (SpeedResult) -> Unit