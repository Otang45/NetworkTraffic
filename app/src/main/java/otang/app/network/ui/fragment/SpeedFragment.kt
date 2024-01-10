package otang.app.network.ui.fragment

import android.net.TrafficStats
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import otang.app.network.R
import otang.app.network.databinding.SpeedFragmentBinding
import otang.app.network.model.Speed
import java.util.Timer
import java.util.TimerTask

class SpeedFragment : Fragment() {
    private lateinit var binding: SpeedFragmentBinding
    private var mLastRxBytes: Long = 0
    private var mLastTxBytes: Long = 0
    private var mLastTime: Long = 0
    private lateinit var mSpeed: Speed
    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        arguments: Bundle?
    ): View {
        binding = SpeedFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, arguments: Bundle?) {
        super.onViewCreated(view, arguments)
        mSpeed = Speed(requireActivity())
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
                if (activity != null) updateSpeed(mSpeed)
            }
        }, 0, 1000)
    }

    private fun updateSpeed(speed: Speed) {
        requireActivity().runOnUiThread {
            val speedToShow: Speed.HumanSpeed = speed.getHumanSpeed("total")
            val metric: String
            val value: Int
            if (speedToShow.speedUnit != getString(R.string.MBps)
                || speedToShow.speedUnit != getString(R.string.Mbps)
            ) {
                metric = "x2 KB/s"
                value = (speedToShow.speedValue?.toInt() ?: 0) / 10
            } else {
                metric = "x2 KB+/s"
                value = 100
            }
            binding.sm.metricText = metric
            binding.sm.setSpeed(value, 500, null)
        }
    }
}