package com.an.net.util


fun Double.byteToKB(): Double {
    return this / 1024
}


fun Double.byteToMB(): Double {
    return this / 1024 / 1024
}


fun Double.byteToGB(): Double {
    return this / 1024 / 1024 / 1024
}


fun Double.byteToBit(): Double {
    return this * 8
}


fun Double.byteToKBit(): Double {
    return this * 8 / 1024
}


fun Double.byteToMBit(): Double {
    return this * 8 / 1024 / 1024
}


object DataUnit {
    /**
     * @param bytes 字节每秒
     * */
    fun networkSpeedRange(
        bytes: Double,
        mbpsList: List<Int> = listOf(
            0,
            1,
            2,
            3,
            4,
            6,
            8,
            10,
            12,
            20,
            30,
            50,
            100,
            200,
            500,
            1000,
            2000,
            5000,
            10000
        )
    ): String {
        if (bytes.compareTo(0) <= 0) {
            return "0M"
        }
        val mbps = bytes.byteToMBit()
        mbpsList.sorted().forEachIndexed { index, item ->
            if (mbps.compareTo(item) < 0) {
                return "${mbpsList[index - 1]}M~${item}M"
            }
        }
        return "大于${mbpsList.last()}M"
    }

    /**
     * @param mbps 字节每秒
     * */
    fun networkSpeedRange(
        mbps: Double,
        mbpsList: List<SpeedRangeWithTitle> = listOf(
            SpeedRangeWithTitle(1.92, "标清"),
            SpeedRangeWithTitle(2.56, "高清"),
            SpeedRangeWithTitle(8.0, "超清"),
            SpeedRangeWithTitle(16.0, "蓝光")
        )
    ): SpeedRangeWithTitle {
        mbpsList
            .sortedBy {
                it.max
            }.forEach {
                if (mbps.compareTo(it.max) <= 0) {
                    return it
                }
            }
        return mbpsList.last()
    }

    data class SpeedRangeWithTitle(val max: Double, val title: String)
}

