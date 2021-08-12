package com.an.net

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

object Ping {

    fun ping(address: String, count: Int = 3): PingInfo {
        Log.d("ping", "start")
        val process = Runtime.getRuntime().exec("ping -c $count  $address")
        val pingInfo = PingInfo()
        BufferedReader(InputStreamReader(process.inputStream)).useLines { result ->
            result.forEach { line ->
                Log.d("ping", line)
                if (line.contains("packets transmitted")) {
                    parsePacketsTransmitted(line, pingInfo)
                }
                if (line.startsWith("rtt")) {
                    parseRtt(line, pingInfo)
                }
            }
        }
        Log.d("ping", "pingInfo=$pingInfo")

        return pingInfo
    }

    private fun parseRtt(line: String, pingInfo: PingInfo) {
        val startIndex = line.indexOf("=")
        val endIndex = line.indexOf("ms")
        line.substring(startIndex + 1, endIndex)
            .split("/")
            .forEachIndexed { index, s ->
                when (index) {
                    0 -> pingInfo.rttMin = s.toFloat()

                    1 -> pingInfo.rttAvg = s.toFloat()

                    2 -> pingInfo.rttMax = s.toFloat()

                    3 -> pingInfo.rttMdev = s.toFloat()
                }
            }
    }

    private fun parsePacketsTransmitted(line: String, pingInfo: PingInfo) {
        val pattern = Pattern.compile("[^0-9]")
        line.split(",").forEach {
            when {
                it.contains("packets transmitted") ->
                    pingInfo.packetsTransmitted =
                        pattern.matcher(it).replaceAll("").trim().toFloat()

                it.contains("received") ->
                    pingInfo.packetsReceived = pattern.matcher(it).replaceAll("").trim().toFloat()

                it.contains("packet loss") ->
                    pingInfo.packetsLoss = pattern.matcher(it).replaceAll("").trim().toFloat()

                it.contains("time") ->
                    pingInfo.packetsTime = pattern.matcher(it).replaceAll("").trim().toFloat()
            }
        }
    }

    data class PingInfo(
        var rttMin: Float = 0f,            //发一个包最短时长
        var rttMax: Float = 0f,            //发一个包最大时长
        var rttAvg: Float = 0f,            //发包平均时长
        var rttMdev: Float = 0f,           //包差异时长，值越大，网络越不稳定
        var packetsTransmitted: Float = 0f,//发送的总包数
        var packetsReceived: Float = 0f,   //接受了的总包数
        var packetsLoss: Float = 0f,       //丢失的包百分比
        var packetsTime: Float = 0f        //总时长
    )
}