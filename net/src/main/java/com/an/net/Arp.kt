package com.an.net

import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStream
import java.net.*


private fun InputStream.readStreamAsList(): List<String> {
    return this.bufferedReader()
        .use {
            Log.d("ARP", "line=${it.readText()}")
            it.readLines()
        }
}

object Arp {
    private const val TAG = "ARP"

    suspend fun scan() {
        Log.d(TAG, "start")
        sendBroadcast()
        getArpTable()
        Log.d(TAG, "end")
    }

    private suspend fun sendBroadcast() = withContext(Dispatchers.IO) {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            var host = ""
            while (en.hasMoreElements()) {
                val inet = en.nextElement().inetAddresses
                while (inet.hasMoreElements()) {
                    inet.nextElement()
                        .takeIf { !it.isLoopbackAddress && it is Inet4Address }
                        ?.hostAddress?.let {
                            host = it.substringBeforeLast(".")
                        }
                }
            }
            Log.d(TAG, "host=$host")
            for (i in 0..255) {
                launch {
                    val ip = "$host.$i"
                    val inetAddress = InetAddress.getByName(ip)
                    inetAddress.isReachable(100)
                        .apply {
                            if (this) {
                                Log.d(
                                    TAG,
                                    "$inetAddress ${inetAddress.hostAddress} ${inetAddress.hostName} ${inetAddress.canonicalHostName}"
                                )
                            }
                        }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun getArpTable(): List<String> =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "getArpTable start")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getArpTableFromIpCommand()
            } else {
                getArpTableFromFile()
            }
        }

    private fun getArpTableFromFile(): List<String> {
        try {
            return File("/proc/net/arp").inputStream().readStreamAsList()
                .onEach {
                    Log.e(TAG, "arp file = $it")
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    private fun getArpTableFromIpCommand(): List<String> {
        try {
            val execution = Runtime.getRuntime().exec("ip neigh")
            execution.waitFor()
            Log.d(TAG, "${execution.exitValue()}")
            return execution.inputStream.readStreamAsList()
                .onEach {
                    Log.e(TAG, "ip neigh= $it")
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
}

