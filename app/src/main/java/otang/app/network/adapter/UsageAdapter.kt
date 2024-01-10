package otang.network.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.util.ArrayList;
import otang.network.R;
import otang.network.adapter.UsageAdapter.UsageHolder;
import androidx.recyclerview.widget.RecyclerView;
import otang.network.database.Usage;
import otang.network.databinding.UsageItemBinding;
import otang.network.util.TrafficUtils;

public class UsageAdapter extends RecyclerView.Adapter<UsageAdapter.UsageHolder> {

	public static class UsageHolder extends RecyclerView.ViewHolder {

		private UsageItemBinding binding;

		public UsageHolder(UsageItemBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}

	private Context context;
	private ArrayList<Usage> usageList;

	public UsageAdapter(Context context, ArrayList<Usage> usageList) {
		this.context = context;
		this.usageList = usageList;
	}

	@Override
	public UsageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new UsageHolder(UsageItemBinding.inflate(LayoutInflater.from(context), parent, false));
	}

	@Override
	public void onBindViewHolder(UsageHolder holder, int position) {
		Usage usage = usageList.get(position);
		holder.binding.tvDate.setText(usage.day);
		holder.binding.tvMobile.setText(TrafficUtils.getMetricData(usage.mobile));
		holder.binding.tvWifi.setText(TrafficUtils.getMetricData(usage.wifi));
		holder.binding.tvTotal.setText(TrafficUtils.getMetricData(usage.total));
		if (position % 2 == 0) {
			holder.binding.getRoot().setCardBackgroundColor(context.getColor(R.color.colorOnSurfaceInverse));
		} else {
			holder.binding.getRoot().setCardBackgroundColor(context.getColor(R.color.colorSurface));
		}
	}

	@Override
	public int getItemCount() {
		return usageList.size();
	}

}