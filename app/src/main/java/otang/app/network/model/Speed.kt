package otang.app.network.model

import android.content.Context
import otang.app.network.R
import java.util.Locale

class Speed(private val mContext: Context) {
    private var mIsSpeedUnitBits = false
    private var mTotalSpeed: Long = 0
    private var mDownSpeed: Long = 0
    private var mUpSpeed: Long = 0
    private var total = HumanSpeed()
    private var down = HumanSpeed()
    private var up = HumanSpeed()

    init {
        updateHumanSpeeds()
    }

    private fun updateHumanSpeeds() {
        total.setSpeed(mTotalSpeed)
        down.setSpeed(mDownSpeed)
        up.setSpeed(mUpSpeed)
    }

    fun calcSpeed(timeTaken: Long, downBytes: Long, upBytes: Long) {
        var totalSpeed: Long = 0
        var downSpeed: Long = 0
        var upSpeed: Long = 0
        val totalBytes = downBytes + upBytes
        if (timeTaken > 0) {
            totalSpeed = totalBytes * 1000 / timeTaken
            downSpeed = downBytes * 1000 / timeTaken
            upSpeed = upBytes * 1000 / timeTaken
        }
        mTotalSpeed = totalSpeed
        mDownSpeed = downSpeed
        mUpSpeed = upSpeed
        updateHumanSpeeds()
    }

    fun getHumanSpeed(name: String?): HumanSpeed {
        return when (name) {
            "up" -> up
            "down" -> down
            else -> total
        }
    }

    fun setIsSpeedUnitBits(isSpeedUnitBits: Boolean) {
        mIsSpeedUnitBits = isSpeedUnitBits
    }

    inner class HumanSpeed {
        var speedValue: String? = null
        var speedUnit: String? = null

        @Suppress("NAME_SHADOWING", "KotlinConstantConditions")
        fun setSpeed(speed: Long) {
            var speed = speed
            if (mIsSpeedUnitBits) {
                speed *= 8
            }
            if (speed < 1000000) {
                speedUnit =
                    mContext.getString(if (mIsSpeedUnitBits) R.string.kbps else R.string.kBps)
                speedValue = (speed / 1000).toString()
            } else if (speed >= 1000000) {
                speedUnit =
                    mContext.getString(if (mIsSpeedUnitBits) R.string.Mbps else R.string.MBps)
                speedValue = if (speed < 10000000) {
                    String.format(Locale.ENGLISH, "%.1f", speed / 1000000.0)
                } else if (speed < 100000000) {
                    (speed / 1000000).toString()
                } else {
                    mContext.getString(R.string.plus99)
                }
            } else {
                speedValue = mContext.getString(R.string.dash)
                speedUnit = mContext.getString(R.string.dash)
            }
        }
    }
}
