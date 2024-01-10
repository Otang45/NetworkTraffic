package otang.app.network.util

import android.net.TrafficStats

object TrafficUtils {
    private const val GB: Long = 1000000000
    private const val MB: Long = 1000000
    private const val KB: Long = 1000
    val downloadSpeed: String
        get() {
            val speed: String
            val unit: String
            val bytesPrevious = TrafficStats.getTotalRxBytes()
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            val bytesCurrent = TrafficStats.getTotalRxBytes()
            val networkSpeed = bytesCurrent - bytesPrevious
            val downSpeed: Float
            if (networkSpeed >= GB) {
                downSpeed = (networkSpeed / GB).toFloat()
                unit = "GB/s"
            } else if (networkSpeed >= MB) {
                downSpeed = (networkSpeed / MB).toFloat()
                unit = "MB/s"
            } else {
                downSpeed = (networkSpeed / KB).toFloat()
                unit = "KB/s"
            }
            speed = String.format("%.1f", downSpeed)
            return speed + unit
        }
    val uploadSpeed: String
        get() {
            val speed: String
            val unit: String
            val bytesPrevious = TrafficStats.getTotalTxBytes()
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            val bytesCurrent = TrafficStats.getTotalTxBytes()
            val networkSpeed = bytesCurrent - bytesPrevious
            val upSpeed: Float
            if (networkSpeed >= GB) {
                upSpeed = (networkSpeed / GB).toFloat()
                unit = "GB/s"
            } else if (networkSpeed >= MB) {
                upSpeed = (networkSpeed / MB).toFloat()
                unit = "MB/s"
            } else {
                upSpeed = (networkSpeed / KB).toFloat()
                unit = "KB/s"
            }
            speed = String.format("%.1f", upSpeed)
            return speed + unit
        }
    val networkSpeed: Array<String>
        get() {
            val downloadSpeedOutput: String
            val unit: String
            val bytesPrevious =
                TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            val bytesCurrent =
                TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()
            val networkSpeed = bytesCurrent - bytesPrevious
            unit = if (networkSpeed >= GB) {
                "GB/s"
            } else if (networkSpeed >= MB) {
                "MB/s"
            } else {
                "KB/s"
            }
            downloadSpeedOutput = networkSpeed.toString()
            return arrayOf(downloadSpeedOutput, unit)
        }

    fun convertToBytes(value: Float, unit: String): Long {
        if (unit === "GB/s") {
            return value.toLong() * GB
        } else if (unit === "MB/s") {
            return value.toLong() * MB
        } else if (unit === "KB/s") {
            return value.toLong() * KB
        }
        return 0
    }

    fun getMetricData(bytes: Long): String {
        val dataWithDecimal: Float
        val units: String
        if (bytes >= GB) {
            dataWithDecimal = bytes.toFloat() / GB
            units = " GB"
        } else if (bytes >= MB) {
            dataWithDecimal = bytes.toFloat() / MB
            units = " MB"
        } else {
            dataWithDecimal = bytes.toFloat() / KB
            units = " KB"
        }
        return String.format("%.1f", dataWithDecimal) + units
    }
}