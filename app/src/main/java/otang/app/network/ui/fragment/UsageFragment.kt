package otang.network.ui.fragment;

import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import otang.network.R;
import otang.network.database.DatabaseHelper;
import otang.network.databinding.UsageFragmentBinding;
import otang.network.util.ChartUtils;
import otang.network.util.LargeValueFormatterBytes;

public class UsageFragment extends Fragment {

	private UsageFragmentBinding binding;
	private DatabaseHelper helper;
	private List<BarEntry> networkList;
	boolean isChartUpdated = false;
	boolean isWifiSelected = false;

	public UsageFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle arguments) {
		binding = UsageFragmentBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(View view, Bundle arguments) {
		super.onViewCreated(view, arguments);
		setupSpinner();
		helper = DatabaseHelper.getInstance(getActivity());
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (getActivity() != null)
					getActivity().runOnUiThread(() -> {
						if (isWifiSelected) {
							networkList = ChartUtils.getDailyWifiEntry(helper.getUsageList());
						} else {
							networkList = ChartUtils.getDailyMobileEntry(helper.getUsageList());
						}
						if (!isChartUpdated) {
							setupLineChart(networkList);
							isChartUpdated = true;
						}
					});
			}
		}, 0, 5000);
	}

	private void setupSpinner() {
		binding.acs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				isWifiSelected = position != 0;
				isChartUpdated = false;
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});
	}

	private void setupLineChart(List<BarEntry> entries) {
		binding.bc.invalidate();
		try {
			BarDataSet barDataSet = new BarDataSet(entries, "Usage");
			barDataSet.setColor(getActivity().getColor(R.color.colorPrimary));
			BarData barData = new BarData(barDataSet);
			barData.setValueFormatter(new LargeValueFormatterBytes());
			binding.bc.getDescription().setEnabled(false);
			XAxis xAxis = binding.bc.getXAxis();
			xAxis.setDrawAxisLine(false);
			xAxis.setDrawLabels(false);
			xAxis.setDrawGridLines(false);
			YAxis yAxis = binding.bc.getAxisLeft();
			yAxis.setDrawLabels(false);
			yAxis.setDrawAxisLine(false);
			yAxis.setDrawGridLines(false);
			yAxis.setDrawZeroLine(false);
			binding.bc.getAxisRight().setEnabled(false);
			binding.bc.setData(barData);
			binding.bc.setPinchZoom(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}