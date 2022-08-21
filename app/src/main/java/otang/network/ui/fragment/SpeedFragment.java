package otang.network.ui.fragment;

import android.net.TrafficStats;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.util.TimerTask;
import java.util.Timer;
import otang.network.R;
import otang.network.databinding.SpeedFragmentBinding;
import otang.network.model.Speed;
import otang.network.util.AppUtils;

public class SpeedFragment extends Fragment {

	private SpeedFragmentBinding binding;
	private long mLastRxBytes = 0;
	private long mLastTxBytes = 0;
	private long mLastTime = 0;
	private Speed mSpeed;

	public SpeedFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle arguments) {
		binding = SpeedFragmentBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(View view, Bundle arguments) {
		super.onViewCreated(view, arguments);
		mSpeed = new Speed(getActivity());
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				long currentRxBytes = TrafficStats.getTotalRxBytes();
				long currentTxBytes = TrafficStats.getTotalTxBytes();
				long usedRxBytes = currentRxBytes - mLastRxBytes;
				long usedTxBytes = currentTxBytes - mLastTxBytes;
				long currentTime = System.currentTimeMillis();
				long usedTime = currentTime - mLastTime;
				mLastRxBytes = currentRxBytes;
				mLastTxBytes = currentTxBytes;
				mLastTime = currentTime;
				mSpeed.calcSpeed(usedTime, usedRxBytes, usedTxBytes);
				if (getActivity() != null)
					updateSpeed(mSpeed);
			}
		}, 0, 1000);
	}

	private void updateSpeed(Speed speed) {
		getActivity().runOnUiThread(() -> {
			Speed.HumanSpeed speedToShow = speed.getHumanSpeed("total");
			String metric;
			int value;
			if (speedToShow.speedUnit != getString(R.string.MBps)
					|| speedToShow.speedUnit != getString(R.string.Mbps)) {
				metric = "x2 KB/s";
				value = (Long.valueOf(speedToShow.speedValue).intValue() / 10);
			} else {
				metric = "x2 KB+/s";
				value = 100;
			}
			binding.sm.setMetricText(metric);
			binding.sm.setSpeed(value, 500, null);
		});
	}
}