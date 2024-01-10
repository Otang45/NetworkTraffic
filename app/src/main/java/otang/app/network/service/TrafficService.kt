package otang.app.network.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.TrafficStats
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import otang.app.network.R
import otang.app.network.database.DatabaseHelper
import otang.app.network.database.Usage
import otang.app.network.model.Speed
import otang.app.network.ui.activity.MainActivity
import otang.app.network.util.AppUtils
import otang.app.network.util.ImageUtils
import otang.app.network.util.PrefUtils
import otang.app.network.util.TrafficUtils
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class TrafficService : Service() {
    private var mLastRxBytes: Long = 0
    private var mLastTxBytes: Long = 0
    private var mLastTime: Long = 0
    private lateinit var mSpeed: Speed
    private val timer = Timer()
    private lateinit var helper: DatabaseHelper
    private lateinit var prefUtils: PrefUtils
    private var notificationChannel: NotificationChannel? = null
    private lateinit var builder: NotificationCompat.Builder
    private var notificationManager: NotificationManager? = null
    private val mSpeedToShow = "total"
    private val mSpeedToShow1 = "down"
    private val mSpeedToShow2 = "up"
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        init()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        init()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentRxBytes = TrafficStats.getTotalRxBytes()
                val currentTxBytes = TrafficStats.getTotalTxBytes()
                val currentTime = System.currentTimeMillis()
                val usedRxBytes = currentRxBytes - mLastRxBytes
                val usedTxBytes = currentTxBytes - mLastTxBytes
                val usedTime = currentTime - mLastTime
                mLastRxBytes = currentRxBytes
                mLastTxBytes = currentTxBytes
                mLastTime = currentTime
                mSpeed.calcSpeed(usedTime, usedRxBytes, usedTxBytes)
                val speed = TrafficUtils.networkSpeed
                saveToDatabase(java.lang.Long.valueOf(speed[0]))
                val usage = helper.getTodayUsage(AppUtils.date)
                updateNotification(mSpeed, usage.total)
            }
        }, 0, 1000)
    }

    @SuppressLint("ForegroundServiceType")
    private fun init() {
        prefUtils = PrefUtils(this)
        helper = DatabaseHelper.getInstance(this)!!
        mSpeed = Speed(this)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationChannel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        //Create channel
        if (notificationManager != null) {
            notificationManager!!.createNotificationChannel(notificationChannel!!)
        }
        builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setSmallIcon(
            IconCompat.createWithBitmap(
                ImageUtils.createBitmapFromString(
                    "0",
                    "KB/s"
                )
            )
        )
        //Title
        builder.setContentTitle(getString(R.string.app_name))
        // Notification content
        builder.setContentText("Network traffic")
        //High priority
        builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        //Pattern
        val pattern = longArrayOf(0, 100, 200, 300)
        builder.setVibrate(pattern)
        builder.setContentIntent(createPendingIntent())
        builder.setAutoCancel(true)
        builder.setOngoing(false)
        builder.setOnlyAlertOnce(true)
        startForeground(NOTIFICATION_ID, builder.build())
        //for reset
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        val intent = Intent(this, TrafficService::class.java)
        val pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY,
            pIntent
        )
    }

    private fun saveToDatabase(bytes: Long) {
        val usage = Usage()
        usage.day = AppUtils.date
        val mobile: Long
        val wifi: Long
        val total: Long
        if (AppUtils.isWifiConnection(this)) {
            mobile = prefUtils.getLong("mobile")
            wifi = bytes + prefUtils.getLong("wifi")
            prefUtils.saveAs("wifi", wifi)
            total = mobile + wifi
            prefUtils.saveAs("total", total)
        } else {
            mobile = bytes + prefUtils.getLong("mobile")
            prefUtils.saveAs("mobile", mobile)
            wifi = prefUtils.getLong("wifi")
            total = mobile + wifi
            prefUtils.saveAs("total", total)
        }
        usage.mobile = mobile
        usage.wifi = wifi
        usage.total = total
        helper.addOrUpdateUser(usage)
    }

    private fun updateNotification(speed: Speed, usage: Long) {
        val speedToShow = speed.getHumanSpeed(mSpeedToShow)
        val bitmap = ImageUtils.createBitmapFromString(
            speedToShow.speedValue,
            speedToShow.speedUnit
        )
        val iconCompat = IconCompat.createWithBitmap(bitmap)
        builder.setSmallIcon(iconCompat)
        builder.setContentTitle("Today Usage : " + TrafficUtils.getMetricData(usage))
        val speedToShow1 = speed.getHumanSpeed(mSpeedToShow1)
        val speedToShow2 = speed.getHumanSpeed(mSpeedToShow2)
        builder.setContentText(
            "Download : " + speedToShow1.speedValue + " " + speedToShow1.speedUnit + "  -  "
                    + "Upload : " + speedToShow2.speedValue + " " + speedToShow2.speedUnit
        )
        notificationManager!!.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        var CHANNEL_ID = "Channel-01"
        var CHANNEL_NAME = "Notifikasi"
        var NOTIFICATION_ID = 1
    }
}