package otang.app.network.util

import com.github.mikephil.charting.data.BarEntry
import otang.app.network.database.Usage
import java.util.Collections.reverse

object ChartUtils {
    fun getDailyMobileEntry(list: List<Usage?>): List<BarEntry> {
        val entries: MutableList<BarEntry> = ArrayList()
        var xEntry = 0.0f
        reverse(list)
        for (usage in list) {
            val entry = BarEntry(xEntry, usage!!.mobile.toFloat())
            xEntry += 1f
            entries.add(entry)
        }
        return entries
    }

    fun getDailyWifiEntry(list: List<Usage?>): List<BarEntry> {
        val entries: MutableList<BarEntry> = ArrayList()
        var xEntry = 0.0f
        reverse(list)
        for (usage in list) {
            val entry = BarEntry(xEntry, usage!!.wifi.toFloat())
            xEntry += 1f
            entries.add(entry)
        }
        return entries
    }
}