package otang.app.network.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.internal.EdgeToEdgeUtils
import otang.app.network.adapter.UsageAdapter
import otang.app.network.adapter.ViewAdapter
import otang.app.network.database.DatabaseHelper
import otang.app.network.database.Usage
import otang.app.network.databinding.ActivityMainBinding
import otang.app.network.model.Speed
import otang.app.network.service.TrafficService
import otang.app.network.util.AppUtils
import otang.app.network.util.TrafficUtils
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mLastRxBytes: Long = 0
    private var mLastTxBytes: Long = 0
    private var mLastTime: Long = 0
    private lateinit var helper: DatabaseHelper
    private lateinit var mSpeed: Speed
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPermission()
        setupWindow()
        initSpeed()
        initViewPager()
        initUsage()
        initUsageList()
    }

    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (isGranted()) {
                startTrafficService()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        } else {
            startTrafficService()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    }

    private fun initSpeed() {
        mSpeed = Speed(this)
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentRxBytes: Long = TrafficStats.getTotalRxBytes()
                val currentTxBytes: Long = TrafficStats.getTotalTxBytes()
                val usedRxBytes = currentRxBytes - mLastRxBytes
                val usedTxBytes = currentTxBytes - mLastTxBytes
                val currentTime = System.currentTimeMillis()
                val usedTime = currentTime - mLastTime
                mLastRxBytes = currentRxBytes
                mLastTxBytes = currentTxBytes
                mLastTime = currentTime
                mSpeed.calcSpeed(usedTime, usedRxBytes, usedTxBytes)
                updateSpeed(mSpeed)
            }
        }, 0, 1000)
    }

    @SuppressLint("SetTextI18n")
    private fun updateSpeed(speed: Speed) {
        runOnUiThread {
            val down: Speed.HumanSpeed = speed.getHumanSpeed("down")
            val up: Speed.HumanSpeed = speed.getHumanSpeed("up")
            binding.tvDownload.text = down.speedValue + " " + down.speedUnit
            binding.tvUpload.text = up.speedValue + " " + up.speedUnit
        }
    }

    private fun initViewPager() {
        val adapter = ViewAdapter(supportFragmentManager, lifecycle)
        binding.vp2.adapter = adapter
        binding.wdi.attachTo(binding.vp2)
    }

    private fun initUsage() {
        helper = DatabaseHelper.getInstance(this)!!
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                this@MainActivity.runOnUiThread {
                    val usage: Usage = helper.getTodayUsage(AppUtils.date)
                    val usageList: List<Usage> = helper.usageList
                    binding.tvTodayMobile.text = TrafficUtils.getMetricData(usage.mobile)
                    binding.tvTodayWifi.text = TrafficUtils.getMetricData(usage.wifi)
                    binding.tvTotalToday.text = TrafficUtils.getMetricData(usage.total)
                    var month: Long = 0
                    for (u in usageList) {
                        month += u.total
                    }
                    binding.tvTotalMonth.text = TrafficUtils.getMetricData(month)
                }
            }
        }, 0, 5000)
    }

    private fun initUsageList() {
        helper = DatabaseHelper.getInstance(this)!!
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                this@MainActivity.runOnUiThread {
                    val usageList: ArrayList<Usage> = helper.usageList
                    val adapter = UsageAdapter(this@MainActivity, usageList)
                    binding.rv.layoutManager = LinearLayoutManager(this@MainActivity)
                    binding.rv.adapter = adapter
                }
            }
        }, 0, 5000)
    }

    private fun startTrafficService() {
        val i = Intent(this, TrafficService::class.java)
        i.setPackage(this.packageName)
        startService(i)
    }

    @SuppressLint("RestrictedApi")
    private fun setupWindow() {
        EdgeToEdgeUtils.applyEdgeToEdge(window, true)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            initPermission()
        }
    }
}