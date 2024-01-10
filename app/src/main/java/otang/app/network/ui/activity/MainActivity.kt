package otang.network.ui.activity;

import android.content.Intent;
import android.net.TrafficStats;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.Timer;
import otang.network.R;
import otang.network.adapter.UsageAdapter;
import otang.network.adapter.ViewAdapter;
import otang.network.database.DatabaseHelper;
import otang.network.database.Usage;
import otang.network.databinding.ActivityMainBinding;
import otang.network.model.Speed;
import otang.network.preference.WindowPreference;
import otang.network.service.TrafficService;
import otang.network.util.AppUtils;
import otang.network.util.TrafficUtils;

public class MainActivity extends AppCompatActivity {

	private ActivityMainBinding binding;
	private long mLastRxBytes = 0;
	private long mLastTxBytes = 0;
	private long mLastTime = 0;
	private DatabaseHelper helper;
	private Speed mSpeed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		startTrafficService();
		setupWindow();
		initSpeed();
		initViewPager();
		initUsage();
		initUsageList();
	}

	private void initSpeed() {
		mSpeed = new Speed(this);
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
				updateSpeed(mSpeed);
			}
		}, 0, 1000);
	}

	private void updateSpeed(Speed speed) {
		runOnUiThread(() -> {
			Speed.HumanSpeed down = speed.getHumanSpeed("down");
			Speed.HumanSpeed up = speed.getHumanSpeed("up");
			binding.tvDownload.setText(down.speedValue + " " + down.speedUnit);
			binding.tvUpload.setText(up.speedValue + " " + up.speedUnit);
		});
	}

	private void initViewPager() {
		ViewAdapter adapter = new ViewAdapter(getSupportFragmentManager(), getLifecycle());
		binding.vp2.setAdapter(adapter);
		binding.wdi.setViewPager2(binding.vp2);
	}

	private void initUsage() {
		helper = DatabaseHelper.getInstance(this);
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				MainActivity.this.runOnUiThread(() -> {
					Usage usage = helper.getTodayUsage(AppUtils.getDate());
					List<Usage> usageList = helper.getUsageList();
					binding.tvTodayMobile.setText(TrafficUtils.getMetricData(usage.mobile));
					binding.tvTodayWifi.setText(TrafficUtils.getMetricData(usage.wifi));
					binding.tvTotalToday.setText(TrafficUtils.getMetricData(usage.total));
					long month = 0;
					for (Usage u : usageList) {
						month += u.total;
					}
					binding.tvTotalMonth.setText(TrafficUtils.getMetricData(month));
				});
			}
		}, 0, 5000);
	}

	private void initUsageList() {
		helper = DatabaseHelper.getInstance(this);
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				MainActivity.this.runOnUiThread(() -> {
					ArrayList<Usage> usageList = helper.getUsageList();
					UsageAdapter adapter = new UsageAdapter(MainActivity.this, usageList);
					binding.rv.setLayoutManager(new LinearLayoutManager(MainActivity.this));
					binding.rv.setAdapter(adapter);
				});
			}
		}, 0, 5000);
	}

	private void startTrafficService() {
		Intent i = new Intent(this, TrafficService.class);
		i.setPackage(this.getPackageName());
		startService(i);
	}

	private void setupWindow() {
		new WindowPreference(this).applyEdgeToEdgePreference(getWindow(), getColor(R.color.colorSurface));
		AppUtils.addSystemWindowInsetToPadding(binding.getRoot(), false, true, false, true);
	}

}