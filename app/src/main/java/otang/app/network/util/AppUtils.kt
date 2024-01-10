package otang.app.network.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import java.text.SimpleDateFormat

object AppUtils {
    val date: String
        @SuppressLint("SimpleDateFormat")
        get() {
            val timestamp = System.currentTimeMillis()
            val format = SimpleDateFormat("dd-MMM-yyyy")
            return format.format(timestamp)
        }

    @Suppress("DEPRECATION")
    fun isWifiConnection(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.activeNetworkInfo
        return if (cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected) {
            (info?.type ?: ConnectivityManager.TYPE_WIFI) == ConnectivityManager.TYPE_WIFI
        } else false
    }
}