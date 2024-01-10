package otang.app.network.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import otang.app.network.database.Usage
import otang.app.network.databinding.UsageItemBinding
import otang.app.network.util.ColourUtils
import otang.app.network.util.TrafficUtils

class UsageAdapter(private val context: Context, usageList: ArrayList<Usage>) :
    RecyclerView.Adapter<UsageAdapter.UsageHolder>() {
    class UsageHolder(binding: UsageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val binding: UsageItemBinding

        init {
            this.binding = binding
        }
    }

    private val usageList: ArrayList<Usage>

    init {
        this.usageList = usageList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsageHolder {
        return UsageHolder(UsageItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: UsageHolder, position: Int) {
        val usage: Usage = usageList[position]
        holder.binding.tvDate.text = usage.day
        holder.binding.tvMobile.text = TrafficUtils.getMetricData(usage.mobile)
        holder.binding.tvWifi.text = TrafficUtils.getMetricData(usage.wifi)
        holder.binding.tvTotal.text = TrafficUtils.getMetricData(usage.total)
        if (position % 2 == 0) {
            holder.binding.root.setCardBackgroundColor(ColourUtils.getOnSurfaceInverse(context))
        } else {
            holder.binding.root.setCardBackgroundColor(ColourUtils.getSurface(context))
        }
    }

    override fun getItemCount(): Int {
        return usageList.size
    }
}