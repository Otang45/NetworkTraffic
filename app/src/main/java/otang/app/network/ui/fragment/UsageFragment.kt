package otang.app.network.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import otang.app.network.database.DatabaseHelper
import otang.app.network.databinding.UsageFragmentBinding
import otang.app.network.util.ChartUtils
import otang.app.network.util.ColourUtils
import otang.app.network.util.LargeValueFormatterBytes
import java.util.Timer
import java.util.TimerTask

class UsageFragment : Fragment() {
    private lateinit var binding: UsageFragmentBinding
    private lateinit var helper: DatabaseHelper
    private lateinit var networkList: List<BarEntry>
    var isChartUpdated = false
    var isWifiSelected = false
    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        arguments: Bundle?
    ): View {
        binding = UsageFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, arguments: Bundle?) {
        super.onViewCreated(view, arguments)
        setupSpinner()
        helper = DatabaseHelper.getInstance(requireActivity())!!
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                requireActivity().runOnUiThread {
                    networkList = if (isWifiSelected) {
                        ChartUtils.getDailyWifiEntry(helper.usageList)
                    } else {
                        ChartUtils.getDailyMobileEntry(helper.usageList)
                    }
                    if (!isChartUpdated) {
                        setupLineChart(networkList)
                        isChartUpdated = true
                    }
                }
            }
        }, 0, 5000)
    }

    private fun setupSpinner() {
        binding.acs.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                isWifiSelected = position != 0
                isChartUpdated = false
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun setupLineChart(entries: List<BarEntry>) {
        binding.bc.invalidate()
        try {
            val barDataSet = BarDataSet(entries, "Usage")
            barDataSet.color = ColourUtils.getPrimary(requireActivity())
            val barData = BarData(barDataSet)
            barData.setValueFormatter(LargeValueFormatterBytes())
            binding.bc.description.isEnabled = false
            val xAxis: XAxis = binding.bc.xAxis
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawLabels(false)
            xAxis.setDrawGridLines(false)
            val yAxis: YAxis = binding.bc.axisLeft
            yAxis.setDrawLabels(false)
            yAxis.setDrawAxisLine(false)
            yAxis.setDrawGridLines(false)
            yAxis.setDrawZeroLine(false)
            binding.bc.axisRight.isEnabled = false
            binding.bc.data = barData
            binding.bc.setPinchZoom(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}