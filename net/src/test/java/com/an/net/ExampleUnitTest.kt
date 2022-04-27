package com.an.net

import com.an.net.wifi.util.DataUnit
import com.an.net.wifi.util.byteToMBit
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val data = (-1.0).apply {
            println(this.byteToMBit())
        }

        DataUnit.networkSpeedRange(data).apply {
            println(this)
        }
    }
}